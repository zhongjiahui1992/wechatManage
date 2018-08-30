package com.uhope.rl.application.config;


import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.uhope.rl.application.constants.SysConstant;
import com.uhope.rl.application.filter.PassportFilter;
import com.uhope.rl.application.filter.ServiceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;

/**
 * Spring MVC 配置
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    /**
     * 当前激活的配置文件
     */
    @Value("${spring.profiles.active}")
    private String env;

    /**
     * 解决路径资源映射问题
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }


    /**
     * 使用fastJson代替Jackjson解析JSON数据
     *
     * @return
     */
    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        /*
         * 转换为JSON字符串，默认:
         * WriteNullListAsEmpty    List字段如果为null,输出为[],而非null
         * WriteNullStringAsEmpty  字符类型字段如果为null,输出为”“,而非null
         * WriteMapNullValue       是否输出值为null的字段,默认为false
         */
        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat);
        fastConverter.setFastJsonConfig(fastJsonConfig);
        fastConverter.setDefaultCharset(Charset.forName(SysConstant.CHAR_SET));
        HttpMessageConverter<?> converter = fastConverter;
        return new HttpMessageConverters(converter);
    }

    /**
     * 这个Filter 解决页面跨域访问问题
     */
    @Bean
    public FilterRegistrationBean omsFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new ServiceFilter());
        registration.addUrlPatterns("/*");
        // registration.addInitParameter("paramName", "paramValue");
        registration.setName("MainFilter");
        registration.setAsyncSupported(true);
        registration.setOrder(1);
        return registration;
    }

    /**
     * 之所以通过这种形式，是因为PassportFilter类中需要通过@Autowire注解注入一些属性,因此交给Spring管理
     *
     * @return
     */
    @Bean
    public PassportFilter getPassportFilter() {
        return new PassportFilter(env);
    }

    /**
     * 鉴权Filter
     */
    @Bean
    public FilterRegistrationBean passportFilter(PassportFilter passportFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(passportFilter);
        registration.addUrlPatterns("/*");
        registration.setName("PassportFilter");
        registration.setOrder(2);

        return registration;
    }

}
