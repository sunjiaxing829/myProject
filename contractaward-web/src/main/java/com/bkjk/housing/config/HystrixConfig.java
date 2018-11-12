package com.bkjk.housing.config;

import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableHystrix
@EnableCircuitBreaker
public class HystrixConfig {
   
}