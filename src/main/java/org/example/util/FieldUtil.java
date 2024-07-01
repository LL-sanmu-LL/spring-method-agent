package org.example.util;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class FieldUtil {

    public static Object generateGenericInstance(Class<?> clz, Type type) throws Exception {
        try {
            if (clz == List.class || clz == ArrayList.class) {
                ArrayList<Object> list = new ArrayList<>();
                if (type instanceof ParameterizedType) {
                    Type subType = ((ParameterizedType) type).getActualTypeArguments()[0];
                    list.add(generateGenericInstance((Class<?>) type, subType));
                }else{
                    list.add(generateClassInstance((Class<?>) type));
                }
                return list;
            } else if (clz == Map.class || clz == HashMap.class) {
                return new HashMap<>();
            }
        } catch (Exception var12) {
            var12.printStackTrace();
        }
        return null;
    }

    public static Object generateClassInstance(Class<?> clz) throws Exception {
        Object obj = null;
        if (isBaseType(clz)) {
            if (clz == Integer.class) {
                obj = 0;
            } else if (clz == Long.class) {
                obj = 0L;
            } else if (clz == Boolean.class) {
                obj = false;
            } else if (clz == BigDecimal.class) {
                obj = BigDecimal.ZERO;
            } else if (clz == String.class) {
                obj = "";
            } else {
                obj = clz.newInstance();
            }
            return obj;
        } else {
            try {
                if (clz.isInterface() || clz.isEnum()) {
                    return obj;
                }
                obj = clz.newInstance();

                //TODO 填充obj的属性
            } catch (Exception var12) {
                var12.printStackTrace();
            }
            return obj;
        }
    }

    public static Object generateFieldInstance(Class<?> clz) throws Exception {

        Object obj = null;
        if (isBaseType(clz)) {
            if (clz == Integer.class) {
                obj = 0;
            } else if (clz == Long.class) {
                obj = 0L;
            } else if (clz == Boolean.class) {
                obj = false;
            } else if (clz == BigDecimal.class) {
                obj = BigDecimal.ZERO;
            } else if (clz == String.class) {
                obj = "";
            } else {
                obj = clz.newInstance();
            }
            return obj;
        } else {
            try {
                if (clz == List.class || clz == ArrayList.class) {
                    return new ArrayList<>();
                } else if (clz == Map.class || clz == HashMap.class) {
                    return new HashMap<>();
                } else if (clz.isInterface() || clz.isEnum()) {
                    return obj;
                }
                obj = clz.newInstance();
                List<Field> filteredFields = getClassField(clz)
                        .stream()
                        .filter(field -> !field.getName().equals("serialVersionUID") && !isBaseType(field.getType()))
                        .collect(Collectors.toList());

                Map<String, Method> methodMap = getClassMethod(clz);
                for (Field field : filteredFields) {
                    Object fieldObj = getSubFieldInstance(field);
                    fillSubField(field, methodMap, obj, fieldObj);
                }

            } catch (Exception var12) {
                var12.printStackTrace();
            }

            return obj;
        }
    }

    private static void fillSubField(Field field, Map<String, Method> methodMap, Object obj, Object fieldObj) throws IllegalAccessException, InvocationTargetException {
        if (fieldObj == null) {
            return;
        }
        String methodName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        Method method = methodMap.get(methodName);
        method.invoke(obj, fieldObj);
    }

    private static Object getSubFieldInstance(Field field) throws Exception {
        Object fieldObj = null;
        if (field.getType() == List.class || field.getType() == ArrayList.class) {
            Class<?> genericType = getGenericType(field, 0);
            fieldObj = new ArrayList<>();
            List<Object> listObj = (List<Object>) fieldObj;
            Object genericTypeInstance = generateFieldInstance(genericType);
            listObj.add(genericTypeInstance);
        } else if (field.getType() == Map.class || field.getType() == HashMap.class) {
            fieldObj = new HashMap<>();
            Class<?> firstGenericType = getGenericType(field, 0);
            Class<?> secondGenericType = getGenericType(field, 1);
            if (firstGenericType != null && secondGenericType != null) {
                Object first = generateFieldInstance(firstGenericType);
                Object second = generateFieldInstance(secondGenericType);
                Map<Object, Object> mapObj = (Map<Object, Object>) fieldObj;
                if (first != null && second != null) {
                    mapObj.put(first, second);
                }
            }
        } else {
            fieldObj = generateFieldInstance(field.getType());
        }
        return fieldObj;
    }

    /**
     * 获取字段的第position个泛型的类型
     */
    private static Class<?> getGenericType(Field field, int postion) throws Exception {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {//判断是否是范型类
            ParameterizedType pt = (ParameterizedType) genericType;

            try {
                Class<?> genericClazz = (Class<?>) pt.getActualTypeArguments()[postion];
                return Class.forName(genericClazz.getName());
            } catch (Exception e) {
                return null;//泛型内嵌套泛型?先不考虑这么复杂
            }
        } else {
            return null;
        }
    }

    private static boolean isBaseType(Class<?> clz) {
        return clz == Integer.class
                || clz == String.class
                || clz == Boolean.class
                || clz == Long.class
                || clz == BigDecimal.class
                || clz == Date.class;
    }

    /**
     * 获取一个类的所有属性
     */
    private static List<Field> getClassField(Class<?> clz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clz;
        while (currentClass != null) {
            Field[] declaredFields = currentClass.getDeclaredFields();
            fields.addAll(Arrays.asList(declaredFields));
            currentClass = currentClass.getSuperclass();
        }
        return fields;
    }

    /**
     * 获取一个类的所有方法
     */
    private static Map<String, Method> getClassMethod(Class<?> clz) {
        Map<String, Method> methodMap = new LinkedHashMap<>();
        LinkedList<Class<?>> classQueue = new LinkedList<>();
        classQueue.add(clz);
        while (!classQueue.isEmpty()) {
            Class<?> currentClass = classQueue.removeFirst();
            Method[] declaredMethods = currentClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                methodMap.putIfAbsent(method.getName(), method);
            }
            Class<?> superclass = currentClass.getSuperclass();
            if (superclass != null) {
                classQueue.add(superclass);
            }
        }
        return methodMap;
    }

}
