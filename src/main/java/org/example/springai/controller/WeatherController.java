package org.example.springai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.springai.model.ApiResponse;
import org.example.springai.model.WeatherInfo;
import org.example.springai.model.WeatherRequest;
import org.example.springai.service.WeatherService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 天气控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WeatherController {
    
    private final WeatherService weatherService;
    
    /**
     * 获取当前天气信息
     * @param request 天气请求参数
     * @return 天气信息
     */
    @PostMapping("/current")
    @Tool(description = "获取当前天气")
    public ApiResponse<WeatherInfo> getCurrentWeather(@RequestBody WeatherRequest request) {
        try {
            log.info("收到天气查询请求: {}", request);
            WeatherInfo weather = weatherService.getCurrentWeather(request);
            return ApiResponse.success("获取天气信息成功", weather);
        } catch (Exception e) {
            log.error("获取天气信息失败: {}", e.getMessage(), e);
            return ApiResponse.error("获取天气信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据城市名称获取天气信息
     * @param city 城市名称
     * @return 天气信息
     */
    @Tool(description = "根据城市名称获取天气信息")
    @GetMapping("/city/{city}")
    public ApiResponse<WeatherInfo> getWeatherByCity(@PathVariable String city) {
        try {
            log.info("查询城市天气: {}", city);
            WeatherInfo weather = weatherService.getWeatherByCity(city);
            return ApiResponse.success("获取天气信息成功", weather);
        } catch (Exception e) {
            log.error("获取城市天气失败: {}", e.getMessage(), e);
            return ApiResponse.error("获取城市天气失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取天气预报
     * @param city 城市名称
     * @param days 天数（1-7天）
     * @return 天气预报列表
     */
    @GetMapping("/forecast/{city}")
    @Tool(description = "获取城市天气预报")
    public ApiResponse<List<WeatherInfo>> getWeatherForecast(
            @PathVariable String city,
            @RequestParam(defaultValue = "3") int days) {
        try {
            log.info("查询天气预报: {}, 天数: {}", city, days);
            List<WeatherInfo> forecast = weatherService.getWeatherForecast(city, days);
            return ApiResponse.success("获取天气预报成功", forecast);
        } catch (Exception e) {
            log.error("获取天气预报失败: {}", e.getMessage(), e);
            return ApiResponse.error("获取天气预报失败: " + e.getMessage());
        }
    }
    
    /**
     * 健康检查接口
     * @return 服务状态
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("天气服务运行正常", "OK");
    }
} 