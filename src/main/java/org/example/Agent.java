package org.example;


import org.example.context.ApplicationContextEnhancer;
import org.example.server.WebServer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Agent {
    public static void premain(String agentArgs, Instrumentation inst) {
        // 强制加载 ApplicationContextInterceptor 类
        try {
            Class.forName("org.example.context.ApplicationContextHolder");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // 注册一个 ClassFileTransformer
        inst.addTransformer(new ApplicationContextTransformer());
        System.out.println("Agent started.");
        WebServer.start();
        System.out.println("Agent premain completed.");
    }

    static class ApplicationContextTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws IllegalClassFormatException {
            if ("org/springframework/context/support/AbstractApplicationContext".equals(className)) {
                ClassReader cr = new ClassReader(classfileBuffer);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
                ApplicationContextEnhancer enhancer = new ApplicationContextEnhancer(cw);
                cr.accept(enhancer, ClassReader.EXPAND_FRAMES);
//            try {
//                saveClassFile(cw.toByteArray(), "D:/EnhancedAbstractApplicationContext.class");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }

                return cw.toByteArray();
            }
            return null; // 没有修改字节码
        }

        public static void saveClassFile(byte[] classBytes, String fileName) throws IOException {
            try (FileOutputStream fos = new FileOutputStream(new File(fileName))) {
                fos.write(classBytes);
                System.out.println("写入saveClassFile");
            }
        }
    }

}