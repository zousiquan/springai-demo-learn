package org.example.springai.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 天气信息数据模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherInfo {
    
    /**
     * 城市名称
     */
    private String city;
    
    /**
     * 温度（摄氏度）
     */
    private Double temperature;
    
    /**
     * 湿度（百分比）
     */
    private Integer humidity;
    
    /**
     * 天气描述
     */
    private String description;
    
    /**
     * 风速（米/秒）
     */
    private Double windSpeed;
    
    /**
     * 风向
     */
    private String windDirection;
    
    /**
     * 气压（百帕）
     */
    private Double pressure;
    
    /**
     * 能见度（公里）
     */
    private Double visibility;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 天气图标代码
     */
    private String weatherIcon;
} 