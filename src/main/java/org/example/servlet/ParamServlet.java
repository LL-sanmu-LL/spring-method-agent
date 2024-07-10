package org.example.servlet;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.context.ApplicationContextHolder;
import org.example.util.ParamUtils;
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
    static {
        //都设置为不可见，即不会被序列化和反序列化。
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        //类的字段是可见的，即会被序列化和反序列化。
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

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
                        result = ParamUtils.getInterfaceInputJsonString(clazz.getName(), method.getName());
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