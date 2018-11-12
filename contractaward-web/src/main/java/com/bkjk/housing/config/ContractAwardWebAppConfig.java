package com.bkjk.housing.config;

import com.bkjk.housing.common.interceptor.LoginInterceptor;
import com.bkjk.platform.web.WebMvcConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Configuration
public class ContractAwardWebAppConfig extends WebMvcConfiguration {


    @Bean
    public LoginInterceptor loginInteceptor() {
        return new LoginInterceptor();
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInteceptor());
        super.addInterceptors(registry);
    }

}