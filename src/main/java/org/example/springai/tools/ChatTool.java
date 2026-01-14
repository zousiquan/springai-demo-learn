package org.example.springai.tools;


import org.example.springai.model.User;
import org.example.springai.model.WeatherInfo;
import org.example.springai.service.UserService;
import org.example.springai.service.WeatherService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatTool {

    @Autowired
    UserService userService;

    @Autowired
    WeatherService weatherService;

    @Tool(description = "用户注册")
    public String register(@ToolParam(description = "用户名") String username,
                           @ToolParam(description = "密码") String password){
        System.out.println("注册===================================");
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setStatus("ACTIVE");
        userService.createUser(user);
        return "注册完成";
    }


    @Tool(description = "获取天气")
    public WeatherInfo weather(@ToolParam(description = "城市") String city){
        WeatherInfo weatherByCity = weatherService.getWeatherByCity(city);
        return weatherByCity;
    }
}
