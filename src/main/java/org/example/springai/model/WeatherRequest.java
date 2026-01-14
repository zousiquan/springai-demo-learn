package org.example.springai.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 天气请求数据模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherRequest {
    
    /**
     * 城市名称
     */
    private String city;
    
    /**
     * 国家代码（可选）
     */
    private String countryCode;
    
    /**
     * 语言（可选，默认中文）
     */
    private String language = "zh";
    
    /**
     * 温度单位（celsius/fahrenheit，默认摄氏度）
     */
    private String unit = "celsius";
} 