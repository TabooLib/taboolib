package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.EnumGetMethod;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import org.tabooproject.reflex.ClassMethod;
import org.tabooproject.reflex.Reflex;
import org.tabooproject.reflex.ReflexClass;
import org.tabooproject.reflex.UnsafeAccess;
import taboolib.module.configuration.*;

import java.lang.reflect.Type;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * Converts Java objects to configs and vice-versa.
 *
 * @author TheElectronWill
 */
@SuppressWarnings("ALL")
public final class ObjectConverter {

    private final boolean bypassTransient, bypassFinal;
    private boolean ignoreConstructor = false;

    /**
     * Creates a new ObjectConverter with advanced parameters.
     *
     * @param bypassTransient {@code true} to use (parse or write) a field even if it's transient
     * @param bypassFinal     {@code true} to write a field even if it's final
     */
    public ObjectConverter(boolean bypassTransient, boolean bypassFinal) {
        this.bypassTransient = bypassTransient;
        this.bypassFinal = bypassFinal;
    }

    /**
     * Creates a new ObjectConverter with the default parameters. This is equivalent to {@code
     * new ObjectConverter(false, true)}.
     *
     * @see #ObjectConverter(boolean, boolean)
     */
    public ObjectConverter() {
        this(false, true);
    }

    public ObjectConverter(boolean ignoreConstructor) {
        this(false, true);
        setIgnoreConstructor(ignoreConstructor);
    }

    public ObjectConverter setIgnoreConstructor(boolean ignoreConstructor) {
        this.ignoreConstructor = ignoreConstructor;
        return this;
    }

    /**
     * Converts an Object to a Config.
     *
     * @param o           the object to convert
     * @param destination the Config where to put the values into
     */
    public void toConfig(Object o, Config destination) {
        Objects.requireNonNull(o, "The object must not be null.");
        Objects.requireNonNull(destination, "The config must not be null.");
        Class<?> clazz = o.getClass();
        List<String> annotatedPath = AnnotationUtils.getPath(clazz);
        if (annotatedPath != null) {
            destination = destination.getRaw(annotatedPath);
        }
        convertToConfig(o, clazz, destination);
    }

    public void toConfig(Class<?> clazz, Config destination) {
        Objects.requireNonNull(destination, "The config must not be null.");
        List<String> annotatedPath = AnnotationUtils.getPath(clazz);
        if (annotatedPath != null) {
            destination = destination.getRaw(annotatedPath);
        }
        convertToConfig(null, clazz, destination);
    }

    /**
     * Converts an Object to a Config.
     *
     * @param o                   the object to convert
     * @param destinationSupplier a Supplier that provides the Config where to put the values into
     * @param <C>                 the destination's type
     * @return the Config obtained from the Supplier
     */
    public <C extends Config> C toConfig(Object o, Supplier<C> destinationSupplier) {
        C destination = destinationSupplier.get();
        toConfig(o, destination);
        return destination;
    }

    public <C extends Config> C toConfig(Class<?> clazz, Supplier<C> destinationSupplier) {
        C destination = destinationSupplier.get();
        toConfig(clazz, destination);
        return destination;
    }

    /**
     * Converts a Config to an Object.
     *
     * @param config      the config to convert
     * @param destination the Object where to put the values into
     */
    public void toObject(UnmodifiableConfig config, Object destination) {
        Objects.requireNonNull(config, "The config must not be null.");
        Objects.requireNonNull(destination, "The object must not be null.");
        Class<?> clazz = destination.getClass();
        List<String> annotatedPath = AnnotationUtils.getPath(clazz);
        if (annotatedPath != null) {
            config = config.getRaw(annotatedPath);
        }
        convertToObject(config, destination, clazz);
    }

    /**
     * Converts a Config to an Object.
     *
     * @param config              the config to convert
     * @param destinationSupplier a Supplier that provides the Object where to put the values into
     * @param <O>                 the destination's type
     * @return the object obtained from the Supplier
     */
    public <O> O toObject(UnmodifiableConfig config, Supplier<O> destinationSupplier) {
        O destination = destinationSupplier.get();
        toObject(config, destination);
        return destination;
    }

    /**
     * Converts an Object to a Config. The {@link #bypassTransient} setting applies.
     */
    private void convertToConfig(Object object, Class<?> clazz, Config destination) {
        // This loop walks through the class hierarchy, see clazz = clazz.getSuperclass(); at the end
        while (clazz != Object.class) {
            // 获取内置转换器
            InnerConverter innerConverter = getInnerConverter(clazz);
            // This loop walks through the fields of the class
            for (Field field : clazz.getDeclaredFields()) {
                // --- Checks modifiers ---
                final int fieldModifiers = field.getModifiers();
                if (object == null && Modifier.isStatic(fieldModifiers)) {
                    continue;// Don't process static fields of object instances
                }
                if (!bypassTransient && Modifier.isTransient(fieldModifiers)) {
                    continue;// Don't process transient fields if configured so
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);// Enforces field access if needed
                }

                // --- Applies annotations ---
                Object value;
                try {
                    value = field.get(object);
                } catch (IllegalAccessException e) {// Unexpected: setAccessible is called if needed
                    throw new ReflectionException("Unable to parse the field " + field, e);
                }
                // Checks that the value is conform to an eventual @SpecSometing annotation
                AnnotationUtils.checkField(field, value);
                // 自定义路径
                List<String> path = AnnotationUtils.getPath(field);
                // 该字段获取到的值为 null
                if (value == null) {
                    destination.set(path, null);
                    continue;
                }

                // 内置转换器
                if (innerConverter != null) {
                    Converter<Object, Object> ic = innerConverter.getConverter(field, object);
                    ConvertResult result = (ConvertResult) ic.convertFromField(value);
                    if (result instanceof ConvertResult.Success) {
                        destination.set(path, ((ConvertResult.Success) result).getValue());
                        continue;
                    } else if (result instanceof ConvertResult.Failure) {
                        ((ConvertResult.Failure) result).getException().printStackTrace();
                        continue;
                    }
                }

                // 全局注册的转换器（优先于 @Conversion 注解）
                Converter<Object, Object> registryConverter = ConverterRegistry.INSTANCE.getConverter(field.getType());
                if (registryConverter != null) {
                    value = registryConverter.convertFromField(value);
                    // 如果 value 返回为 Map 则转换为 Config
                    if (value instanceof Map) {
                        value = ConfigSection.Companion.toNightConfig$basic_configuration(((Map<?, ?>) value), destination);
                    }
                }
                // 自定义 @Converter（如果全局转换器未处理）
                else {
                    Converter<Object, Object> converter = getConverter(field);
                    if (converter != null) {
                        value = converter.convertFromField(value);
                        // 如果 value 返回为 Map 则转换为 Config
                        if (value instanceof Map) {
                            value = ConfigSection.Companion.toNightConfig$basic_configuration(((Map<?, ?>) value), destination);
                        }
                    }
                }
                ConfigFormat<?> format = destination.configFormat();

                // --- Writes the value to the configuration ---
                if (value == null) {
                    destination.set(path, null);
                } else {
                    Class<?> valueType = value.getClass();
                    if (Enum.class.isAssignableFrom(valueType)) {
                        // Enums must not be treated as objects to break down
                        // Note: isEnum() doesn't work with enum items that have a body
                        if (destination.configFormat().supportsType(Enum.class)) {
                            destination.set(path, value); // keep the enum value if supported
                        } else {
                            destination.set(path, value.toString()); // if not supported, serialize it
                        }
                    } else if (field.isAnnotationPresent(ForceBreakdown.class) || !format.supportsType(valueType)) {
                        // We have to convert the value
                        destination.set(path, value);
                        Config converted = destination.createSubConfig();
                        convertToConfig(value, valueType, converted);
                        destination.set(path, converted);
                    } else if (value instanceof Collection) {
                        // Checks that the ConfigFormat supports the type of the collection's elements
                        Collection<?> src = (Collection<?>) value;
                        Class<?> bottomType = bottomElementType(src);
                        if (format.supportsType(bottomType)) {
                            // Everything is supported, no conversion needed
                            destination.set(path, value);
                        } else {
                            // List of complex objects => the bottom elements need conversion
                            Collection<Object> dst = new ArrayList<>(src.size());
                            convertObjectsToConfigs(src, bottomType, dst, destination);
                            destination.set(path, dst);
                        }
                    } else {
                        // Simple value
                        destination.set(path, value);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Converts a Config to an Object. The {@link #bypassTransient} and {@link #bypassFinal}
     * settings apply.
     */
    private void convertToObject(UnmodifiableConfig config, Object object, Class<?> clazz) {
        // This loop walks through the class hierarchy, see clazz = clazz.getSuperclass(); at the end
        while (clazz != Object.class) {
            // 获取内置转换器
            InnerConverter innerConverter = getInnerConverter(clazz);
            // This loop walks through the fields of the class
            for (Field field : clazz.getDeclaredFields()) {
                // --- Checks modifiers ---
                final int fieldModifiers = field.getModifiers();
                if (object == null && Modifier.isStatic(fieldModifiers)) {
                    continue;// Don't process static fields of object instances
                }
                if (bypassFinal || !Modifier.isFinal(fieldModifiers)) {
                    field.setAccessible(true);// Enforces field access if needed AND configured so
                } else {
                    continue;// Don't process final fields if configured so
                }
                if (!bypassTransient && Modifier.isTransient(fieldModifiers)) {
                    continue;// Don't process transient fields if configured so
                }

                // --- Applies annotations ---
                List<String> path = AnnotationUtils.getPath(field);
                Object value = config.get(path);
                // 配置文件中不存在该字段
                if (value == null) {
                    continue;
                }

                // 内置转换器
                if (innerConverter != null) {
                    Converter<Object, Object> ic = innerConverter.getConverter(field, new ConfigSection((Config) config, "", null));
                    ConvertResult result = (ConvertResult) ic.convertToField(value);
                    if (result instanceof ConvertResult.Success) {
                        UnsafeAccess.INSTANCE.put(object, field, ((ConvertResult.Success) result).getValue());
                        continue;
                    } else if (result instanceof ConvertResult.Failure) {
                        ((ConvertResult.Failure) result).getException().printStackTrace();
                        continue;
                    }
                }

                // 全局注册的转换器（优先于 @Conversion 注解）
                Converter<Object, Object> registryConverter = ConverterRegistry.INSTANCE.getConverter(field.getType());
                if (registryConverter != null) {
                    value = registryConverter.convertToField(ConfigSection.Companion.unwrap(value));
                }
                // 自定义 @Converter（如果全局转换器未处理）
                else {
                    Converter<Object, Object> converter = getConverter(field);
                    if (converter != null) {
                        value = converter.convertToField(ConfigSection.Companion.unwrap(value));
                    }
                }
                // 转换后的值为 null
                if (value == null) {
                    continue;
                }

                // --- Writes the value to the object's field, converting it if needed ---
                Class<?> fieldType = field.getType();
                try {
                    if ((value instanceof UnmodifiableConfig || value instanceof Map) && Map.class.isAssignableFrom(fieldType)) {
                        // --- Reads as a map while preserving the declared map and generic value types ---
                        Map<Object, Object> converted = convertMap(value, field.getGenericType(), fieldType);
                        AnnotationUtils.checkField(field, converted);
                        field.set(object, converted);
                    } else if ((value instanceof UnmodifiableConfig || value instanceof Map) && !(fieldType.isAssignableFrom(value.getClass()))) {
                        // --- Read as a sub-object ---
                        final UnmodifiableConfig cfg = value instanceof UnmodifiableConfig
                                ? (UnmodifiableConfig) value
                                : configFromMap((Map<?, ?>) value);
                        // Gets or creates the field and convert it (if null OR not preserved)
                        Object fieldValue = field.get(object);
                        if (fieldValue == null) {
                            fieldValue = createInstance(fieldType);
                            field.set(object, fieldValue);
                            convertToObject(cfg, fieldValue, field.getType());
                        } else if (!AnnotationUtils.mustPreserve(field, clazz)) {
                            convertToObject(cfg, fieldValue, field.getType());
                        }
                    } else if (value instanceof Collection && Collection.class.isAssignableFrom(fieldType)) {
                        // --- Reads as a collection while preserving the declared collection and generic element types ---
                        Collection<Object> converted = convertCollection((Collection<?>) value, field.getGenericType(), fieldType);
                        AnnotationUtils.checkField(field, converted);
                        field.set(object, converted);
                    } else {
                        // --- Read as a plain value ---
                        if (value == null && AnnotationUtils.mustPreserve(field, clazz)) {
                            AnnotationUtils.checkField(field, field.get(object));
                        } else {
                            AnnotationUtils.checkField(field, value);
                            if (field.getType().isEnum()) {
                                Class<? extends Enum> enumType = (Class<? extends Enum>) field.getType();
                                SpecEnum specEnum = field.getAnnotation(SpecEnum.class);
                                EnumGetMethod method = (specEnum == null) ? EnumGetMethod.NAME_IGNORECASE : specEnum.method();
                                field.set(object, method.get(value, enumType));
                            } else {
                                field.set(object, value);
                            }
                        }
                    }
                } catch (ReflectiveOperationException ex) {
                    throw new ReflectionException("Unable to work with field " + field, ex);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private Collection<Object> convertCollection(Collection<?> source, Type declaredType, Class<?> declaredClass) {
        Type elementType = collectionElementType(declaredType);
        Collection<Object> destination = createCollection(declaredClass, elementType, source.size());
        for (Object element : source) {
            destination.add(convertValue(element, elementType));
        }
        return destination;
    }

    private Object convertValue(Object value, Type declaredType) {
        if (value == null) {
            return null;
        }
        Class<?> declaredClass = rawClass(declaredType);
        if (declaredClass == Object.class) {
            return value;
        }
        if (value instanceof Collection && Collection.class.isAssignableFrom(declaredClass)) {
            return convertCollection((Collection<?>) value, declaredType, declaredClass);
        }
        if ((value instanceof UnmodifiableConfig || value instanceof Map) && Map.class.isAssignableFrom(declaredClass)) {
            return convertMap(value, declaredType, declaredClass);
        }
        if ((value instanceof UnmodifiableConfig || value instanceof Map) && isStructuredObjectType(declaredClass)) {
            Object elementObject = createInstance(declaredClass);
            UnmodifiableConfig elementConfig = value instanceof UnmodifiableConfig
                    ? (UnmodifiableConfig) value
                    : configFromMap((Map<?, ?>) value);
            convertToObject(elementConfig, elementObject, declaredClass);
            return elementObject;
        }
        Object unwrapped = ConfigSection.Companion.unwrap(value);
        if (unwrapped == null || declaredClass.isAssignableFrom(unwrapped.getClass())) {
            return unwrapped;
        }
        if (declaredClass.isEnum()) {
            return EnumGetMethod.NAME_IGNORECASE.get(unwrapped, (Class<? extends Enum>) declaredClass);
        }
        if (unwrapped instanceof Number) {
            Object number = convertNumber((Number) unwrapped, declaredClass);
            if (number != null) {
                return number;
            }
        }
        if (declaredClass == String.class && !(unwrapped instanceof Map) && !(unwrapped instanceof Collection)) {
            return unwrapped.toString();
        }
        throw new InvalidValueException("Unexpected element of type " + unwrapped.getClass() + " for " + declaredType);
    }

    private boolean isStructuredObjectType(Class<?> type) {
        return type != String.class
                && type != Boolean.class
                && type != Character.class
                && !Number.class.isAssignableFrom(type)
                && !type.isEnum()
                && !Collection.class.isAssignableFrom(type)
                && !Map.class.isAssignableFrom(type);
    }

    private Map<Object, Object> convertMap(Object source, Type declaredType, Class<?> declaredClass) {
        Map<?, ?> sourceMap;
        if (source instanceof UnmodifiableConfig) {
            sourceMap = ((UnmodifiableConfig) source).valueMap();
        } else {
            Object unwrapped = ConfigSection.Companion.unwrap(source);
            if (!(unwrapped instanceof Map)) {
                throw new InvalidValueException("Unexpected value of type " + source.getClass() + " for " + declaredType);
            }
            sourceMap = (Map<?, ?>) unwrapped;
        }
        Type keyType = Object.class;
        Type valueType = Object.class;
        Type resolvedType = boundedType(declaredType);
        if (resolvedType instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) resolvedType).getActualTypeArguments();
            if (typeArguments.length > 0) {
                keyType = typeArguments[0];
            }
            if (typeArguments.length > 1) {
                valueType = typeArguments[1];
            }
        }
        Map<Object, Object> destination = createMap(declaredClass);
        for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
            destination.put(convertValue(entry.getKey(), keyType), convertValue(entry.getValue(), valueType));
        }
        return destination;
    }

    private UnmodifiableConfig configFromMap(Map<?, ?> source) {
        Config config = Config.inMemory();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            config.set(String.valueOf(entry.getKey()), entry.getValue());
        }
        return config;
    }

    private Collection<Object> createCollection(Class<?> declaredClass, Type elementType, int size) {
        if (!declaredClass.isInterface() && !Modifier.isAbstract(declaredClass.getModifiers())) {
            return (Collection<Object>) createInstance((Class<? extends Collection>) declaredClass);
        }
        if (EnumSet.class.isAssignableFrom(declaredClass)) {
            Class<?> enumType = rawClass(elementType);
            if (!enumType.isEnum()) {
                throw new ReflectionException("Unable to determine enum type for " + declaredClass);
            }
            return (Collection<Object>) (Collection<?>) EnumSet.noneOf((Class<? extends Enum>) enumType);
        }
        if ((NavigableSet.class.isAssignableFrom(declaredClass) || SortedSet.class.isAssignableFrom(declaredClass))
                && declaredClass.isAssignableFrom(TreeSet.class)) {
            return new TreeSet<>();
        }
        if (Set.class.isAssignableFrom(declaredClass) && declaredClass.isAssignableFrom(LinkedHashSet.class)) {
            return new LinkedHashSet<>(Math.max(16, size));
        }
        if ((Deque.class.isAssignableFrom(declaredClass) || Queue.class.isAssignableFrom(declaredClass))
                && declaredClass.isAssignableFrom(LinkedList.class)) {
            return new LinkedList<>();
        }
        if (Collection.class.isAssignableFrom(declaredClass) && declaredClass.isAssignableFrom(ArrayList.class)) {
            return new ArrayList<>(size);
        }
        throw new ReflectionException("Unable to create compatible collection for " + declaredClass);
    }

    private Map<Object, Object> createMap(Class<?> declaredClass) {
        if (!declaredClass.isInterface() && !Modifier.isAbstract(declaredClass.getModifiers())) {
            return (Map<Object, Object>) createInstance((Class<? extends Map>) declaredClass);
        }
        if ((NavigableMap.class.isAssignableFrom(declaredClass) || SortedMap.class.isAssignableFrom(declaredClass))
                && declaredClass.isAssignableFrom(TreeMap.class)) {
            return new TreeMap<>();
        }
        if (Map.class.isAssignableFrom(declaredClass) && declaredClass.isAssignableFrom(LinkedHashMap.class)) {
            return new LinkedHashMap<>();
        }
        throw new ReflectionException("Unable to create compatible map for " + declaredClass);
    }

    private Type collectionElementType(Type declaredType) {
        Type resolvedType = boundedType(declaredType);
        if (resolvedType instanceof ParameterizedType) {
            Type[] arguments = ((ParameterizedType) resolvedType).getActualTypeArguments();
            if (arguments.length > 0) {
                return arguments[0];
            }
        }
        return Object.class;
    }

    private Type boundedType(Type type) {
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            Type[] lowerBounds = wildcardType.getLowerBounds();
            if (lowerBounds.length > 0) {
                return boundedType(lowerBounds[0]);
            }
            Type[] upperBounds = wildcardType.getUpperBounds();
            return upperBounds.length == 0 ? Object.class : boundedType(upperBounds[0]);
        }
        if (type instanceof TypeVariable) {
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            return bounds.length == 0 ? Object.class : boundedType(bounds[0]);
        }
        return type;
    }

    private Class<?> rawClass(Type type) {
        if (type instanceof Class) {
            return wrapPrimitive((Class<?>) type);
        }
        if (type instanceof ParameterizedType) {
            return rawClass(((ParameterizedType) type).getRawType());
        }
        if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            return upperBounds.length == 0 ? Object.class : rawClass(upperBounds[0]);
        }
        if (type instanceof TypeVariable) {
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            return bounds.length == 0 ? Object.class : rawClass(bounds[0]);
        }
        return Object.class;
    }

    private Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == short.class) return Short.class;
        if (type == byte.class) return Byte.class;
        if (type == boolean.class) return Boolean.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private Object convertNumber(Number value, Class<?> targetType) {
        if (targetType == Integer.class) return value.intValue();
        if (targetType == Long.class) return value.longValue();
        if (targetType == Double.class) return value.doubleValue();
        if (targetType == Float.class) return value.floatValue();
        if (targetType == Short.class) return value.shortValue();
        if (targetType == Byte.class) return value.byteValue();
        return null;
    }

    /**
     * Gets the type of the "bottom element" of a collection.
     * For instance, for a list {@code [["string"], ["another string"]]}
     * this method returns the class {@code String}.
     *
     * @param list the list object
     * @return the type of the elements of the most nested list
     */
    private Class<?> bottomElementType(Collection<?> list) {
        for (Object elem : list) {
            if (elem instanceof Collection) {
                return bottomElementType((Collection<?>) elem);
            } else if (elem != null) {
                return elem.getClass();
            }
        }
        return null;
    }

    /**
     * Converts a collection of objects of the type srcBottomType to a collection of configurations.
     *
     * @param src           the collection of objects, may be nested, source
     * @param srcBottomType the type of objects
     * @param dst           the collection of configs, destination
     * @param parentConfig  the parent configuration, used to create the new configs to put in dst
     */
    private void convertObjectsToConfigs(Collection<?> src, Class<?> srcBottomType, Collection<Object> dst, Config parentConfig) {
        for (Object elem : src) {
            if (elem == null) {
                dst.add(null);
            } else if (srcBottomType.isAssignableFrom(elem.getClass())) {
                Config elementConfig = parentConfig.createSubConfig();
                convertToConfig(elem, elem.getClass(), elementConfig);
                dst.add(elementConfig);
            } else if (elem instanceof Collection) {
                ArrayList<Object> subList = new ArrayList<>();
                convertObjectsToConfigs((Collection<?>) elem, srcBottomType, subList, parentConfig);
                subList.trimToSize();
                dst.add(subList);
            } else {
                String elemType = elem.getClass().toString();
                throw new InvalidValueException("Unexpected element of type " + elemType + " in collection of " + srcBottomType);
            }
        }
    }

    /**
     * Creates a generic instance of the specified class, using its constructor that requires no
     * argument.
     *
     * @param tClass the class to create an instance of
     * @param <T>    the class's type
     * @return a new instance of the class
     * @throws ReflectionException if the class doesn't have a constructor without arguments, or if
     *                             the constructor cannot be accessed, or for another reason.
     */
    private <T> T createInstance(Class<T> tClass) {
        try {
            if (ignoreConstructor) {
                return (T) Reflex.Companion.unsafeInstance(tClass);
            }
            Constructor<T> ctor = tClass.getDeclaredConstructor(); // constructor without params
            if (!ctor.isAccessible()) {
                ctor.setAccessible(true); // forces the constructor to be accessible
            }
            return ctor.newInstance(); // calls the constructor
        } catch (ReflectiveOperationException ex) {
            throw new ReflectionException("Unable to create an instance of " + tClass, ex);
        }
    }

    /**
     * 获取字段的转换器
     */
    private Converter getConverter(Field field) {
        // 优先获取 @Converter 注解
        Converter converter = AnnotationUtils.getConverter(field);
        if (converter != null) return converter;
        // 已知的包装类型
        if (UUID.class == field.getType()) {
            return new UUIDConverter();
        }
        if (Map.class.isAssignableFrom(field.getType())) {
            return new MapConverter();
        }
        return null;
    }

    /**
     * 获取内置转换器
     */
    private InnerConverter getInnerConverter(Class<?> type) {
        ReflexClass reflexClass = ReflexClass.Companion.of(type, true);
        ClassMethod toField = reflexClass.getStructure().getMethods().stream().filter(it -> it.getName().equals("toField")).findFirst().orElse(null);
        ClassMethod fromField = reflexClass.getStructure().getMethods().stream().filter(it -> it.getName().equals("fromField")).findFirst().orElse(null);
        if (toField == null && fromField == null) {
            return null;
        }
        if (toField != null && toField.getResult().getInstance() != ConvertResult.class) {
            throw new IllegalStateException("InnerConverter method must return ConvertResult");
        }
        if (fromField != null && fromField.getResult().getInstance() != ConvertResult.class) {
            throw new IllegalStateException("InnerConverter method must return ConvertResult");
        }
        if ((toField == null && fromField != null) || (toField != null && fromField == null)) {
            throw new IllegalStateException("InnerConverter method must have two methods toField and fromField");
        }
        return new InnerConverter(toField, fromField);
    }
}
