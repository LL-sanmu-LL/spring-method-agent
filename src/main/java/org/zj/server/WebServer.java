package org.zj.server;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.zj.servlet.*;

public class WebServer {
    public static void start() {
        Tomcat tomcat = new Tomcat();

        final Connector connector = new Connector();
        connector.setPort(8100);
        tomcat.getService().addConnector(connector);

        Context ctx = tomcat.addContext("", null);
        Tomcat.addServlet(ctx, "index", new IndexServlet());
        Tomcat.addServlet(ctx, "bean", new BeanServlet());
        Tomcat.addServlet(ctx, "methods", new MethodsServlet());
        Tomcat.addServlet(ctx, "param", new ParamServlet());
        Tomcat.addServlet(ctx, "request", new RequestServlet());
        ctx.addServletMappingDecoded("/", "index");
        ctx.addServletMappingDecoded("/testing/services", "bean");
        ctx.addServletMappingDecoded("/testing/methods", "methods");
        ctx.addServletMappingDecoded("/testing/load", "param");
        ctx.addServletMappingDecoded("/testing/request", "request");

        try {
            System.out.println("Starting Tomcat...");
            tomcat.start();
            System.out.println("Tomcat started on port 8100");
        } catch (LifecycleException e) {
            System.err.println("Tomcat startup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        start();
    }
}