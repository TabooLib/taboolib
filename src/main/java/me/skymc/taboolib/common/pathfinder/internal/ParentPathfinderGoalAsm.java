package me.skymc.taboolib.common.pathfinder.internal;

import org.objectweb.asm.*;

/**
 * @author sky
 */
public class ParentPathfinderGoalAsm {

    public static byte[] create(String version) {

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "me/skymc/taboolib/common/pathfinder/internal/ParentPathfinderGoal", null, "net/minecraft/server/" + version + "/PathfinderGoal", null);

        cw.visitSource("ParentPathfinderGoal.java", null);

        {
            fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAI;", null, null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Lme/skymc/taboolib/common/pathfinder/SimpleAI;)V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(13, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraft/server/" + version + "/PathfinderGoal", "<init>", "()V", false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(14, l1);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, "me/skymc/taboolib/common/pathfinder/internal/ParentPathfinderGoal", "simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAI;");
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLineNumber(15, l2);
            mv.visitInsn(Opcodes.RETURN);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLocalVariable("this", "Lme/skymc/taboolib/common/pathfinder/internal/ParentPathfinderGoal;", null, l0, l3, 0);
            mv.visitLocalVariable("simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAI;", null, l0, l3, 1);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "a", "()Z", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(19, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "me/skymc/taboolib/common/pathfinder/internal/ParentPathfinderGoal", "simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAI;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/skymc/taboolib/common/pathfinder/SimpleAI", "shouldExecute", "()Z", false);
            mv.visitInsn(Opcodes.IRETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "Lme/skymc/taboolib/common/pathfinder/internal/ParentPathfinderGoal;", null, l0, l1, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "b", "()Z", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(24, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "me/skymc/taboolib/common/pathfinder/internal/ParentPathfinderGoal", "simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAI;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/skymc/taboolib/common/pathfinder/SimpleAI", "continueExecute", "()Z", false);
            mv.visitInsn(Opcodes.IRETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "Lme/skymc/taboolib/common/pathfinder/internal/ParentPathfinderGoal;", null, l0, l1, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "c", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(29, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "me/skymc/taboolib/common/pathfinder/internal/ParentPathfinderGoal", "simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAI;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/skymc/taboolib/common/pathfinder/SimpleAI", "startTask", "()V", false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(30, l1);
            mv.visitInsn(Opcodes.RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", "Lme/skymc/taboolib/common/pathfinder/internal/ParentPathfinderGoal;", null, l0, l2, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "d", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(34, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "me/skymc/taboolib/common/pathfinder/internal/ParentPathfinderGoal", "simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAI;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/skymc/taboolib/common/pathfinder/SimpleAI", "resetTask", "()V", false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(35, l1);
            mv.visitInsn(Opcodes.RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", "Lme/skymc/taboolib/common/pathfinder/internal/ParentPathfinderGoal;", null, l0, l2, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "e", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(39, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "me/skymc/taboolib/common/pathfinder/internal/ParentPathfinderGoal", "simpleAI", "Lme/skymc/taboolib/common/pathfinder/SimpleAI;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/skymc/taboolib/common/pathfinder/SimpleAI", "updateTask", "()V", false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(40, l1);
            mv.visitInsn(Opcodes.RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", "Lme/skymc/taboolib/common/pathfinder/internal/ParentPathfinderGoal;", null, l0, l2, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}
