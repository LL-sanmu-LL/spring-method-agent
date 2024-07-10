package org.zj;


import org.zj.context.ApplicationContextEnhancer;
import org.zj.server.WebServer;
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
        try {
            Class.forName("org.zj.context.ApplicationContextHolder");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        inst.addTransformer(new ApplicationContextTransformer());
        System.out.println("spring method agent started.");
        WebServer.start();
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
                return cw.toByteArray();
            }
            return null;
        }
    }

}