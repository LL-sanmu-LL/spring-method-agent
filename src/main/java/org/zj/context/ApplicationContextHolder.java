package org.zj.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class ApplicationContextHolder {

    private static ApplicationContext applicationContext;

    public static void intercept(AbstractApplicationContext context) {
        applicationContext = context;
    }

    public static String[] getBeans() {
        return applicationContext.getBeanDefinitionNames();
    }

    public static ApplicationContext get() {
        return applicationContext;
    }

    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }
}