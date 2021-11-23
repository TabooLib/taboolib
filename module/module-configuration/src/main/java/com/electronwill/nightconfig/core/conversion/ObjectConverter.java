package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.EnumGetMethod;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import taboolib.common.reflect.Reflex;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
                AnnotationUtils.checkField(field, value);/* Checks that the value is conform to an
																eventual @SpecSometing annotation */
                Converter<Object, Object> converter = AnnotationUtils.getConverter(field);
                if (converter != null) {
                    value = converter.convertFromField(value);
                }
                List<String> path = AnnotationUtils.getPath(field);
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
                Converter<Object, Object> converter = AnnotationUtils.getConverter(field);
                if (converter != null) {
                    value = converter.convertToField(value);
                }

                // --- Writes the value to the object's field, converting it if needed ---
                Class<?> fieldType = field.getType();
                try {
                    if (value instanceof UnmodifiableConfig && !(fieldType.isAssignableFrom(value.getClass()))) {
                        // --- Read as a sub-object ---
                        final UnmodifiableConfig cfg = (UnmodifiableConfig) value;

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
                        // --- Reads as a collection, maybe a list of objects with conversion ---
                        final Collection<?> src = (Collection<?>) value;
                        final Class<?> srcBottomType = bottomElementType(src);

                        final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                        final List<Class<?>> dstTypes = elementTypes(genericType);
                        final Class<?> dstBottomType = dstTypes.get(dstTypes.size() - 1);

                        if (srcBottomType == null
                                || dstBottomType == null
                                || dstBottomType.isAssignableFrom(srcBottomType)) {

                            // Simple list, no conversion needed
                            AnnotationUtils.checkField(field, value);
                            field.set(object, value);

                        } else {
                            // List of objects => the bottom elements need conversion

                            // Uses the current field value if there is one, or create a new list
                            Collection<Object> dst = (Collection<Object>) field.get(object);
                            if (dst == null) {
                                if (fieldType == ArrayList.class
                                        || fieldType.isInterface()
                                        || Modifier.isAbstract(fieldType.getModifiers())) {
                                    dst = new ArrayList<>(src.size());// allocates the right size
                                } else {
                                    dst = (Collection<Object>) createInstance(fieldType);
                                }
                                field.set(object, dst);
                            }

                            // Converts the elements of the list
                            convertConfigsToObject(src, dst, dstTypes, 0);

                            // Applies the checks
                            AnnotationUtils.checkField(field, dst);
                        }
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

    /**
     * Gets the type of the "bottom element" of a list.
     * For instance, for {@code LinkedList<List<List<Supplier<String>>>>}
     * this method returns the class {@code Supplier}.
     *
     * @param genericType the generic list type
     * @return the type of the elements of the most nested list
     */
    private Class<?> bottomElementType(ParameterizedType genericType) {
        if (genericType != null && genericType.getActualTypeArguments().length > 0) {
            Type parameter = genericType.getActualTypeArguments()[0];
            if (parameter instanceof ParameterizedType) {
                ParameterizedType genericParameter = (ParameterizedType) parameter;
                Class<?> paramClass = (Class<?>) genericParameter.getRawType();
                if (paramClass.isAssignableFrom(Collection.class)) {
                    return bottomElementType(genericParameter);
                } else {
                    return paramClass;
                }
            }
            if ((parameter instanceof Class)) {
                return (Class<?>) parameter;
            }
        }
        return null;
    }

    private void detectElementTypes(ParameterizedType genericType, List<Class<?>> storage) {
        if (genericType != null && genericType.getActualTypeArguments().length > 0) {
            Type parameter = genericType.getActualTypeArguments()[0];
            if (parameter instanceof ParameterizedType) {
                ParameterizedType genericParameter = (ParameterizedType) parameter;
                Class<?> paramClass = (Class<?>) genericParameter.getRawType();

                storage.add(paramClass);
                if (Collection.class.isAssignableFrom(paramClass)) {
                    detectElementTypes(genericParameter, storage);
                }
            } else if ((parameter instanceof Class)) {
                storage.add((Class<?>) parameter);
            }
        }
    }

    /**
     * Returns a list of the generic parameters of a list.
     * For instance, for {@code LinkedList<List<Collection<Supplier<String>>>>}
     * this method returns a list containing {@code [Collection.class, Supplier.class]}.
     *
     * @param genericType the list generic type
     * @return a list of the types of the list's elements
     */
    private List<Class<?>> elementTypes(ParameterizedType genericType) {
        List<Class<?>> storage = new ArrayList<>();
        detectElementTypes(genericType, storage);
        return storage;
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
     * Converts a collection of configurations to a collection of objects of the type dstBottomType.
     *
     * @param src             the collection of configs, may be nested, source
     * @param dst             the collection of objects, destination
     * @param dstElementTypes the type of lists and objects in dst
     */
    private void convertConfigsToObject(Collection<?> src,
                                        Collection<Object> dst,
                                        List<Class<?>> dstElementTypes,
                                        int currentLevel) {
        final Class<?> currentType = dstElementTypes.get(currentLevel);
        for (Object elem : src) {
            if (elem == null) {
                dst.add(null);
            } else if (elem instanceof Collection) {
                final Collection<?> subSrc = (Collection<?>) elem;
                final Collection<Object> subDst;

                if (currentType == ArrayList.class
                        || currentType.isInterface()
                        || Modifier.isAbstract(currentType.getModifiers())) {

                    subDst = new ArrayList<>();
                } else {
                    subDst = (Collection<Object>) createInstance(currentType);
                }
                convertConfigsToObject(subSrc, subDst, dstElementTypes, currentLevel + 1);
                dst.add(subDst);
            } else if (elem instanceof UnmodifiableConfig) {
                Object elementObj = createInstance(currentType);
                convertToObject((UnmodifiableConfig) elem, elementObj, currentType);
                dst.add(elementObj);
            } else {
                String elemType = elem.getClass().toString();
                throw new InvalidValueException("Unexpected element of type " + elemType + " in collection of objects");
            }
        }
    }

    /**
     * Converts a collection of objects of the type srcBottomType to a collection of configurations.
     *
     * @param src           the collection of objects, may be nested, source
     * @param srcBottomType the type of objects
     * @param dst           the collection of configs, destination
     * @param parentConfig  the parent configuration, used to create the new configs to put in dst
     */
    private void convertObjectsToConfigs(Collection<?> src,
                                         Class<?> srcBottomType,
                                         Collection<Object> dst,
                                         Config parentConfig) {
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
}