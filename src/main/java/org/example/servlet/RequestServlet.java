package org.example.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.context.ApplicationContextHolder;
import org.example.util.MethodUtil;
import org.example.vo.Resp;
import org.springframework.aop.TargetClassAware;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
            if (AopUtils.isCglibProxy(bean)) {//cglib动态代理
                Method method = Arrays.stream(realBeanClass.getDeclaredMethods()).filter(v -> methodName.equals(v.getName())).findFirst().orElse(null);
                try {
                    result = invokeMethod(bean, method, param);

                } catch (Exception e) {
                    System.err.println("invokeMethod failed");
                    e.printStackTrace();

                }
            } else if (AopUtils.isJdkDynamicProxy(bean)) {//jdk动态代理
                Proxy proxyBean = (Proxy) bean;
                InvocationHandler handler = (InvocationHandler) new DirectFieldAccessor(proxyBean)
                        .getPropertyValue("h");
                Class<?> clazz = ((TargetClassAware) bean).getTargetClass();
                if (clazz == null) {
                    System.err.println("clazz is not exist");
                    result = null;
                }
                Method method = Arrays.stream(clazz.getDeclaredMethods()).filter(v -> methodName.equals(v.getName())).findFirst().orElse(null);
                result = invokeProxyMethod(handler, bean, method, param);
            }else if (bean.getClass().isInterface()) {// 必须用getInterfaces，这样才能取到方法参数的泛型
                // 获取接口或类的所有方法
                List<Method> methodList = MethodUtil.getMethods(realBeanClass);

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
        List<Object> params = buildParam(targetMethod, inputParams);
        if (params != null) {
            targetMethod.setAccessible(true);
            return targetMethod.invoke(bean, params.toArray());
        }
        return null;
    }

    public Object invokeProxyMethod(InvocationHandler handler, Object bean, Method targetMethod, String inputParams) throws Throwable {
        List<Object> params = buildParam(targetMethod, inputParams);
        if (params != null) {
            targetMethod.setAccessible(true);
            return handler.invoke(bean, targetMethod, params.toArray());
        }
        return null;
    }

    private static List<Object> buildParam(Method targetMethod, String inputParams) throws JsonProcessingException {
        if (targetMethod == null) {
            System.err.println("targetMethod is null");
            return null;
        }
        Class<?>[] types = targetMethod.getParameterTypes();
        List<Object> params = new ArrayList<>();
        JsonNode jsonNode = mapper.readTree(inputParams);
        // 参数数量必须相同
        if (types.length == jsonNode.size()) {
            for (int i = 0; i < types.length; i++) {
                JsonNode jsonNode1 = jsonNode.get(i);
                Object paramObject = mapper.treeToValue(jsonNode1, types[i]);
                params.add(paramObject);
            }
            return params;
        } else {
            return null;
        }
    }
}