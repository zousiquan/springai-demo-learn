# SpringAI 完整教学教程

## 📚 教程概述

本教程将带您深入了解SpringAI框架，通过一个完整的项目实例来学习如何构建AI驱动的Spring Boot应用。项目包含天气查询服务、用户管理系统和AI聊天功能，展示了SpringAI的核心特性和最佳实践。

## 🎯 学习目标

通过本教程，您将学会：
- SpringAI框架的核心概念和架构
- 如何集成不同的AI模型（Ollama、DashScope）
- 使用SpringAI构建智能聊天应用
- 实现AI工具调用和函数调用
- 构建完整的AI驱动Web服务

## 🏗️ 项目架构

```
SpringAI项目/
├── 核心功能模块
│   ├── AI聊天服务 (ChatClient)
│   ├── 天气查询服务 (WeatherService)
│   └── 用户管理系统 (UserService)
├── 技术栈
│   ├── Spring Boot 3.5.0
│   ├── SpringAI 1.0.0
│   ├── MyBatis + MySQL
│   └── Lombok
└── AI模型集成
    ├── Ollama (本地模型)
    └── DashScope (阿里云模型)
```

## 🚀 第一章：SpringAI基础概念

### 1.1 什么是SpringAI？

SpringAI是Spring官方推出的AI应用开发框架，它提供了：
- **统一的AI模型抽象**：支持多种AI服务提供商
- **声明式工具调用**：通过注解轻松实现函数调用
- **流式响应支持**：支持实时流式AI响应
- **记忆管理**：内置对话历史管理
- **提示词工程**：强大的提示词构建和管理

### 1.2 核心组件

#### ChatClient
```java
@Bean
public ChatClient chatClient(OllamaChatModel ollamaChatModel,
                             DashScopeChatModel dashScopeChatModel,
                             ChatMemory chatMemory,
                             ChatTool chatTool,
                             WeatherController weatherController) {
    return ChatClient.builder(ollamaChatModel)
            .defaultSystem("您是观风科技软件公司的客户经理...")
            .defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build())
            .defaultTools(chatTool, weatherController)
            .build();
}
```

#### 工具调用 (Tools)
```java
@Tool(description = "获取天气")
public WeatherInfo weather(@ToolParam(description = "城市") String city) {
    return weatherService.getWeatherByCity(city);
}
```

#### 记忆管理 (Memory)
```java
@Bean
public ChatMemory chatMemory() {
    return MessageWindowChatMemory
            .builder()
            .maxMessages(20)
            .build();
}
```

## 🛠️ 第二章：环境搭建

### 2.1 项目依赖配置

```xml
<properties>
    <java.version>17</java.version>
    <spring-ai.version>1.0.0</spring-ai.version>
</properties>

<dependencies>
    <!-- SpringAI核心依赖 -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-ollama</artifactId>
    </dependency>
    
    <!-- 阿里云DashScope模型 -->
    <dependency>
        <groupId>com.alibaba.cloud.ai</groupId>
        <artifactId>spring-ai-alibaba-starter-dashscope</artifactId>
        <version>1.0.0.1</version>
    </dependency>
    
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

### 2.2 配置文件

```yaml
# application.yml
spring:
  application:
    name: spring-ai-weather-service
  
  # 数据库配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/db_springai
    username: root
    password: 12345678

# AI模型配置
ai:
  type: dashscope  # 或 ollama

# 天气API配置
weather:
  api:
    key: ${WEATHER_API_KEY:}
    url: https://api.openweathermap.org/data/2.5
    timeout: 5000
```

## 🎨 第三章：AI聊天服务实现

### 3.1 基础聊天功能

```java
@RestController
public class TestController {
    
    @Autowired
    ChatClient chatClient;
    
    // 普通文本聊天
    @RequestMapping("/text")
    public String index(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        return chatClient.prompt()
                .user(message + "/no_think")
                .system(p->p.param("current_data", LocalDateTime.now().toString()))
                .call()
                .content();
    }
    
    // 流式聊天
    @RequestMapping(value = "/stream")
    public Flux<String> streamChat(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        return chatClient.prompt()
                .user(message)
                .system(p->p.param("current_data", LocalDateTime.now().toString()))
                .stream()
                .content();
    }
}
```

### 3.2 系统提示词配置

```java
.defaultSystem("""
        ##角色
        您是观风科技软件公司的客户经理，请以友好的方式来回复。
        您正在通过在线聊天系统与客户互动。
        今天的日期是 {current_data}
        """)
```

### 3.3 参数化提示词

```java
.system(p->p.param("current_data", LocalDateTime.now().toString()))
```

## 🔧 第四章：工具调用实现

### 4.1 工具类定义

```java
@Service
public class ChatTool {
    
    @Autowired
    UserService userService;
    
    @Autowired
    WeatherService weatherService;
    
    @Tool(description = "用户注册")
    public String register(@ToolParam(description = "用户名") String username,
                           @ToolParam(description = "密码") String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setStatus("ACTIVE");
        userService.createUser(user);
        return "注册完成";
    }
    
    @Tool(description = "获取天气")
    public WeatherInfo weather(@ToolParam(description = "城市") String city) {
        return weatherService.getWeatherByCity(city);
    }
}
```

### 4.2 控制器工具化

```java
@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    
    @PostMapping("/current")
    @Tool(description = "获取当前天气")
    public ApiResponse<WeatherInfo> getCurrentWeather(@RequestBody WeatherRequest request) {
        // 实现逻辑
    }
    
    @GetMapping("/city/{city}")
    @Tool(description = "根据城市名称获取天气信息")
    public ApiResponse<WeatherInfo> getWeatherByCity(@PathVariable String city) {
        // 实现逻辑
    }
}
```

### 4.3 工具注册

```java
.defaultTools(chatTool, weatherController)
```

## 🌤️ 第五章：天气服务集成

### 5.1 天气数据模型

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherInfo {
    private String city;
    private Double temperature;
    private Integer humidity;
    private String description;
    private Double windSpeed;
    private String windDirection;
    private Double pressure;
    private Double visibility;
    private LocalDateTime updateTime;
    private String weatherIcon;
}
```

### 5.2 天气服务实现

```java
@Service
@Slf4j
public class WeatherService {
    
    public WeatherInfo getCurrentWeather(WeatherRequest request) {
        // 实现天气查询逻辑
        return WeatherInfo.builder()
                .city(request.getCity())
                .temperature(25.5)
                .humidity(65)
                .description("晴天")
                .build();
    }
    
    public WeatherInfo getWeatherByCity(String city) {
        // 根据城市查询天气
        return getCurrentWeather(WeatherRequest.builder().city(city).build());
    }
    
    public List<WeatherInfo> getWeatherForecast(String city, int days) {
        // 获取天气预报
        return Arrays.asList(
            getWeatherByCity(city),
            getWeatherByCity(city),
            getWeatherByCity(city)
        );
    }
}
```

### 5.3 API接口设计

```java
// 获取当前天气
POST /api/weather/current

// 根据城市查询天气
GET /api/weather/city/{city}

// 获取天气预报
GET /api/weather/forecast/{city}?days={days}

// 健康检查
GET /api/weather/health
```

## 👥 第六章：用户管理系统

### 6.1 用户实体类

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

### 6.2 数据访问层

```java
@Mapper
public interface UserMapper {
    
    @Insert("INSERT INTO users (username, password, status) VALUES (#{username}, #{password}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(User user);
    
    @Select("SELECT * FROM users WHERE id = #{id}")
    User selectUserById(Long id);
    
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectUserByUsername(String username);
    
    @Update("UPDATE users SET username = #{username}, password = #{password}, status = #{status} WHERE id = #{id}")
    int updateUser(User user);
    
    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteUserById(Long id);
    
    @Select("SELECT * FROM users")
    List<User> selectAllUsers();
}
```

### 6.3 业务逻辑层

```java
@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public User createUser(User user) {
        // 检查用户名唯一性
        User existingUser = userMapper.selectUserByUsername(user.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus("ACTIVE");
        }
        
        userMapper.insertUser(user);
        log.info("创建用户成功: {}", user.getUsername());
        return user;
    }
    
    // 其他CRUD方法实现...
}
```

## 🔄 第七章：流式响应和记忆管理

### 7.1 流式响应实现

```java
@RequestMapping(value = "/stream")
public Flux<String> streamChat(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
    return chatClient.prompt()
            .user(message)
            .system(p->p.param("current_data", LocalDateTime.now().toString()))
            .stream()
            .content();
}
```

### 7.2 记忆管理配置

```java
@Bean
public ChatMemory chatMemory() {
    return MessageWindowChatMemory
            .builder()
            .maxMessages(20)  // 保留最近20条消息
            .build();
}
```

### 7.3 记忆顾问配置

```java
.defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build())
```

## 🎯 第八章：实际应用场景

### 8.1 智能客服系统

```java
// 系统提示词
.defaultSystem("""
    您是观风科技软件公司的客户经理，请以友好的方式来回复。
    您可以帮助客户：
    1. 查询天气信息
    2. 注册用户账号
    3. 回答产品相关问题
    4. 提供技术支持
    """)
```

### 8.2 多轮对话示例

用户: "我想注册一个账号"
AI: "好的，我来帮您注册账号。请告诉我您想使用的用户名和密码。"

用户: "用户名是testuser，密码是123456"
AI: [调用register工具] "注册完成！您的账号已经成功创建。"

用户: "北京今天天气怎么样？"
AI: [调用weather工具] "北京今天天气晴朗，温度25.5°C，湿度65%，适合外出活动。"

### 8.3 错误处理

```java
try {
    WeatherInfo weather = weatherService.getWeatherByCity(city);
    return ApiResponse.success("获取天气信息成功", weather);
} catch (Exception e) {
    log.error("获取天气信息失败: {}", e.getMessage(), e);
    return ApiResponse.error("获取天气信息失败: " + e.getMessage());
}
```

## 🚀 第九章：部署和优化

### 9.1 生产环境配置

```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  
  datasource:
    url: jdbc:mysql://prod-db:3306/db_springai
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

ai:
  type: dashscope

weather:
  api:
    key: ${WEATHER_API_KEY}
```

### 9.2 Docker部署

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/SpringAI-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 9.3 性能优化

1. **缓存策略**
```java
@Cacheable("weather")
public WeatherInfo getWeatherByCity(String city) {
    // 实现缓存逻辑
}
```

2. **连接池配置**
```yaml
spring:
  datasource:
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
```

3. **AI模型优化**
```java
// 使用更合适的模型参数
.defaultSystem("简洁明了的系统提示词")
```

## 📊 第十章：测试和监控

### 10.1 单元测试

```java
@SpringBootTest
class WeatherServiceTest {
    
    @Autowired
    private WeatherService weatherService;
    
    @Test
    void testGetWeatherByCity() {
        WeatherInfo weather = weatherService.getWeatherByCity("北京");
        assertNotNull(weather);
        assertEquals("北京", weather.getCity());
    }
}
```

### 10.2 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WeatherControllerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testGetWeatherByCity() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
            "/api/weather/city/北京", 
            ApiResponse.class
        );
        assertEquals(200, response.getStatusCodeValue());
    }
}
```

### 10.3 监控和日志

```yaml
# 日志配置
logging:
  level:
    org.example.springai: DEBUG
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# 健康检查
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

## 🎓 第十一章：最佳实践

### 11.1 代码组织

```
src/main/java/org/example/springai/
├── config/          # 配置类
├── controller/      # 控制器
├── service/         # 业务逻辑
├── mapper/          # 数据访问
├── model/           # 实体类
└── tools/           # AI工具
```

### 11.2 异常处理

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return ResponseEntity.ok(ApiResponse.error("系统异常: " + e.getMessage()));
    }
}
```

### 11.3 安全考虑

1. **API密钥管理**
```yaml
weather:
  api:
    key: ${WEATHER_API_KEY:}  # 使用环境变量
```

2. **输入验证**
```java
@Valid
public class WeatherRequest {
    @NotBlank(message = "城市名称不能为空")
    private String city;
}
```

3. **SQL注入防护**
```java
@Select("SELECT * FROM users WHERE username = #{username}")
User selectUserByUsername(String username);
```

## 🔮 第十二章：扩展和进阶

### 12.1 支持更多AI模型

```java
// 支持OpenAI
@Bean
public OpenAiChatModel openAiChatModel() {
    return new OpenAiChatModel(openAiApi);
}

// 支持Azure OpenAI
@Bean
public AzureOpenAiChatModel azureOpenAiChatModel() {
    return new AzureOpenAiChatModel(azureOpenAiApi);
}
```

### 12.2 向量数据库集成

```java
@Bean
public VectorStore vectorStore() {
    return new ChromaVectorStore(chromaClient);
}
```

### 12.3 自定义工具开发

```java
@Component
public class CustomTool {
    
    @Tool(description = "自定义业务工具")
    public String customBusinessLogic(@ToolParam(description = "参数") String param) {
        // 实现自定义业务逻辑
        return "处理结果";
    }
}
```

## 📝 总结

通过本教程，您已经学会了：

1. **SpringAI核心概念**：ChatClient、工具调用、记忆管理
2. **项目架构设计**：模块化、分层架构
3. **AI模型集成**：Ollama、DashScope等
4. **实际应用开发**：天气服务、用户管理、智能聊天
5. **最佳实践**：错误处理、性能优化、安全考虑

### 下一步学习建议

1. 深入学习SpringAI的高级特性
2. 探索更多AI模型和工具
3. 实践大规模AI应用部署
4. 学习AI应用的安全和隐私保护
5. 关注SpringAI的最新更新和社区动态

### 资源链接

- [SpringAI官方文档](https://docs.spring.io/spring-ai/reference/)
- [SpringAI GitHub仓库](https://github.com/spring-projects/spring-ai)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)

---

**恭喜您完成了SpringAI的学习！** 🎉

现在您可以开始构建自己的AI驱动应用了。记住，实践是最好的老师，多动手编码，多尝试不同的场景，您会越来越熟练地使用SpringAI框架。 