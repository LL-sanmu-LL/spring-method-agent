package org.example.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.example.context.ApplicationContextHolder;
import org.example.util.InterfaceParametersUtils;
import org.example.vo.Resp;
import org.springframework.aop.TargetClassAware;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ParamServlet extends HttpServlet {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HashMap<String, String> hashMap = mapper.readValue(req.getInputStream(), HashMap.class);
        String serviceName = hashMap.get("serviceName");
        String methodName = hashMap.get("methodName");
        Object bean = ApplicationContextHolder.getBean(serviceName);
        Object result = null;
        Class<?> clazz = bean.getClass();
        if(bean instanceof TargetClassAware){
            clazz = ((TargetClassAware) bean).getTargetClass();
        }
        if(clazz != null){
            for (Method method : clazz.getDeclaredMethods()) {
                try {
                    if (methodName.equals(method.getName())) {
                        result = InterfaceParametersUtils.getInterfaceInputJsonString(clazz.getName(), method.getName());
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String jsonResponse = mapper.writeValueAsString(Resp.success(result));

        resp.getWriter().write(jsonResponse);
    }


}