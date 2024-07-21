package taboolib.library.xseries.reflection;

import taboolib.library.xseries.reflection.jvm.NamedReflectiveHandle;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface ReflectiveMapping {
    boolean shouldBeChecked();

    String category();

    String name();

    String process(NamedReflectiveHandle handle, String name);
}
