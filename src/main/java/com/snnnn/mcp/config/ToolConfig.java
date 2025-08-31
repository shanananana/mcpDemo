package com.snnnn.mcp.config;

import com.snnnn.mcp.service.WeatherService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * ToolConfig
 *
 * @author zj
 * @since 2025/04/22 21:08
 */
@Component
public class ToolConfig {
    @Bean
    public ToolCallbackProvider myTools(WeatherService weatherService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherService)
                .build();
    }
}
