#ifndef _JAVASOFT_JNI_MD_H_
#define _JAVASOFT_JNI_MD_H_

#ifndef JNIEXPORT
  #define JNIEXPORT __attribute__((visibility("default")))
#endif
#define JNIIMPORT
#define JNICALL

typedef int jint;
typedef long long jlong;
typedef signed char jbyte;

#endif
