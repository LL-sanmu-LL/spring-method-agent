package org.example.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.context.ApplicationContextHolder;
import org.example.vo.Resp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class BeanServlet extends HttpServlet {


    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] beans = ApplicationContextHolder.getBeans();
        List<String> beanList = Arrays.stream(beans).filter(v->!v.contains(".")).sorted().collect(Collectors.toList());
        Resp<List<String>> result = Resp.success(beanList);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String jsonResponse = mapper.writeValueAsString(result);

        resp.getWriter().write(jsonResponse);
    }
}