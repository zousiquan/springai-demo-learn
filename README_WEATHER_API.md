# 天气API服务

这是一个基于Spring Boot的天气查询服务，提供RESTful API接口来获取天气信息。

## 功能特性

- 🌤️ 获取当前天气信息
- 📅 获取天气预报（1-7天）
- 🏙️ 支持多城市查询
- 🔧 支持真实API和模拟数据
- 📱 提供Web界面演示
- 🛡️ 完善的错误处理和日志记录

## 技术栈

- **Spring Boot 3.5.0** - 主框架
- **Spring Web** - RESTful API
- **Lombok** - 代码简化
- **MySQL** - 数据存储（可选）
- **MyBatis** - 数据访问层（可选）

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+（可选）

### 2. 配置

编辑 `src/main/resources/application.yml` 文件：

```yaml
# 天气API配置
weather:
  api:
    # OpenWeatherMap API密钥（可选）
    key: your_api_key_here
    url: https://api.openweathermap.org/data/2.5
```

### 3. 运行

```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

## API接口

### 1. 获取当前天气

**POST** `/api/weather/current`

请求体：
```json
{
  "city": "北京",
  "countryCode": "CN",
  "language": "zh",
  "unit": "celsius"
}
```

响应：
```json
{
  "code": 200,
  "message": "获取天气信息成功",
  "data": {
    "city": "北京",
    "temperature": 25.5,
    "humidity": 65,
    "description": "晴天",
    "windSpeed": 3.2,
    "windDirection": "东北风",
    "pressure": 1013.2,
    "visibility": 10.5,
    "updateTime": "2024-01-15T14:30:00",
    "weatherIcon": "01d"
  },
  "timestamp": 1705312200000
}
```

### 2. 根据城市查询天气

**GET** `/api/weather/city/{city}`

示例：`GET /api/weather/city/北京`

### 3. 获取天气预报

**GET** `/api/weather/forecast/{city}?days={days}`

示例：`GET /api/weather/forecast/北京?days=5`

### 4. 健康检查

**GET** `/api/weather/health`

## Web界面

访问 `http://localhost:8080` 可以看到天气查询的Web界面，支持：

- 输入城市名称查询天气
- 查看当前天气详情
- 查看5天天气预报
- 响应式设计，支持移动端

## 数据模型

### WeatherInfo（天气信息）

| 字段 | 类型 | 说明 |
|------|------|------|
| city | String | 城市名称 |
| temperature | Double | 温度（摄氏度） |
| humidity | Integer | 湿度（百分比） |
| description | String | 天气描述 |
| windSpeed | Double | 风速（米/秒） |
| windDirection | String | 风向 |
| pressure | Double | 气压（百帕） |
| visibility | Double | 能见度（公里） |
| updateTime | LocalDateTime | 更新时间 |
| weatherIcon | String | 天气图标代码 |

### WeatherRequest（天气请求）

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| city | String | - | 城市名称（必填） |
| countryCode | String | - | 国家代码（可选） |
| language | String | "zh" | 语言 |
| unit | String | "celsius" | 温度单位 |

## 配置说明

### 天气API配置

如果不配置API密钥，服务将使用模拟数据：

```yaml
weather:
  api:
    key: ${WEATHER_API_KEY:}  # 环境变量或留空使用模拟数据
    url: https://api.openweathermap.org/data/2.5
    timeout: 5000
```

### 数据库配置（可选）

如果需要存储天气历史数据，可以配置MySQL：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spring_ai
    username: root
    password: 123456
```

## 扩展功能

### 1. 集成真实天气API

目前支持OpenWeatherMap API，可以扩展支持其他天气服务：

- 和风天气API
- 心知天气API
- AccuWeather API

### 2. 添加缓存

可以使用Redis缓存天气数据，减少API调用：

```java
@Cacheable("weather")
public WeatherInfo getWeatherByCity(String city) {
    // 实现缓存逻辑
}
```

### 3. 添加定时任务

定时更新天气数据：

```java
@Scheduled(fixedRate = 1800000) // 30分钟更新一次
public void updateWeatherData() {
    // 更新天气数据
}
```

## 部署

### Docker部署

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/SpringAI-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 环境变量

```bash
export WEATHER_API_KEY=your_api_key_here
export SPRING_PROFILES_ACTIVE=prod
```

## 故障排除

### 常见问题

1. **API调用失败**
   - 检查API密钥是否正确
   - 检查网络连接
   - 查看日志中的错误信息

2. **数据库连接失败**
   - 检查MySQL服务是否启动
   - 检查数据库配置是否正确
   - 确保数据库用户有足够权限

3. **端口被占用**
   - 修改 `application.yml` 中的端口配置
   - 或者停止占用端口的其他服务

### 日志查看

```bash
# 查看应用日志
tail -f logs/spring-ai.log

# 查看错误日志
grep ERROR logs/spring-ai.log
```

## 贡献

欢迎提交Issue和Pull Request来改进这个项目！

## 许可证

MIT License 