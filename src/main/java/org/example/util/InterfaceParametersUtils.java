package org.example.util;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;
import java.util.Map;

public class InterfaceParametersUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 根据解决类名和方法名，获取接口参数
     */
    public static List<Object> getInterfaceInputJsonString(String interfaceName, String methodName) {
        Method[] methods;
        List<Object> jsonInput = new ArrayList<>();
        try {
            methods = Class.forName(interfaceName).getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    jsonInput.add(getMethodsParameterJson(method));
                }
            }
        } catch (Exception e) {
            System.out.println(String.format("获取%s Bean %s方法 参数列表失败！", interfaceName, methodName));
            e.printStackTrace();
        }
        return jsonInput;
    }


    public static Object getMethodsParameterJson(Method method) {
        Class[] ts = method.getParameterTypes();
        List<Object> list = new ArrayList<>();
        for (Type t : ts) {
            list.add((getInstance((Class<?>) t)));
        }

        return list.size() > 1 ? list : list.get(0);

    }

    private static Object getInstance(Class<?> clazz){
        try {

            return  GenerateNodeService.generateNode(clazz);
        }catch (Exception e){
            return null;
        }

    }

}
