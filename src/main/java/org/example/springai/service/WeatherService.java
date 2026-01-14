package org.example.springai.service;

import org.example.springai.model.WeatherInfo;
import org.example.springai.model.WeatherRequest;

/**
 * 天气服务接口
 */
public interface WeatherService {
    
    /**
     * 获取当前天气信息
     * @param request 天气请求参数
     * @return 天气信息
     */
    WeatherInfo getCurrentWeather(WeatherRequest request);
    
    /**
     * 根据城市名称获取天气信息
     * @param city 城市名称
     * @return 天气信息
     */
    WeatherInfo getWeatherByCity(String city);
    
    /**
     * 获取天气预报（未来几天）
     * @param city 城市名称
     * @param days 天数（1-7天）
     * @return 天气预报列表
     */
    java.util.List<WeatherInfo> getWeatherForecast(String city, int days);
} 