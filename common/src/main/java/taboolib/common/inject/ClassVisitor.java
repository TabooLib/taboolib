package taboolib.common.inject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.ClassField;
import org.tabooproject.reflex.ClassMethod;
import taboolib.common.LifeCycle;

import java.util.function.Supplier;

/**
 * TabooLib
 * taboolib.common.inject.ClassVisitor
 *
 * @author sky
 * @since 2021/8/14 12:18 上午
 */
public abstract class ClassVisitor {

   private final byte priority;

   public ClassVisitor() {
      this.priority = 0;
   }

   protected ClassVisitor(byte priority) {
      this.priority = priority;
   }

   @NotNull
   abstract public LifeCycle getLifeCycle();

   public void visitStart(@NotNull Class<?> clazz, @Nullable Supplier<?> instance) {}

   public void visitEnd(@NotNull Class<?> clazz, @Nullable Supplier<?> instance) {}

   public void visit(@NotNull ClassField field, @NotNull Class<?> clazz, @Nullable Supplier<?> instance) {}

   public void visit(@NotNull ClassMethod method, @NotNull Class<?> clazz, @Nullable Supplier<?> instance) {}

   public byte getPriority() {
      return this.priority;
   }
}
