package org.example.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.context.ApplicationContextHolder;
import org.example.vo.Resp;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MethodsServlet extends HttpServlet {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HashMap<String, String> hashMap = mapper.readValue(req.getInputStream(), HashMap.class);
        String beanName = hashMap.get("serviceName");
        Object bean = ApplicationContextHolder.getBean(beanName);
        Class<?> clazz;
        if (AopUtils.isJdkDynamicProxy(bean)) {
            InvocationHandler handler = Proxy.getInvocationHandler(bean);
            AdvisedSupport advised = (AdvisedSupport) new DirectFieldAccessor(handler)
                    .getPropertyValue("advised");
            clazz = advised.getTargetClass();
        } else {
            clazz = AopUtils.getTargetClass(bean);
        }
        List<String> ms = new ArrayList<>();
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            String methodName = method.getName();
            if (!ms.contains(methodName) && !methodName.contains("$")) {
                ms.add(method.getName());
            }
        }
        // 父类接口方法
        Class<?> superclass = clazz.getSuperclass();
        Method[] superDeclaredMethods = null;
        if (superclass != null) {
            superDeclaredMethods = superclass.getDeclaredMethods();
        }
        if (superDeclaredMethods != null) {
            for (Method superDeclaredMethod : superDeclaredMethods) {
                String methodName = superDeclaredMethod.getName();
                if (!ms.contains(superDeclaredMethod) && !methodName.contains("$")) {
                    ms.add(superDeclaredMethod.getName());
                }
            }
        }
        Collections.sort(ms);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String jsonResponse = mapper.writeValueAsString(Resp.success(ms));

        resp.getWriter().write(jsonResponse);
    }
}