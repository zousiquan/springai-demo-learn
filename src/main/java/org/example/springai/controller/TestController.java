package org.example.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RestController
public class TestController {


    @Autowired
    ChatClient chatClient;

//    public TestController(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder.build();
//    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello Spring Ai!";
    }

    @RequestMapping("/text")
    public String index(@RequestParam(value = "message", defaultValue = "讲个笑话") String message,
                       @RequestParam(value = "conversationId", defaultValue = "") String conversationId) {
        // 可以将conversationId用于会话管理，如会话历史存储、上下文维护等
        System.out.println("Received conversationId: " + conversationId);
        
        return chatClient.prompt()
                .user(message + "/no_think")
                .system(p->{
                    p.param("current_data", LocalDateTime.now().toString());
                    p.param("chat_memory_conversation_id", conversationId);
                })
                .call()
                .content();
    }

    @RequestMapping(value = "/stream")
    public Flux<String> index1(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        return chatClient.prompt()
                // 用户提示词
                .user(message)
                .system(p->p.param("current_data", LocalDateTime.now().toString()))
                .stream()
                .content();
    }
}
