package com.uhope.rl.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages={"com.uhope"})
public class RlApplicationTemplateApplication {

	public static void main(String[] args) {
		SpringApplication.run(RlApplicationTemplateApplication.class, args);
	}
}
