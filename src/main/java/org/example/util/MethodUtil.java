package org.example.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodUtil {

    public static List<Method> getMethods(Class<?> realBeanClass) {
        List<Method> methodList = new ArrayList<>();
        for (Class<?> beanInterface : realBeanClass.getInterfaces()) {
            for (Method method : beanInterface.getMethods()) {
                if (!methodList.contains(method)) {
                    methodList.add(method);
                }
            }
        }
        for (Method method : realBeanClass.getDeclaredMethods()) {
            if (!methodList.contains(method)) {
                methodList.add(method);
            }
        }
        return methodList;
    }
}
