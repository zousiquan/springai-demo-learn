package org.example.springai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 天气服务配置类
 */
@Configuration
public class WeatherConfig {
    
    /**
     * 配置RestTemplate用于调用外部天气API
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 