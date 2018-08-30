package com.uhope.rl.application.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by zhengdepiao on 2018/2/8.
 * 定时器service注入工具类
 */
@Component
public class ApplicationContextUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    public static Object getBean(String BeanName){
        return applicationContext.getBean(BeanName);
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtil.applicationContext = applicationContext;
    }
}
