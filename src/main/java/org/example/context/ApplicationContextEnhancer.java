package org.example.context;

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
            // 匹配到 refresh 方法，进行增强处理
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
            mv.visitVarInsn(Opcodes.ALOAD, 0); // 将 this 对象加载到栈顶
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/example/context/ApplicationContextHolder", "intercept", "(Lorg/springframework/context/support/AbstractApplicationContext;)V", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/example/context/ApplicationContextHolder", "get", "()Lorg/springframework/context/ApplicationContext;", false);
            // 如果需要使用返回值，可以通过以下方式存储到局部变量表中
            mv.visitVarInsn(Opcodes.ASTORE, 1); // 将返回值存储到局部变量表中的索引为1的位置
            super.visitCode();
        }
//
//        @Override
//        public void visitInsn(int opcode) {
//            if (opcode == Opcodes.RETURN) {
//                // 在方法体的结束处插入日志输出
//                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//                mv.visitLdcInsn("Enhanced: ApplicationContext refresh completed");
//                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
//            }
//            super.visitInsn(opcode);
//        }
    }
}
