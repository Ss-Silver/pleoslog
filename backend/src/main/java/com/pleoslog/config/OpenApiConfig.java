package com.pleoslog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pleos Log Backend API")
                        .description("현대자동차그룹 차량용 앱 개발 공모전 - 업무용 승용차 법정 운행기록부 자동화 플랫폼 API 명세")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Pleos Log Team")
                                .email("contact@pleoslog.com")));
    }
}
