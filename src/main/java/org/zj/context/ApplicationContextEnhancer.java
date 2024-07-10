package org.zj.context;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ApplicationContextEnhancer extends ClassVisitor {

    public ApplicationContextEnhancer(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if ("refresh".equals(name) && "()V".equals(descriptor)) {
            return new RefreshMethodEnhancer(mv);
        }
        return mv;
    }

    static class RefreshMethodEnhancer extends MethodVisitor {

        public RefreshMethodEnhancer(MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }

        @Override
        public void visitCode() {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/zj/context/ApplicationContextHolder", "intercept", "(Lorg/springframework/context/support/AbstractApplicationContext;)V", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/zj/context/ApplicationContextHolder", "get", "()Lorg/springframework/context/ApplicationContext;", false);
            mv.visitVarInsn(Opcodes.ASTORE, 1);
            super.visitCode();
        }
    }
}
