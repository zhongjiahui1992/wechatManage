package com.uhope.rl.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by zhongjiahui on 2018/02/07
 */
@Configuration
@EnableSwagger2
public class Swagger {
    /**
     *basePackage对应的是controller层所在的包路径，及web包所在的路径
     */
    @Bean
    public Docket creatRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.uhope.rl.application.web"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * API访问路径：/swagger-ui.html
     * title: 模块名称
     * description: 模块描述
     * termsOfServiceUrl:条款地址(不可见)
     * Contact：开发者姓名、urll、开发者邮箱
     * version:版本
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("【微信】应用程序项目模板")
                .description("河长制-应用程序项目模板提供的服务接口")
                .termsOfServiceUrl("http://www.uhope.com")
                .contact(new Contact("zhongjiahui", "http://www.uhope.com", "zhongjiahui@uhope.com"))
                .version("1.0")
                .build();
    }
}
