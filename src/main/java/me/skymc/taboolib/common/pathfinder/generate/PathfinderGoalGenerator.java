package me.skymc.taboolib.common.pathfinder.generate;

import me.skymc.taboolib.TabooLib;
import org.objectweb.asm.*;

/**
 * @author idea
 */
public class PathfinderGoalGenerator {

    public static byte[] generate() {
        String version = TabooLib.getVersion();

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "me/skymc/taboolib/common/pathfinder/internal/InternalPathfinderGoal", null, "net/minecraft/server/" + version + "/PathfinderGoal", null);

        cw.visitSource("InternalPathfinderGoal.java", null);

        {
            fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAi;", null, null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Lme/skymc/taboolib/common/pathfinder/SimpleAi;)V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(15, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraft/server/" + version + "/PathfinderGoal", "<init>", "()V", false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(16, l1);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, "me/skymc/taboolib/common/pathfinder/internal/InternalPathfinderGoal", "simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAi;");
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLineNumber(17, l2);
            mv.visitInsn(Opcodes.RETURN);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLocalVariable("this", "Lme/skymc/taboolib/common/pathfinder/internal/InternalPathfinderGoal;", null, l0, l3, 0);
            mv.visitLocalVariable("simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAi;", null, l0, l3, 1);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "a", "()Z", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(21, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "me/skymc/taboolib/common/pathfinder/internal/InternalPathfinderGoal", "simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAi;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/skymc/taboolib/common/pathfinder/SimpleAi", "shouldExecute", "()Z", false);
            mv.visitInsn(Opcodes.IRETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "Lme/skymc/taboolib/common/pathfinder/internal/InternalPathfinderGoal;", null, l0, l1, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "b", "()Z", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(26, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "me/skymc/taboolib/common/pathfinder/internal/InternalPathfinderGoal", "simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAi;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/skymc/taboolib/common/pathfinder/SimpleAi", "continueExecute", "()Z", false);
            mv.visitInsn(Opcodes.IRETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "Lme/skymc/taboolib/common/pathfinder/internal/InternalPathfinderGoal;", null, l0, l1, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "c", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(31, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "me/skymc/taboolib/common/pathfinder/internal/InternalPathfinderGoal", "simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAi;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/skymc/taboolib/common/pathfinder/SimpleAi", "startTask", "()V", false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(32, l1);
            mv.visitInsn(Opcodes.RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", "Lme/skymc/taboolib/common/pathfinder/internal/InternalPathfinderGoal;", null, l0, l2, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "d", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(36, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "me/skymc/taboolib/common/pathfinder/internal/InternalPathfinderGoal", "simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAi;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/skymc/taboolib/common/pathfinder/SimpleAi", "resetTask", "()V", false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(37, l1);
            mv.visitInsn(Opcodes.RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", "Lme/skymc/taboolib/common/pathfinder/internal/InternalPathfinderGoal;", null, l0, l2, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "e", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(41, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "me/skymc/taboolib/common/pathfinder/internal/InternalPathfinderGoal", "simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAi;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/skymc/taboolib/common/pathfinder/SimpleAi", "updateTask", "()V", false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(42, l1);
            mv.visitInsn(Opcodes.RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", "Lme/skymc/taboolib/common/pathfinder/internal/InternalPathfinderGoal;", null, l0, l2, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}
