package taboolib.library.xseries.reflection.constraint;

import taboolib.library.xseries.reflection.ReflectiveHandle;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public enum VisibilityConstraint implements ReflectiveConstraint {
    PUBLIC {
        @Override
        public boolean appliesTo(ReflectiveHandle<?> handle) {
            return false;
        }
    };

    @Override
    public String category() {
        return "Visibility";
    }
}
