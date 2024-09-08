package taboolib.library.xseries.reflection.jvm;

import taboolib.library.xseries.reflection.XReflection;
import taboolib.library.xseries.reflection.jvm.classes.ClassHandle;
import taboolib.library.xseries.reflection.jvm.classes.PackageHandle;
import taboolib.library.xseries.reflection.minecraft.MinecraftMapping;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

/**
 * A handle for using reflection for enums, which are technically a {@link Field}.
 */
public class EnumMemberHandle extends NamedMemberHandle {
    public EnumMemberHandle(ClassHandle clazz) {
        super(clazz);
    }

    public EnumMemberHandle map(MinecraftMapping mapping, @Pattern(PackageHandle.JAVA_IDENTIFIER_PATTERN) String name) {
        super.map(mapping, name);
        return this;
    }

    public EnumMemberHandle named(@Pattern(PackageHandle.JAVA_IDENTIFIER_PATTERN) String... names) {
        super.named(names);
        return this;
    }

    /**
     * Why would you even want to use this method for enums?
     */
    @Override
    public MemberHandle signature(String declaration) {
        throw new UnsupportedOperationException();
    }

    /**
     * Use {@link #getEnumConstant()} instead.
     */
    @NotNull
    @Override
    public MethodHandle unreflect() {
        return super.unreflect();
    }

    /**
     * Use {@link #getEnumConstant()} instead.
     */
    @Override
    public @Nullable MethodHandle reflectOrNull() {
        return super.reflectOrNull();
    }

    /**
     * Use {@link #getEnumConstant()} instead.
     * If you want to use this, you can do so with:
     * <pre>{@code
     *     Object enumConstant = handle.reflect().invoke();
     * }</pre>
     */
    @Override
    public MethodHandle reflect() throws ReflectiveOperationException {
        Field jvm = reflectJvm();
        return clazz.getNamespace().getLookup().unreflectGetter(jvm);
    }

    @Nullable
    public Object getEnumConstant() {
        try {
            return reflectJvm().get(null);
        } catch (ReflectiveOperationException ex) {
            throw XReflection.throwCheckedException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Field reflectJvm() throws ReflectiveOperationException {
        if (names.isEmpty()) throw new IllegalStateException("No enum names specified");
        NoSuchFieldException errors = null;
        Field field = null;

        Class<?> clazz = this.clazz.reflect();
        for (String name : this.names) {
            if (field != null) break;
            try {
                field = clazz.getDeclaredField(name);
                if (!field.isEnumConstant()) {
                    throw new NoSuchFieldException("Field named '" + name + "' was found but it's not an enum constant " + this);
                }
            } catch (NoSuchFieldException ex) {
                field = null;
                if (errors == null) errors = new NoSuchFieldException("None of the enums were found for " + this);
                errors.addSuppressed(ex);
            }
        }

        if (field == null) throw XReflection.relativizeSuppressedExceptions(errors);
        return handleAccessible(field);
    }

    /**
     * Why would you even want to use this method for enums?
     */
    @Override
    public EnumMemberHandle clone() {
        throw new UnsupportedOperationException();
    }
}
