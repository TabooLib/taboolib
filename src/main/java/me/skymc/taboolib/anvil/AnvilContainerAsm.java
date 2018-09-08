package me.skymc.taboolib.anvil;

import org.objectweb.asm.*;

/**
 * @author sky
 */
public class AnvilContainerAsm {

    public static byte[] create(String version) {

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "me/skymc/taboolib/anvil/AnvilContainer", null, "net/minecraft/server/" + version + "/ContainerAnvil", null);

        cw.visitSource("AnvilContainer.java", null);

        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Lnet/minecraft/server/" + version + "/EntityHuman;)V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(12, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/server/" + version + "/EntityHuman", "inventory", "Lnet/minecraft/server/" + version + "/PlayerInventory;");
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/server/" + version + "/EntityHuman", "world", "Lnet/minecraft/server/" + version + "/World;");
            mv.visitTypeInsn(Opcodes.NEW, "net/minecraft/server/" + version + "/BlockPosition");
            mv.visitInsn(Opcodes.DUP);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraft/server/" + version + "/BlockPosition", "<init>", "(III)V", false);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraft/server/" + version + "/ContainerAnvil", "<init>", "(Lnet/minecraft/server/" + version + "/PlayerInventory;Lnet/minecraft/server/" + version + "/World;Lnet/minecraft/server/" + version + "/BlockPosition;Lnet/minecraft/server/" + version + "/EntityHuman;)V", false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(13, l1);
            mv.visitInsn(Opcodes.RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", "Lme/skymc/taboolib/anvil/AnvilContainer;", null, l0, l2, 0);
            mv.visitLocalVariable("player", "Lnet/minecraft/server/" + version + "/EntityHuman;", null, l0, l2, 1);
            mv.visitMaxs(8, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "a", "(Lnet/minecraft/server/" + version + "/EntityHuman;)Z", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(17, l0);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IRETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "Lme/skymc/taboolib/anvil/AnvilContainer;", null, l0, l1, 0);
            mv.visitLocalVariable("player", "Lnet/minecraft/server/" + version + "/EntityHuman;", null, l0, l1, 1);
            mv.visitMaxs(1, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "openAnvil", "(Lorg/bukkit/entity/Player;)V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(21, l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.CHECKCAST, "org/bukkit/craftbukkit/" + version + "/entity/CraftPlayer");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/bukkit/craftbukkit/" + version + "/entity/CraftPlayer", "getHandle", "()Lnet/minecraft/server/" + version + "/EntityPlayer;", false);
            mv.visitVarInsn(Opcodes.ASTORE, 1);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(22, l1);
            mv.visitTypeInsn(Opcodes.NEW, "me/skymc/taboolib/anvil/AnvilContainer");
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "me/skymc/taboolib/anvil/AnvilContainer", "<init>", "(Lnet/minecraft/server/" + version + "/EntityHuman;)V", false);
            mv.visitVarInsn(Opcodes.ASTORE, 2);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLineNumber(23, l2);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/server/" + version + "/EntityPlayer", "nextContainerCounter", "()I", false);
            mv.visitVarInsn(Opcodes.ISTORE, 3);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLineNumber(24, l3);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/server/" + version + "/EntityPlayer", "playerConnection", "Lnet/minecraft/server/" + version + "/PlayerConnection;");
            mv.visitTypeInsn(Opcodes.NEW, "net/minecraft/server/" + version + "/PacketPlayOutOpenWindow");
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ILOAD, 3);
            mv.visitLdcInsn("minecraft:anvil");
            mv.visitTypeInsn(Opcodes.NEW, "net/minecraft/server/" + version + "/ChatMessage");
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn("Repairing");
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraft/server/" + version + "/ChatMessage", "<init>", "(Ljava/lang/String;[Ljava/lang/Object;)V", false);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraft/server/" + version + "/PacketPlayOutOpenWindow", "<init>", "(ILjava/lang/String;Lnet/minecraft/server/" + version + "/IChatBaseComponent;I)V", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/server/" + version + "/PlayerConnection", "sendPacket", "(Lnet/minecraft/server/" + version + "/Packet;)V", false);
            Label l4 = new Label();
            mv.visitLabel(l4);
            mv.visitLineNumber(25, l4);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitFieldInsn(Opcodes.PUTFIELD, "net/minecraft/server/" + version + "/EntityPlayer", "activeContainer", "Lnet/minecraft/server/" + version + "/Container;");
            Label l5 = new Label();
            mv.visitLabel(l5);
            mv.visitLineNumber(26, l5);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/server/" + version + "/EntityPlayer", "activeContainer", "Lnet/minecraft/server/" + version + "/Container;");
            mv.visitVarInsn(Opcodes.ILOAD, 3);
            mv.visitFieldInsn(Opcodes.PUTFIELD, "net/minecraft/server/" + version + "/Container", "windowId", "I");
            Label l6 = new Label();
            mv.visitLabel(l6);
            mv.visitLineNumber(27, l6);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/server/" + version + "/EntityPlayer", "activeContainer", "Lnet/minecraft/server/" + version + "/Container;");
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/server/" + version + "/Container", "addSlotListener", "(Lnet/minecraft/server/" + version + "/ICrafting;)V", false);
            Label l7 = new Label();
            mv.visitLabel(l7);
            mv.visitLineNumber(28, l7);
            mv.visitInsn(Opcodes.RETURN);
            Label l8 = new Label();
            mv.visitLabel(l8);
            mv.visitLocalVariable("p", "Lorg/bukkit/entity/Player;", null, l0, l8, 0);
            mv.visitLocalVariable("player", "Lnet/minecraft/server/" + version + "/EntityPlayer;", null, l1, l8, 1);
            mv.visitLocalVariable("container", "Lme/skymc/taboolib/anvil/AnvilContainer;", null, l2, l8, 2);
            mv.visitLocalVariable("c", "I", null, l3, l8, 3);
            mv.visitMaxs(9, 4);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}
