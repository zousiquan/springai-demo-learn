package org.example.springai.service;

import org.example.springai.model.WeatherInfo;
import org.example.springai.model.WeatherRequest;
import org.example.springai.service.impl.WeatherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 天气服务测试类
 */
@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {
    
    @InjectMocks
    private WeatherServiceImpl weatherService;
    
    private WeatherRequest testRequest;
    
    @BeforeEach
    void setUp() {
        testRequest = new WeatherRequest();
        testRequest.setCity("北京");
        testRequest.setLanguage("zh");
        testRequest.setUnit("celsius");
    }
    
    @Test
    void testGetCurrentWeather() {
        // 测试获取当前天气
        WeatherInfo weather = weatherService.getCurrentWeather(testRequest);
        
        // 验证返回的天气信息
        assertNotNull(weather);
        assertEquals("北京", weather.getCity());
        assertNotNull(weather.getTemperature());
        assertNotNull(weather.getHumidity());
        assertNotNull(weather.getDescription());
        assertNotNull(weather.getWindSpeed());
        assertNotNull(weather.getWindDirection());
        assertNotNull(weather.getPressure());
        assertNotNull(weather.getVisibility());
        assertNotNull(weather.getUpdateTime());
        assertNotNull(weather.getWeatherIcon());
        
        // 验证数据范围
        assertTrue(weather.getTemperature() >= 15.0 && weather.getTemperature() <= 35.0);
        assertTrue(weather.getHumidity() >= 40 && weather.getHumidity() <= 80);
        assertTrue(weather.getWindSpeed() >= 1.0 && weather.getWindSpeed() <= 11.0);
        assertTrue(weather.getPressure() >= 1000.0 && weather.getPressure() <= 1050.0);
        assertTrue(weather.getVisibility() >= 5.0 && weather.getVisibility() <= 20.0);
    }
    
    @Test
    void testGetWeatherByCity() {
        // 测试根据城市名称获取天气
        WeatherInfo weather = weatherService.getWeatherByCity("上海");
        
        assertNotNull(weather);
        assertEquals("上海", weather.getCity());
        assertNotNull(weather.getTemperature());
        assertNotNull(weather.getDescription());
    }
    
    @Test
    void testGetWeatherForecast() {
        // 测试获取天气预报
        List<WeatherInfo> forecast = weatherService.getWeatherForecast("广州", 5);
        
        assertNotNull(forecast);
        assertEquals(5, forecast.size());
        
        // 验证每一天的天气信息
        for (int i = 0; i < forecast.size(); i++) {
            WeatherInfo dayWeather = forecast.get(i);
            assertNotNull(dayWeather);
            assertEquals("广州", dayWeather.getCity());
            assertNotNull(dayWeather.getTemperature());
            assertNotNull(dayWeather.getDescription());
        }
    }
    
    @Test
    void testGetWeatherForecastWithMaxDays() {
        // 测试获取天气预报（超过最大天数限制）
        List<WeatherInfo> forecast = weatherService.getWeatherForecast("深圳", 10);
        
        assertNotNull(forecast);
        assertEquals(7, forecast.size()); // 应该限制在7天
    }
    
    @Test
    void testWeatherDataConsistency() {
        // 测试天气数据的一致性
        WeatherInfo weather1 = weatherService.getWeatherByCity("杭州");
        WeatherInfo weather2 = weatherService.getWeatherByCity("杭州");
        
        assertNotNull(weather1);
        assertNotNull(weather2);
        assertEquals(weather1.getCity(), weather2.getCity());
        
        // 由于是模拟数据，每次调用可能不同，但基本字段应该存在
        assertNotNull(weather1.getTemperature());
        assertNotNull(weather1.getDescription());
        assertNotNull(weather2.getTemperature());
        assertNotNull(weather2.getDescription());
    }
} 