package org.example.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.context.ApplicationContextHolder;
import org.example.vo.Resp;
import org.springframework.aop.TargetClassAware;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.DirectFieldAccessor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.*;

public class RequestServlet extends HttpServlet {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HashMap<String, String> hashMap = mapper.readValue(req.getInputStream(), HashMap.class);
        String beanName = hashMap.get("serviceName");
        String methodName = hashMap.get("methodName");
        String param = hashMap.get("param");
        Object bean = ApplicationContextHolder.getBean(beanName);
        // 获取Bean的真实类，这样保证JSON.parseObject按照类型对泛型，反序列化不出问题
        Class<?> realBeanClass = AopUtils.getTargetClass(bean);
        Object result = null;

        try {
            if (AopUtils.isCglibProxy(bean)){//cglib动态代理
                Method method = Arrays.stream(realBeanClass.getDeclaredMethods()).filter(v -> methodName.equals(v.getName())).findFirst().orElse(null);
                try {
                    result = invokeMethod(bean, method, param);

                } catch (Exception e) {
                    System.err.println("invokeMethod failed");
                    e.printStackTrace();

                }
            }
            if(AopUtils.isJdkDynamicProxy(bean)){//jdk动态代理
                Proxy proxyBean = (Proxy)bean;
                InvocationHandler handler = (InvocationHandler) new DirectFieldAccessor(proxyBean)
                        .getPropertyValue("h");
                Class<?> clazz = ((TargetClassAware) bean).getTargetClass();
                if(clazz == null){
                    System.err.println("clazz is not exist");
                    result = null;
                }
                Method method = Arrays.stream(clazz.getDeclaredMethods()).filter(v -> methodName.equals(v.getName())).findFirst().orElse(null);
                result = invokeProxyMethod(handler, bean, method, param);
            }
            // 必须用getInterfaces，这样才能取到方法参数的泛型
            if (bean.getClass().isInterface()) {
                // 获取接口或类的所有方法
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

                Method method = methodList.stream().filter(v -> methodName.equals(v.getName())).findFirst().orElse(null);
                try {
                    result = invokeMethod(bean, method, param);
                } catch (Exception e) {
                    System.err.println("invokeMethod failed A");
                    e.printStackTrace();
                }
            } else {
                // 非接口实现类
                Method method = Arrays.stream(realBeanClass.getDeclaredMethods()).filter(v -> methodName.equals(v.getName())).findFirst().orElse(null);
                try {
                    result = invokeMethod(bean, method, param);

                } catch (Exception e) {
                    System.err.println("invokeMethod failed B");
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            System.err.println("invokeMethod failed C");
            e.printStackTrace();
        }


        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String jsonResponse = mapper.writeValueAsString(Resp.success(result));

        resp.getWriter().write(jsonResponse);
    }
    public Object invokeMethod(Object bean, Method targetMethod, String inputParams) throws Exception {
        if(targetMethod == null){
            System.err.println("targetMethod is null");
            return null;
        }
        Class[] types = targetMethod.getParameterTypes();
        List<String> paramList = processParams(targetMethod, inputParams);
        List<Object> params = new ArrayList<>();
        // 参数数量必须相同
        if (types.length == paramList.size()) {
            for (int i = 0; i < types.length; i++) {
                String paramJson = paramList.get(i);
                Object paramObject;
                try {
                    paramObject = types[i] == String.class ? paramJson : mapper.readValue(paramJson, types[i]);
                    Class<String> stringClass = String.class;
                } catch (Exception e) {
                    paramObject = paramList.get(i);
                }
                params.add(paramObject);
            }
            return targetMethod.invoke(bean, params.toArray());
        }
        return null;
    }

    public Object invokeProxyMethod(InvocationHandler handler, Object bean, Method targetMethod, String inputParams) throws Throwable {
        if(targetMethod == null){
            System.err.println("targetMethod is null");
            return null;
        }
        Class[] types = targetMethod.getParameterTypes();
        List<String> paramList = processParams(targetMethod, inputParams);
        List<Object> params = new ArrayList<>();
        // 参数数量必须相同
        if (types.length == paramList.size()) {
            for (int i = 0; i < types.length; i++) {
                String paramJson = paramList.get(i);
                Object paramObject;
                try {

                    paramObject = types[i] == String.class ? paramJson : mapper.readValue(paramJson, types[i]);
                } catch (Exception e) {
                    paramObject = paramList.get(i);
                }
                params.add(paramObject);
            }
            return handler.invoke(bean, targetMethod, params.toArray());
        }
        return null;
    }

    private List<String> processParams(Method method, String param) {
        List<String> paramList = new ArrayList<>();
        Type[] types = method.getGenericParameterTypes();
        if (types.length == 1) {
            paramList.add(param);
        }
        // 超过2个参数，将JSON参数转为List<String>，后续再逐个转为对应的对象
        if (types.length > 1) {
            List<String> tmepList = null;
            try {
                tmepList = mapper.readValue(param, new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            paramList.addAll(tmepList);
        }

        return paramList;
    }
}