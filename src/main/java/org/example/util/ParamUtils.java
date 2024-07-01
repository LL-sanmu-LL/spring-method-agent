package org.example.util;


import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ParamUtils {

    /**
     * 根据解决类名和方法名，获取接口参数
     */
    public static Object getInterfaceInputJsonString(String interfaceName, String methodName) {
        Method[] methods;
        List<Object> jsonInput = new ArrayList<>();
        try {
            methods = Class.forName(interfaceName).getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    return getMethodsParameterJson(method);
                }
            }
        } catch (Exception e) {
            System.out.println(String.format("获取%s Bean %s方法 参数列表失败！", interfaceName, methodName));
            e.printStackTrace();
        }
        return jsonInput;
    }

    public static Object getMethodsParameterJson(Method method) {
        Type[] typeList = method.getGenericParameterTypes();
        Class<?>[] classList = method.getParameterTypes();
        ArrayList<Object> list = new ArrayList<>();
        for (int i = 0; i < typeList.length; i++) {
            if (typeList[i] instanceof ParameterizedType) {
                Object genericInstance = ParamUtils.getGenericInstance(classList[i], ((ParameterizedType) typeList[i]).getActualTypeArguments()[0]);
                list.add(genericInstance);
            } else {
                Object classInstance = ParamUtils.getClassInstance(classList[i]);
                list.add(classInstance);
            }
        }
        return list.size() > 1 ? list : list.get(0);
    }

    /**
     * 获取非泛型实例
     */
    private static Object getClassInstance(Class<?> clazz) {
        try {
            return FieldUtil.generateClassInstance(clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取泛型实例
     */
    private static Object getGenericInstance(Class<?> clazz, Type type) {
        try {
            return FieldUtil.generateGenericInstance(clazz, type);
        } catch (Exception e) {
            return null;
        }
    }
}
