package org.example.springai.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.springai.model.WeatherInfo;
import org.example.springai.model.WeatherRequest;
import org.example.springai.service.WeatherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 天气服务实现类
 */
@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {
    
    @Value("${weather.api.key:}")
    private String weatherApiKey;
    
    @Value("${weather.api.url:https://api.openweathermap.org/data/2.5}")
    private String weatherApiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();
    
    @Override
    public WeatherInfo getCurrentWeather(WeatherRequest request) {
        log.info("获取天气信息，城市: {}", request.getCity());
        
        // 如果有API密钥，调用真实API
        if (weatherApiKey != null && !weatherApiKey.isEmpty()) {
            return getWeatherFromApi(request);
        }
        
        // 否则返回模拟数据
        return generateMockWeather(request.getCity());
    }
    
    @Override
    public WeatherInfo getWeatherByCity(String city) {
        WeatherRequest request = new WeatherRequest();
        request.setCity(city);
        return getCurrentWeather(request);
    }
    
    @Override
    public List<WeatherInfo> getWeatherForecast(String city, int days) {
        log.info("获取天气预报，城市: {}, 天数: {}", city, days);
        
        List<WeatherInfo> forecast = new ArrayList<>();
        for (int i = 0; i < Math.min(days, 7); i++) {
            WeatherInfo weather = generateMockWeather(city);
            // 为未来日期调整温度
            weather.setTemperature(weather.getTemperature() + random.nextDouble() * 10 - 5);
            forecast.add(weather);
        }
        
        return forecast;
    }
    
    /**
     * 从真实API获取天气数据
     */
    private WeatherInfo getWeatherFromApi(WeatherRequest request) {
        try {
            String url = String.format("%s/weather?q=%s&appid=%s&units=metric&lang=zh_cn",
                    weatherApiUrl, request.getCity(), weatherApiKey);
            
            // 这里需要根据实际的API响应格式来解析
            // 由于不同天气API的响应格式不同，这里提供基础框架
            String response = restTemplate.getForObject(url, String.class);
            log.info("API响应: {}", response);
            
            // 解析响应并转换为WeatherInfo对象
            return parseApiResponse(response, request.getCity());
            
        } catch (Exception e) {
            log.error("调用天气API失败: {}", e.getMessage());
            // 降级到模拟数据
            return generateMockWeather(request.getCity());
        }
    }
    
    /**
     * 解析API响应
     */
    private WeatherInfo parseApiResponse(String response, String city) {
        // 这里需要根据实际使用的天气API来解析响应
        // 示例：OpenWeatherMap API的解析
        WeatherInfo weather = new WeatherInfo();
        weather.setCity(city);
        weather.setUpdateTime(LocalDateTime.now());
        
        // 实际实现中需要解析JSON响应
        // 这里返回模拟数据作为示例
        return generateMockWeather(city);
    }
    
    /**
     * 生成模拟天气数据
     */
    private WeatherInfo generateMockWeather(String city) {
        WeatherInfo weather = new WeatherInfo();
        weather.setCity(city);
        weather.setTemperature(15.0 + random.nextDouble() * 20); // 15-35度
        weather.setHumidity(40 + random.nextInt(40)); // 40-80%
        weather.setWindSpeed(1.0 + random.nextDouble() * 10); // 1-11 m/s
        weather.setPressure(1000.0 + random.nextDouble() * 50); // 1000-1050 hPa
        weather.setVisibility(5.0 + random.nextDouble() * 15); // 5-20 km
        weather.setUpdateTime(LocalDateTime.now());
        
        // 根据温度设置天气描述
        if (weather.getTemperature() > 25) {
            weather.setDescription("晴天");
            weather.setWeatherIcon("01d");
        } else if (weather.getTemperature() > 15) {
            weather.setDescription("多云");
            weather.setWeatherIcon("02d");
        } else {
            weather.setDescription("阴天");
            weather.setWeatherIcon("03d");
        }
        
        // 设置风向
        String[] directions = {"北风", "东北风", "东风", "东南风", "南风", "西南风", "西风", "西北风"};
        weather.setWindDirection(directions[random.nextInt(directions.length)]);
        
        return weather;
    }
} 