package com.snnnn.mcp.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ToolConfig
 *
 * 自动注册：扫描所有 Bean，凡是包含 @Tool 的方法，统一注册为 MCP 工具。
 * 这样新增/删除工具时无需修改本类。
 */
@Component
public class ToolConfig implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public ToolCallbackProvider myTools() {
        List<Object> toolHolders = new ArrayList<>();
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            // 仅当该 bean 的任一方法存在 @Tool 注解时，才加入注册列表
            if (hasToolMethod(bean)) {
                toolHolders.add(bean);
            }
        }
        return MethodToolCallbackProvider.builder()
                .toolObjects(toolHolders.toArray())
                .build();
    }

    private boolean hasToolMethod(Object bean) {
        try {
            Class<?> clazz = bean.getClass();
            for (var m : clazz.getMethods()) {
                if (m.getAnnotation((Class) Class.forName("org.springframework.ai.tool.annotation.Tool")) != null) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }
}
