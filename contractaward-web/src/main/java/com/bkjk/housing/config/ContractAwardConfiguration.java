package com.bkjk.housing.config;

import com.bkjk.platform.web.resolver.PlatformScopeResolver;
import com.bkjk.housing.ContractAwardApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackageClasses = ContractAwardApplication.class, scopeResolver = PlatformScopeResolver.class)
@EnableAspectJAutoProxy
@EnableDiscoveryClient
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients(basePackages = {"com.bkjk.housing"})
@Import({HystrixConfig.class, HystrixConfig.class})
public class ContractAwardConfiguration {

}