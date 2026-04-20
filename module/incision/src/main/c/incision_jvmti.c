/*
 * incision_jvmti.c — Minimal JVMTI native library for Incision.
 *
 * Provides retransformClasses + ClassFileLoadHook without requiring
 * -javaagent or -XX:+EnableDynamicAgentLoading.
 *
 * Uses JNI dynamic registration via JNI_OnLoad so that the Java-side
 * class name can be freely relocated by Gradle Shadow without breaking
 * the native linkage. The Kotlin side sets system property
 * "incision.jvmti.class" to the (possibly relocated) class name before
 * calling System.load().
 *
 * Build (Windows MSVC):
 *   cl /LD /O2 /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32"
 *      incision_jvmti.c /Fe:incision-jvmti.dll
 *
 * Build (Linux GCC):
 *   gcc -shared -fPIC -O2 -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux"
 *      incision_jvmti.c -o libincision-jvmti.so
 *
 * Build (macOS):
 *   gcc -shared -fPIC -O2 -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/darwin"
 *      incision_jvmti.c -o libincision-jvmti.dylib
 *
 * Apache-2.0
 */

#include <jni.h>
#include <jvmti.h>
#include <string.h>
#include <stdlib.h>

/* ================================================================== */
/*  Global state                                                       */
/* ================================================================== */

static jvmtiEnv *g_jvmti          = NULL;
static JavaVM   *g_jvm            = NULL;
static jclass    g_backend_class   = NULL;
static jmethodID g_callback_mid    = NULL;

/* ================================================================== */
/*  字节码缓存（open-addressing hashmap，线性探测）                     */
/* ================================================================== */

#define CACHE_CAPACITY 512

typedef struct {
    char *key;          /* owner internal name（strdup 分配，NULL 表示空槽） */
    jbyte *bytes;       /* 原始字节码（malloc 分配） */
    jint len;           /* 字节码长度 */
} CacheEntry;

static CacheEntry g_cache[CACHE_CAPACITY];
static int g_cache_count = 0;

/* JVMTI raw monitor，用于保护缓存并发访问 */
static jrawMonitorID g_monitor = NULL;

/* 二次 retransform 协调：dummy extract 模式 */
static volatile int g_extract_mode = 0;
static jbyte *g_extract_buf = NULL;
static jint g_extract_len = 0;

/* djb2 字符串哈希 */
static unsigned int hash_str(const char *s) {
    unsigned int h = 5381;
    int c;
    while ((c = (unsigned char)*s++) != 0) {
        h = ((h << 5) + h) + c; /* h * 33 + c */
    }
    return h;
}

/* 查找 key 对应的槽索引，找不到返回 -1 */
static int cache_find(const char *key) {
    if (key == NULL) return -1;
    unsigned int h = hash_str(key) % CACHE_CAPACITY;
    for (int i = 0; i < CACHE_CAPACITY; i++) {
        int idx = (int)((h + i) % CACHE_CAPACITY);
        if (g_cache[idx].key == NULL) return -1;   /* 空槽，终止探测 */
        if (strcmp(g_cache[idx].key, key) == 0) return idx;
    }
    return -1;
}

/* 存入或覆盖缓存条目；成功返回 1，哈希表满返回 0 */
static int cache_put(const char *key, const jbyte *bytes, jint len) {
    if (key == NULL) return 0;
    unsigned int h = hash_str(key) % CACHE_CAPACITY;
    for (int i = 0; i < CACHE_CAPACITY; i++) {
        int idx = (int)((h + i) % CACHE_CAPACITY);
        if (g_cache[idx].key == NULL) {
            /* 新条目：占用空槽 */
            g_cache[idx].key = strdup(key);
            if (g_cache[idx].key == NULL) return 0;
            g_cache[idx].bytes = (jbyte *)bytes;
            g_cache[idx].len = len;
            g_cache_count++;
            return 1;
        }
        if (strcmp(g_cache[idx].key, key) == 0) {
            /* 覆盖现有条目：释放旧字节码 */
            if (g_cache[idx].bytes != NULL) free(g_cache[idx].bytes);
            g_cache[idx].bytes = (jbyte *)bytes;
            g_cache[idx].len = len;
            return 1;
        }
    }
    return 0; /* 哈希表已满 */
}

/* 移除并释放 key 对应的条目 */
static void cache_remove(const char *key) {
    int idx = cache_find(key);
    if (idx < 0) return;
    if (g_cache[idx].key != NULL) { free(g_cache[idx].key); g_cache[idx].key = NULL; }
    if (g_cache[idx].bytes != NULL) { free(g_cache[idx].bytes); g_cache[idx].bytes = NULL; }
    g_cache[idx].len = 0;
    g_cache_count--;
    /* 注：不做线性探测 rehash，因 cache_find 遇 NULL 即停；
       被移除槽后续的同哈希链条目再次查询时会返回 -1。
       可接受代价：同链条目在删除后变得不可达，
       但 Incision 用法以写入和读取为主，很少删除，因此暂不实现 rehash。 */
}

/* 清空全部缓存 */
static void cache_clear() {
    for (int i = 0; i < CACHE_CAPACITY; i++) {
        if (g_cache[i].key != NULL) { free(g_cache[i].key); g_cache[i].key = NULL; }
        if (g_cache[i].bytes != NULL) { free(g_cache[i].bytes); g_cache[i].bytes = NULL; }
        g_cache[i].len = 0;
    }
    g_cache_count = 0;
}

/* ================================================================== */
/*  ClassFileLoadHook                                                  */
/* ================================================================== */

static void JNICALL classFileLoadHook(
        jvmtiEnv *jvmti, JNIEnv *jni,
        jclass class_being_redefined,
        jobject loader,
        const char *name,
        jobject protection_domain,
        jint class_data_len,
        const unsigned char *class_data,
        jint *new_class_data_len,
        unsigned char **new_class_data) {

    /* 抽取模式：复制原始字节码到临时缓冲区，不修改类 */
    if (g_extract_mode) {
        g_extract_buf = (jbyte *)malloc(class_data_len);
        if (g_extract_buf != NULL) {
            memcpy(g_extract_buf, class_data, class_data_len);
            g_extract_len = class_data_len;
        }
        return; /* 不设置 new_class_data，类保持原样 */
    }

    if (g_callback_mid == NULL || name == NULL) return;

    jstring jname = (*jni)->NewStringUTF(jni, name);
    if (jname == NULL) return;

    jbyteArray jbytes = (*jni)->NewByteArray(jni, class_data_len);
    if (jbytes == NULL) { (*jni)->DeleteLocalRef(jni, jname); return; }
    (*jni)->SetByteArrayRegion(jni, jbytes, 0, class_data_len, (const jbyte *)class_data);

    jbyteArray result = (jbyteArray)(*jni)->CallStaticObjectMethod(
            jni, g_backend_class, g_callback_mid, loader, jname, jbytes);

    if ((*jni)->ExceptionCheck(jni)) {
        (*jni)->ExceptionClear(jni);
        (*jni)->DeleteLocalRef(jni, jname);
        (*jni)->DeleteLocalRef(jni, jbytes);
        return;
    }

    if (result != NULL) {
        jint len = (*jni)->GetArrayLength(jni, result);
        unsigned char *buf = NULL;
        (*jvmti)->Allocate(jvmti, len, &buf);
        if (buf != NULL) {
            (*jni)->GetByteArrayRegion(jni, result, 0, len, (jbyte *)buf);
            *new_class_data     = buf;
            *new_class_data_len = len;
        }
        (*jni)->DeleteLocalRef(jni, result);
    }

    (*jni)->DeleteLocalRef(jni, jname);
    (*jni)->DeleteLocalRef(jni, jbytes);
}

/* ================================================================== */
/*  Obtain jvmtiEnv                                                    */
/* ================================================================== */

static int ensureJvmti(JNIEnv *jni) {
    if (g_jvmti != NULL) return 1;

    if (g_jvm == NULL) {
        if ((*jni)->GetJavaVM(jni, &g_jvm) != JNI_OK || g_jvm == NULL) return 0;
    }

    if ((*g_jvm)->GetEnv(g_jvm, (void **)&g_jvmti, JVMTI_VERSION_1_2) != JNI_OK
            || g_jvmti == NULL) {
        return 0;
    }

    jvmtiCapabilities caps;
    memset(&caps, 0, sizeof(caps));
    caps.can_retransform_classes           = 1;
    caps.can_generate_all_class_hook_events = 1;
    if ((*g_jvmti)->AddCapabilities(g_jvmti, &caps) != JVMTI_ERROR_NONE) {
        g_jvmti = NULL;
        return 0;
    }

    jvmtiEventCallbacks cb;
    memset(&cb, 0, sizeof(cb));
    cb.ClassFileLoadHook = &classFileLoadHook;
    (*g_jvmti)->SetEventCallbacks(g_jvmti, &cb, sizeof(cb));
    (*g_jvmti)->SetEventNotificationMode(
            g_jvmti, JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, NULL);

    /* 创建 raw monitor 用于缓存及 extract 模式的互斥保护 */
    if (g_monitor == NULL) {
        (*g_jvmti)->CreateRawMonitor(g_jvmti, "incision-cache", &g_monitor);
    }

    return 1;
}

/* ================================================================== */
/*  Native method implementations (registered dynamically)             */
/* ================================================================== */

static jboolean JNICALL nInit(JNIEnv *jni, jclass self, jclass backendClass) {
    if (!ensureJvmti(jni)) return JNI_FALSE;

    g_backend_class = (jclass)(*jni)->NewGlobalRef(jni, backendClass);
    if (g_backend_class == NULL) return JNI_FALSE;

    g_callback_mid = (*jni)->GetStaticMethodID(jni, g_backend_class,
            "onClassFileLoad",
            "(Ljava/lang/ClassLoader;Ljava/lang/String;[B)[B");
    if (g_callback_mid == NULL) {
        (*jni)->DeleteGlobalRef(jni, g_backend_class);
        g_backend_class = NULL;
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

static jboolean JNICALL nRetransform(JNIEnv *jni, jclass self, jclass target) {
    if (g_jvmti == NULL) return JNI_FALSE;
    jvmtiError err = (*g_jvmti)->RetransformClasses(g_jvmti, 1, &target);
    if (err != JVMTI_ERROR_NONE) {
        /* 输出 JVMTI 错误码，便于诊断静默失败 */
        fprintf(stderr, "[Incision][JVMTI] RetransformClasses 失败: error=%d\n", (int)err);
        fflush(stderr);
    }
    return err == JVMTI_ERROR_NONE ? JNI_TRUE : JNI_FALSE;
}

static jboolean JNICALL nAvailable(JNIEnv *jni, jclass self) {
    return g_jvmti != NULL ? JNI_TRUE : JNI_FALSE;
}

static jclass JNICALL nDefineClass(JNIEnv *jni, jclass self, jobject loader, jstring name, jbyteArray bytes) {
    const char *nameStr = (*jni)->GetStringUTFChars(jni, name, NULL);
    if (nameStr == NULL) return NULL;

    jsize len = (*jni)->GetArrayLength(jni, bytes);
    jbyte *buf = (*jni)->GetByteArrayElements(jni, bytes, NULL);
    if (buf == NULL) {
        (*jni)->ReleaseStringUTFChars(jni, name, nameStr);
        return NULL;
    }

    jclass result = (*jni)->DefineClass(jni, nameStr, loader, buf, len);

    (*jni)->ReleaseByteArrayElements(jni, bytes, buf, JNI_ABORT);
    (*jni)->ReleaseStringUTFChars(jni, name, nameStr);

    if ((*jni)->ExceptionCheck(jni)) {
        (*jni)->ExceptionClear(jni);
        return NULL;
    }

    return result;
}

static void JNICALL nDispose(JNIEnv *jni, jclass self) {
    if (g_jvmti != NULL) {
        (*g_jvmti)->SetEventNotificationMode(
                g_jvmti, JVMTI_DISABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, NULL);
    }
    if (g_backend_class != NULL) {
        (*jni)->DeleteGlobalRef(jni, g_backend_class);
        g_backend_class = NULL;
    }
    g_callback_mid = NULL;

    /* 释放所有缓存字节码 */
    if (g_jvmti != NULL && g_monitor != NULL) {
        (*g_jvmti)->RawMonitorEnter(g_jvmti, g_monitor);
        cache_clear();
        (*g_jvmti)->RawMonitorExit(g_jvmti, g_monitor);
    } else {
        cache_clear();
    }
}

/* ================================================================== */
/*  字节码缓存相关 native 方法                                          */
/* ================================================================== */

/* 缓存指定 owner 的原始字节码 */
static jboolean JNICALL native_cache_original(JNIEnv *env, jclass clz, jstring name, jbyteArray bytes) {
    if (g_jvmti == NULL || g_monitor == NULL) return JNI_FALSE;
    const char *cname = (*env)->GetStringUTFChars(env, name, NULL);
    if (cname == NULL) return JNI_FALSE;

    jint len = (*env)->GetArrayLength(env, bytes);
    jbyte *buf = (jbyte *)malloc(len);
    if (buf == NULL) {
        (*env)->ReleaseStringUTFChars(env, name, cname);
        return JNI_FALSE;
    }
    (*env)->GetByteArrayRegion(env, bytes, 0, len, buf);

    (*g_jvmti)->RawMonitorEnter(g_jvmti, g_monitor);
    int ok = cache_put(cname, buf, len);
    (*g_jvmti)->RawMonitorExit(g_jvmti, g_monitor);

    (*env)->ReleaseStringUTFChars(env, name, cname);
    if (!ok) free(buf); /* cache_put 失败（哈希表满），释放刚分配的缓冲区 */
    return ok ? JNI_TRUE : JNI_FALSE;
}

/* 读取指定 owner 的缓存字节码，未命中返回 null */
static jbyteArray JNICALL native_get_cached(JNIEnv *env, jclass clz, jstring name) {
    if (g_jvmti == NULL || g_monitor == NULL) return NULL;
    const char *cname = (*env)->GetStringUTFChars(env, name, NULL);
    if (cname == NULL) return NULL;

    (*g_jvmti)->RawMonitorEnter(g_jvmti, g_monitor);
    int idx = cache_find(cname);
    jbyteArray result = NULL;
    if (idx >= 0) {
        result = (*env)->NewByteArray(env, g_cache[idx].len);
        if (result != NULL) {
            (*env)->SetByteArrayRegion(env, result, 0, g_cache[idx].len, g_cache[idx].bytes);
        }
    }
    (*g_jvmti)->RawMonitorExit(g_jvmti, g_monitor);

    (*env)->ReleaseStringUTFChars(env, name, cname);
    return result;
}

/* 通过 dummy retransform 抽取目标类当前的字节码 */
static jbyteArray JNICALL native_extract_bytes(JNIEnv *env, jclass clz, jobject target) {
    if (g_jvmti == NULL || g_monitor == NULL) return NULL;

    jclass targetClass = (jclass)target;

    (*g_jvmti)->RawMonitorEnter(g_jvmti, g_monitor);
    /* 进入抽取模式；ClassFileLoadHook 将仅复制 bytes，不触发 Kotlin 回调 */
    g_extract_mode = 1;
    g_extract_buf = NULL;
    g_extract_len = 0;

    jvmtiError err = (*g_jvmti)->RetransformClasses(g_jvmti, 1, &targetClass);

    g_extract_mode = 0;

    jbyteArray result = NULL;
    if (err == JVMTI_ERROR_NONE && g_extract_buf != NULL) {
        result = (*env)->NewByteArray(env, g_extract_len);
        if (result != NULL) {
            (*env)->SetByteArrayRegion(env, result, 0, g_extract_len, g_extract_buf);
        }
    }

    if (g_extract_buf != NULL) { free(g_extract_buf); g_extract_buf = NULL; }
    g_extract_len = 0;
    (*g_jvmti)->RawMonitorExit(g_jvmti, g_monitor);

    return result;
}

/* 从缓存中移除指定 owner 的条目 */
static void JNICALL native_purge_cache(JNIEnv *env, jclass clz, jstring name) {
    if (g_jvmti == NULL || g_monitor == NULL) return;
    const char *cname = (*env)->GetStringUTFChars(env, name, NULL);
    if (cname == NULL) return;
    (*g_jvmti)->RawMonitorEnter(g_jvmti, g_monitor);
    cache_remove(cname);
    (*g_jvmti)->RawMonitorExit(g_jvmti, g_monitor);
    (*env)->ReleaseStringUTFChars(env, name, cname);
}

/* ================================================================== */
/*  通用字段/方法访问器 — 绕过 Java 访问控制                              */
/*                                                                     */
/*  JNI 的 GetFieldID / GetMethodID / Get*Field / Set*Field /          */
/*  Call*Method 不受 private / protected / 模块边界限制。                */
/*  此组 API 为 BodiesClassGenerator 和其他运行时组件提供通用底层访问。   */
/* ================================================================== */

/**
 * 根据 fieldDesc 的首字符判断基本类型，装箱后返回 jobject。
 * 非基本类型直接返回 GetObjectField 结果。
 */
static jobject JNICALL native_field_get(JNIEnv *env, jclass clz,
        jobject obj, jclass ownerClass, jstring fieldName, jstring fieldDesc) {
    const char *fname = (*env)->GetStringUTFChars(env, fieldName, NULL);
    const char *fdesc = (*env)->GetStringUTFChars(env, fieldDesc, NULL);
    if (fname == NULL || fdesc == NULL) {
        if (fname) (*env)->ReleaseStringUTFChars(env, fieldName, fname);
        if (fdesc) (*env)->ReleaseStringUTFChars(env, fieldDesc, fdesc);
        return NULL;
    }

    jfieldID fid = (*env)->GetFieldID(env, ownerClass, fname, fdesc);
    if (fid == NULL) {
        (*env)->ExceptionClear(env);
        (*env)->ReleaseStringUTFChars(env, fieldName, fname);
        (*env)->ReleaseStringUTFChars(env, fieldDesc, fdesc);
        return NULL;
    }

    jobject result = NULL;
    char type = fdesc[0];

    /* 根据描述符首字符分发到对应的 JNI Get*Field 并装箱 */
    switch (type) {
        case 'Z': { /* boolean */
            jboolean v = (*env)->GetBooleanField(env, obj, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Boolean");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(Z)Ljava/lang/Boolean;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        case 'B': { /* byte */
            jbyte v = (*env)->GetByteField(env, obj, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Byte");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(B)Ljava/lang/Byte;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        case 'C': { /* char */
            jchar v = (*env)->GetCharField(env, obj, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Character");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(C)Ljava/lang/Character;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        case 'S': { /* short */
            jshort v = (*env)->GetShortField(env, obj, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Short");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(S)Ljava/lang/Short;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        case 'I': { /* int */
            jint v = (*env)->GetIntField(env, obj, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Integer");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(I)Ljava/lang/Integer;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        case 'J': { /* long */
            jlong v = (*env)->GetLongField(env, obj, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Long");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(J)Ljava/lang/Long;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        case 'F': { /* float */
            jfloat v = (*env)->GetFloatField(env, obj, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Float");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(F)Ljava/lang/Float;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        case 'D': { /* double */
            jdouble v = (*env)->GetDoubleField(env, obj, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Double");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(D)Ljava/lang/Double;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        default: { /* 对象或数组类型（L... 或 [... ） */
            result = (*env)->GetObjectField(env, obj, fid);
            break;
        }
    }

    (*env)->ReleaseStringUTFChars(env, fieldName, fname);
    (*env)->ReleaseStringUTFChars(env, fieldDesc, fdesc);
    return result;
}

/**
 * 设置实例字段值。value 为装箱后的 Object，根据 fieldDesc 拆箱写入。
 */
static void JNICALL native_field_set(JNIEnv *env, jclass clz,
        jobject obj, jclass ownerClass, jstring fieldName, jstring fieldDesc, jobject value) {
    const char *fname = (*env)->GetStringUTFChars(env, fieldName, NULL);
    const char *fdesc = (*env)->GetStringUTFChars(env, fieldDesc, NULL);
    if (fname == NULL || fdesc == NULL) {
        if (fname) (*env)->ReleaseStringUTFChars(env, fieldName, fname);
        if (fdesc) (*env)->ReleaseStringUTFChars(env, fieldDesc, fdesc);
        return;
    }

    jfieldID fid = (*env)->GetFieldID(env, ownerClass, fname, fdesc);
    if (fid == NULL) {
        (*env)->ExceptionClear(env);
        (*env)->ReleaseStringUTFChars(env, fieldName, fname);
        (*env)->ReleaseStringUTFChars(env, fieldDesc, fdesc);
        return;
    }

    char type = fdesc[0];

    /* 根据描述符首字符拆箱并调用对应的 Set*Field */
    switch (type) {
        case 'Z': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Boolean");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "booleanValue", "()Z");
            jboolean v = (*env)->CallBooleanMethod(env, value, unbox);
            (*env)->SetBooleanField(env, obj, fid, v);
            break;
        }
        case 'B': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Byte");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "byteValue", "()B");
            jbyte v = (*env)->CallByteMethod(env, value, unbox);
            (*env)->SetByteField(env, obj, fid, v);
            break;
        }
        case 'C': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Character");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "charValue", "()C");
            jchar v = (*env)->CallCharMethod(env, value, unbox);
            (*env)->SetCharField(env, obj, fid, v);
            break;
        }
        case 'S': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Short");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "shortValue", "()S");
            jshort v = (*env)->CallShortMethod(env, value, unbox);
            (*env)->SetShortField(env, obj, fid, v);
            break;
        }
        case 'I': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Integer");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "intValue", "()I");
            jint v = (*env)->CallIntMethod(env, value, unbox);
            (*env)->SetIntField(env, obj, fid, v);
            break;
        }
        case 'J': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Long");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "longValue", "()J");
            jlong v = (*env)->CallLongMethod(env, value, unbox);
            (*env)->SetLongField(env, obj, fid, v);
            break;
        }
        case 'F': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Float");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "floatValue", "()F");
            jfloat v = (*env)->CallFloatMethod(env, value, unbox);
            (*env)->SetFloatField(env, obj, fid, v);
            break;
        }
        case 'D': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Double");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "doubleValue", "()D");
            jdouble v = (*env)->CallDoubleMethod(env, value, unbox);
            (*env)->SetDoubleField(env, obj, fid, v);
            break;
        }
        default: {
            (*env)->SetObjectField(env, obj, fid, value);
            break;
        }
    }

    (*env)->ReleaseStringUTFChars(env, fieldName, fname);
    (*env)->ReleaseStringUTFChars(env, fieldDesc, fdesc);
}

/**
 * 读取静态字段值并装箱返回。
 */
static jobject JNICALL native_static_field_get(JNIEnv *env, jclass clz,
        jclass ownerClass, jstring fieldName, jstring fieldDesc) {
    const char *fname = (*env)->GetStringUTFChars(env, fieldName, NULL);
    const char *fdesc = (*env)->GetStringUTFChars(env, fieldDesc, NULL);
    if (fname == NULL || fdesc == NULL) {
        if (fname) (*env)->ReleaseStringUTFChars(env, fieldName, fname);
        if (fdesc) (*env)->ReleaseStringUTFChars(env, fieldDesc, fdesc);
        return NULL;
    }

    jfieldID fid = (*env)->GetStaticFieldID(env, ownerClass, fname, fdesc);
    if (fid == NULL) {
        (*env)->ExceptionClear(env);
        (*env)->ReleaseStringUTFChars(env, fieldName, fname);
        (*env)->ReleaseStringUTFChars(env, fieldDesc, fdesc);
        return NULL;
    }

    jobject result = NULL;
    char type = fdesc[0];

    switch (type) {
        case 'Z': {
            jboolean v = (*env)->GetStaticBooleanField(env, ownerClass, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Boolean");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(Z)Ljava/lang/Boolean;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        case 'B': {
            jbyte v = (*env)->GetStaticByteField(env, ownerClass, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Byte");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(B)Ljava/lang/Byte;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        case 'C': {
            jchar v = (*env)->GetStaticCharField(env, ownerClass, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Character");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(C)Ljava/lang/Character;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        case 'S': {
            jshort v = (*env)->GetStaticShortField(env, ownerClass, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Short");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(S)Ljava/lang/Short;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        case 'I': {
            jint v = (*env)->GetStaticIntField(env, ownerClass, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Integer");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(I)Ljava/lang/Integer;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        case 'J': {
            jlong v = (*env)->GetStaticLongField(env, ownerClass, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Long");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(J)Ljava/lang/Long;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        case 'F': {
            jfloat v = (*env)->GetStaticFloatField(env, ownerClass, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Float");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(F)Ljava/lang/Float;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        case 'D': {
            jdouble v = (*env)->GetStaticDoubleField(env, ownerClass, fid);
            jclass boxCls = (*env)->FindClass(env, "java/lang/Double");
            jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(D)Ljava/lang/Double;");
            result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
            break;
        }
        default: {
            result = (*env)->GetStaticObjectField(env, ownerClass, fid);
            break;
        }
    }

    (*env)->ReleaseStringUTFChars(env, fieldName, fname);
    (*env)->ReleaseStringUTFChars(env, fieldDesc, fdesc);
    return result;
}

/**
 * 设置静态字段值。value 为装箱 Object，根据 fieldDesc 拆箱写入。
 */
static void JNICALL native_static_field_set(JNIEnv *env, jclass clz,
        jclass ownerClass, jstring fieldName, jstring fieldDesc, jobject value) {
    const char *fname = (*env)->GetStringUTFChars(env, fieldName, NULL);
    const char *fdesc = (*env)->GetStringUTFChars(env, fieldDesc, NULL);
    if (fname == NULL || fdesc == NULL) {
        if (fname) (*env)->ReleaseStringUTFChars(env, fieldName, fname);
        if (fdesc) (*env)->ReleaseStringUTFChars(env, fieldDesc, fdesc);
        return;
    }

    jfieldID fid = (*env)->GetStaticFieldID(env, ownerClass, fname, fdesc);
    if (fid == NULL) {
        (*env)->ExceptionClear(env);
        (*env)->ReleaseStringUTFChars(env, fieldName, fname);
        (*env)->ReleaseStringUTFChars(env, fieldDesc, fdesc);
        return;
    }

    char type = fdesc[0];

    switch (type) {
        case 'Z': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Boolean");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "booleanValue", "()Z");
            jboolean v = (*env)->CallBooleanMethod(env, value, unbox);
            (*env)->SetStaticBooleanField(env, ownerClass, fid, v);
            break;
        }
        case 'B': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Byte");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "byteValue", "()B");
            jbyte v = (*env)->CallByteMethod(env, value, unbox);
            (*env)->SetStaticByteField(env, ownerClass, fid, v);
            break;
        }
        case 'C': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Character");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "charValue", "()C");
            jchar v = (*env)->CallCharMethod(env, value, unbox);
            (*env)->SetStaticCharField(env, ownerClass, fid, v);
            break;
        }
        case 'S': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Short");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "shortValue", "()S");
            jshort v = (*env)->CallShortMethod(env, value, unbox);
            (*env)->SetStaticShortField(env, ownerClass, fid, v);
            break;
        }
        case 'I': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Integer");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "intValue", "()I");
            jint v = (*env)->CallIntMethod(env, value, unbox);
            (*env)->SetStaticIntField(env, ownerClass, fid, v);
            break;
        }
        case 'J': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Long");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "longValue", "()J");
            jlong v = (*env)->CallLongMethod(env, value, unbox);
            (*env)->SetStaticLongField(env, ownerClass, fid, v);
            break;
        }
        case 'F': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Float");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "floatValue", "()F");
            jfloat v = (*env)->CallFloatMethod(env, value, unbox);
            (*env)->SetStaticFloatField(env, ownerClass, fid, v);
            break;
        }
        case 'D': {
            jclass boxCls = (*env)->FindClass(env, "java/lang/Double");
            jmethodID unbox = (*env)->GetMethodID(env, boxCls, "doubleValue", "()D");
            jdouble v = (*env)->CallDoubleMethod(env, value, unbox);
            (*env)->SetStaticDoubleField(env, ownerClass, fid, v);
            break;
        }
        default: {
            (*env)->SetStaticObjectField(env, ownerClass, fid, value);
            break;
        }
    }

    (*env)->ReleaseStringUTFChars(env, fieldName, fname);
    (*env)->ReleaseStringUTFChars(env, fieldDesc, fdesc);
}

/**
 * 通用方法调用器 — 绕过 Java 访问控制调用任意实例/静态方法。
 *
 * 签名：nInvokeMethod(Object obj, Class ownerClass, String methodName,
 *                      String methodDesc, Object[] args) → Object
 *
 * - obj 为 null 时按静态方法调用
 * - 返回值自动装箱；void 方法返回 null
 * - args 中基本类型已装箱，由本函数拆箱后传入 JNI Call*Method
 *
 * 当前实现使用 jvalue 数组 + CallObjectMethodA 简化变长参数处理。
 * 基本类型返回值通过对应的 JNI Call*Method 获取后装箱。
 */
static jobject JNICALL native_invoke_method(JNIEnv *env, jclass clz,
        jobject obj, jclass ownerClass, jstring methodName, jstring methodDesc, jobjectArray args) {
    const char *mname = (*env)->GetStringUTFChars(env, methodName, NULL);
    const char *mdesc = (*env)->GetStringUTFChars(env, methodDesc, NULL);
    if (mname == NULL || mdesc == NULL) {
        if (mname) (*env)->ReleaseStringUTFChars(env, methodName, mname);
        if (mdesc) (*env)->ReleaseStringUTFChars(env, methodDesc, mdesc);
        return NULL;
    }

    int isStatic = (obj == NULL);
    jmethodID mid;
    if (isStatic) {
        mid = (*env)->GetStaticMethodID(env, ownerClass, mname, mdesc);
    } else {
        mid = (*env)->GetMethodID(env, ownerClass, mname, mdesc);
    }
    if (mid == NULL) {
        (*env)->ExceptionClear(env);
        (*env)->ReleaseStringUTFChars(env, methodName, mname);
        (*env)->ReleaseStringUTFChars(env, methodDesc, mdesc);
        return NULL;
    }

    /* 解析参数描述符，构造 jvalue 数组 */
    int argCount = args != NULL ? (*env)->GetArrayLength(env, args) : 0;
    jvalue *jargs = NULL;
    if (argCount > 0) {
        jargs = (jvalue *)calloc(argCount, sizeof(jvalue));
        if (jargs == NULL) {
            (*env)->ReleaseStringUTFChars(env, methodName, mname);
            (*env)->ReleaseStringUTFChars(env, methodDesc, mdesc);
            return NULL;
        }

        /* 遍历描述符中的参数类型，逐个拆箱 */
        const char *p = mdesc + 1; /* 跳过 '(' */
        for (int i = 0; i < argCount && *p != ')'; i++) {
            jobject arg = (*env)->GetObjectArrayElement(env, args, i);
            switch (*p) {
                case 'Z': {
                    jclass boxCls = (*env)->FindClass(env, "java/lang/Boolean");
                    jmethodID unbox = (*env)->GetMethodID(env, boxCls, "booleanValue", "()Z");
                    jargs[i].z = (*env)->CallBooleanMethod(env, arg, unbox);
                    p++;
                    break;
                }
                case 'B': {
                    jclass boxCls = (*env)->FindClass(env, "java/lang/Byte");
                    jmethodID unbox = (*env)->GetMethodID(env, boxCls, "byteValue", "()B");
                    jargs[i].b = (*env)->CallByteMethod(env, arg, unbox);
                    p++;
                    break;
                }
                case 'C': {
                    jclass boxCls = (*env)->FindClass(env, "java/lang/Character");
                    jmethodID unbox = (*env)->GetMethodID(env, boxCls, "charValue", "()C");
                    jargs[i].c = (*env)->CallCharMethod(env, arg, unbox);
                    p++;
                    break;
                }
                case 'S': {
                    jclass boxCls = (*env)->FindClass(env, "java/lang/Short");
                    jmethodID unbox = (*env)->GetMethodID(env, boxCls, "shortValue", "()S");
                    jargs[i].s = (*env)->CallShortMethod(env, arg, unbox);
                    p++;
                    break;
                }
                case 'I': {
                    jclass boxCls = (*env)->FindClass(env, "java/lang/Integer");
                    jmethodID unbox = (*env)->GetMethodID(env, boxCls, "intValue", "()I");
                    jargs[i].i = (*env)->CallIntMethod(env, arg, unbox);
                    p++;
                    break;
                }
                case 'J': {
                    jclass boxCls = (*env)->FindClass(env, "java/lang/Long");
                    jmethodID unbox = (*env)->GetMethodID(env, boxCls, "longValue", "()J");
                    jargs[i].j = (*env)->CallLongMethod(env, arg, unbox);
                    p++;
                    break;
                }
                case 'F': {
                    jclass boxCls = (*env)->FindClass(env, "java/lang/Float");
                    jmethodID unbox = (*env)->GetMethodID(env, boxCls, "floatValue", "()F");
                    jargs[i].f = (*env)->CallFloatMethod(env, arg, unbox);
                    p++;
                    break;
                }
                case 'D': {
                    jclass boxCls = (*env)->FindClass(env, "java/lang/Double");
                    jmethodID unbox = (*env)->GetMethodID(env, boxCls, "doubleValue", "()D");
                    jargs[i].d = (*env)->CallDoubleMethod(env, arg, unbox);
                    p++;
                    break;
                }
                case 'L': {
                    jargs[i].l = arg;
                    /* 跳过 'L...;' */
                    while (*p && *p != ';') p++;
                    if (*p == ';') p++;
                    break;
                }
                case '[': {
                    jargs[i].l = arg;
                    /* 跳过数组维度前缀 */
                    while (*p == '[') p++;
                    if (*p == 'L') {
                        while (*p && *p != ';') p++;
                        if (*p == ';') p++;
                    } else {
                        p++; /* 基本类型数组 */
                    }
                    break;
                }
                default:
                    jargs[i].l = arg;
                    p++;
                    break;
            }
        }
    }

    /* 解析返回类型 */
    const char *retStart = strchr(mdesc, ')');
    char retType = retStart ? *(retStart + 1) : 'V';

    jobject result = NULL;

    /* 根据返回类型分发调用 */
    if (isStatic) {
        switch (retType) {
            case 'V':
                (*env)->CallStaticVoidMethodA(env, ownerClass, mid, jargs);
                break;
            case 'Z': {
                jboolean v = (*env)->CallStaticBooleanMethodA(env, ownerClass, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Boolean");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(Z)Ljava/lang/Boolean;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            case 'B': {
                jbyte v = (*env)->CallStaticByteMethodA(env, ownerClass, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Byte");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(B)Ljava/lang/Byte;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            case 'C': {
                jchar v = (*env)->CallStaticCharMethodA(env, ownerClass, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Character");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(C)Ljava/lang/Character;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            case 'S': {
                jshort v = (*env)->CallStaticShortMethodA(env, ownerClass, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Short");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(S)Ljava/lang/Short;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            case 'I': {
                jint v = (*env)->CallStaticIntMethodA(env, ownerClass, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Integer");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(I)Ljava/lang/Integer;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            case 'J': {
                jlong v = (*env)->CallStaticLongMethodA(env, ownerClass, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Long");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(J)Ljava/lang/Long;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            case 'F': {
                jfloat v = (*env)->CallStaticFloatMethodA(env, ownerClass, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Float");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(F)Ljava/lang/Float;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            case 'D': {
                jdouble v = (*env)->CallStaticDoubleMethodA(env, ownerClass, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Double");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(D)Ljava/lang/Double;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            default: /* 引用类型 */
                result = (*env)->CallStaticObjectMethodA(env, ownerClass, mid, jargs);
                break;
        }
    } else {
        switch (retType) {
            case 'V':
                (*env)->CallVoidMethodA(env, obj, mid, jargs);
                break;
            case 'Z': {
                jboolean v = (*env)->CallBooleanMethodA(env, obj, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Boolean");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(Z)Ljava/lang/Boolean;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            case 'B': {
                jbyte v = (*env)->CallByteMethodA(env, obj, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Byte");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(B)Ljava/lang/Byte;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            case 'C': {
                jchar v = (*env)->CallCharMethodA(env, obj, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Character");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(C)Ljava/lang/Character;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            case 'S': {
                jshort v = (*env)->CallShortMethodA(env, obj, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Short");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(S)Ljava/lang/Short;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            case 'I': {
                jint v = (*env)->CallIntMethodA(env, obj, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Integer");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(I)Ljava/lang/Integer;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            case 'J': {
                jlong v = (*env)->CallLongMethodA(env, obj, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Long");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(J)Ljava/lang/Long;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            case 'F': {
                jfloat v = (*env)->CallFloatMethodA(env, obj, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Float");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(F)Ljava/lang/Float;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            case 'D': {
                jdouble v = (*env)->CallDoubleMethodA(env, obj, mid, jargs);
                jclass boxCls = (*env)->FindClass(env, "java/lang/Double");
                jmethodID valueOf = (*env)->GetStaticMethodID(env, boxCls, "valueOf", "(D)Ljava/lang/Double;");
                result = (*env)->CallStaticObjectMethod(env, boxCls, valueOf, v);
                break;
            }
            default:
                result = (*env)->CallObjectMethodA(env, obj, mid, jargs);
                break;
        }
    }

    if (jargs != NULL) free(jargs);
    (*env)->ReleaseStringUTFChars(env, methodName, mname);
    (*env)->ReleaseStringUTFChars(env, methodDesc, mdesc);

    /* 如果方法执行中抛出异常，清除并返回 null */
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionClear(env);
        return NULL;
    }

    return result;
}

/* ================================================================== */
/*  JNI_OnLoad — dynamic registration via system property              */
/*                                                                     */
/*  The Kotlin side sets "incision.jvmti.class" to the fully-qualified */
/*  (possibly relocated) class name BEFORE calling System.load().      */
/*  We read it here, find the class, and RegisterNatives all methods.  */
/* ================================================================== */

static JNINativeMethod g_methods[] = {
    { "nInit",        "(Ljava/lang/Class;)Z",  (void *)nInit        },
    { "nRetransform", "(Ljava/lang/Class;)Z",  (void *)nRetransform },
    { "nAvailable",   "()Z",                   (void *)nAvailable   },
    { "nDispose",     "()V",                   (void *)nDispose     },
    { "nDefineClass", "(Ljava/lang/ClassLoader;Ljava/lang/String;[B)Ljava/lang/Class;", (void *)nDefineClass },
    /* 字节码缓存与抽取相关 */
    { "nCacheOriginal",     "(Ljava/lang/String;[B)Z",  (void *)native_cache_original },
    { "nGetCachedOriginal", "(Ljava/lang/String;)[B",   (void *)native_get_cached     },
    { "nExtractClassBytes", "(Ljava/lang/Class;)[B",    (void *)native_extract_bytes  },
    { "nPurgeCache",        "(Ljava/lang/String;)V",    (void *)native_purge_cache    },
    /* 通用字段/方法访问器 — 绕过访问控制 */
    { "nFieldGet",       "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;",                              (void *)native_field_get        },
    { "nFieldSet",       "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V",                             (void *)native_field_set        },
    { "nStaticFieldGet", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;",                                                (void *)native_static_field_get },
    { "nStaticFieldSet", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V",                                               (void *)native_static_field_set },
    { "nInvokeMethod",   "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;",           (void *)native_invoke_method    },
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    g_jvm = vm;

    JNIEnv *jni = NULL;
    if ((*vm)->GetEnv(vm, (void **)&jni, JNI_VERSION_1_6) != JNI_OK || jni == NULL) {
        return JNI_VERSION_1_6;
    }

    /* Read system property "incision.jvmti.class" */
    jclass sysCls = (*jni)->FindClass(jni, "java/lang/System");
    if (sysCls == NULL) return JNI_VERSION_1_6;

    jmethodID getProp = (*jni)->GetStaticMethodID(jni, sysCls,
            "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
    if (getProp == NULL) return JNI_VERSION_1_6;

    jstring key = (*jni)->NewStringUTF(jni, "incision.jvmti.class");
    if (key == NULL) return JNI_VERSION_1_6;

    jstring val = (jstring)(*jni)->CallStaticObjectMethod(jni, sysCls, getProp, key);
    (*jni)->DeleteLocalRef(jni, key);

    if (val == NULL || (*jni)->ExceptionCheck(jni)) {
        if ((*jni)->ExceptionCheck(jni)) (*jni)->ExceptionClear(jni);
        return JNI_VERSION_1_6;
    }

    const char *className = (*jni)->GetStringUTFChars(jni, val, NULL);
    if (className == NULL) return JNI_VERSION_1_6;

    /* Convert dots to slashes for JNI FindClass */
    size_t len = strlen(className);
    char *jniName = (char *)malloc(len + 1);
    if (jniName == NULL) {
        (*jni)->ReleaseStringUTFChars(jni, val, className);
        return JNI_VERSION_1_6;
    }
    for (size_t i = 0; i < len; i++) {
        jniName[i] = (className[i] == '.') ? '/' : className[i];
    }
    jniName[len] = '\0';

    (*jni)->ReleaseStringUTFChars(jni, val, className);

    jclass target = (*jni)->FindClass(jni, jniName);
    free(jniName);

    if (target == NULL) {
        if ((*jni)->ExceptionCheck(jni)) (*jni)->ExceptionClear(jni);
        return JNI_VERSION_1_6;
    }

    (*jni)->RegisterNatives(jni, target, g_methods,
            sizeof(g_methods) / sizeof(g_methods[0]));

    if ((*jni)->ExceptionCheck(jni)) {
        (*jni)->ExceptionClear(jni);
    }

    (*jni)->DeleteLocalRef(jni, target);
    (*jni)->DeleteLocalRef(jni, val);
    (*jni)->DeleteLocalRef(jni, sysCls);

    return JNI_VERSION_1_6;
}
