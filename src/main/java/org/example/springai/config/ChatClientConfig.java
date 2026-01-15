package org.example.springai.config;

import io.modelcontextprotocol.client.McpSyncClient;
import org.example.springai.controller.WeatherController;
import org.example.springai.tools.ChatTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatClientConfig {

//    @Bean
//    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
//        return chatClientBuilder.build();
//    }

    @Value("${ai.type}")
    private String apiType;

    // 业务记忆存储
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory
                .builder()
                .maxMessages(20)
                .build();
    }


    @Bean("chatClient")
    public ChatClient chatClient(OpenAiChatModel openAiChatModel,
                                 ChatMemory chatMemory,
                                 ChatTool chatTool,
                                 WeatherController weatherController) {
            return ChatClient.builder(openAiChatModel)
                    //系统提示词
                    .defaultSystem("""
                            ##角色
                            您是观风科技软件公司的客户经理，请以友好的方式来回复。
                            您正在通过在线聊天系统与客户互动。
                            今天的日期是 {current_data}
                            """)
                    .defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build(),
                            new SimpleLoggerAdvisor())
                    .defaultTools(chatTool,weatherController)
                    .build();

    }


    @Bean("ragChatClient")
    public ChatClient ragChatClient(OpenAiChatModel openAiChatModel,
                                 ChatMemory chatMemory,
                                 ChatTool chatTool,
                                 WeatherController weatherController) {
            return ChatClient.builder(openAiChatModel)
                    //系统提示词
                    .defaultSystem("""
                            ## 角色
                            您是一个个人的知识库助手，专门负责基于内部文档和资料进行准确回答。
                            
                            ## 能力
                            - 只能基于提供的上下文信息回答问题
                            - 不能编造或推测超出文档范围的信息
                            - 当文档中没有相关信息时，明确告知用户无法找到答案
                            
                            ## 回答要求
                            - 严格依据文档内容，不得添加个人理解
                            - 保持专业、简洁、准确的回答风格
                            - 如遇多个相关文档，整合信息给出综合回答
                            """)
                    .defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build(),
                            new SimpleLoggerAdvisor())
                    .defaultTools(chatTool,weatherController)
                    .build();

    }


    @Bean("mcpChatClient")
    public ChatClient mcpChatClient(OpenAiChatModel openAiChatModel,
                                    ChatMemory chatMemory,
                                    McpSyncClient mcpSyncClient
                                    ) {
        ChatClient.Builder builder = ChatClient.builder(openAiChatModel).defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build(),
                new SimpleLoggerAdvisor());
        if(mcpSyncClient != null){
            builder.defaultToolCallbacks(SyncMcpToolCallbackProvider.syncToolCallbacks(List.of(mcpSyncClient)));
        }
        return builder.build();

    }

}
