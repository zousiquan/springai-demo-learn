package org.example.springai.config;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

@Configuration
public class CusMcpClientConfig {
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(McpSyncClient.class)
    public McpSyncClient mcpClient() {

        ServerParameters stdioParams;

        if (isWindows()) {
            // Windows: npx is a batch file and requires cmd.exe to execute
            System.out.println("Detected Windows OS - using cmd.exe wrapper for npx");
            var winArgs = new ArrayList<>(Arrays.asList(
                    "/c", "npx", "-y", "@modelcontextprotocol/server-filesystem", "target"));
            stdioParams = ServerParameters.builder("cmd.exe")
                    .args(winArgs)
                    .build();
        } else {
            // Linux/Mac: npx can be executed directly
            System.out.println("Detected Unix-like OS - using npx directly");
            stdioParams = ServerParameters.builder("npx")
                    .args("-y", "@modelcontextprotocol/server-filesystem", "target")
                    .build();
        }

        var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams, McpJsonMapper.createDefault()))
                .requestTimeout(Duration.ofSeconds(30))
                .build();

        var init = mcpClient.initialize();
        System.out.println("MCP Initialized: " + init);

        return mcpClient;
    }

    /**
     * Detects if the current operating system is Windows.
     *
     * @return true if running on Windows, false otherwise
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
