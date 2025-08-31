package com.snnnn.mcp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RestController
@RequestMapping("/api/mcp")
public class ToolDiscoveryService {

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("/tools")
    public Map<String, Object> getTools() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> tools = new ArrayList<>();
        
        try {
            String[] beanNames = applicationContext.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                Object bean = applicationContext.getBean(beanName);
                Method[] methods = bean.getClass().getMethods();
                for (Method method : methods) {
                    if (hasToolAnnotation(method)) {
                        Map<String, Object> tool = new HashMap<>();
                        tool.put("name", method.getName());
                        tool.put("bean", beanName);
                        tool.put("className", bean.getClass().getSimpleName());
                        tool.put("methodSignature", method.toString());
                        tool.put("returnType", method.getReturnType().getSimpleName());
                        tool.put("parameters", getMethodParameters(method));
                        tool.put("requestPath", getRequestPath(method));
                        tool.put("httpMethod", getHttpMethod(method));
                        tool.put("description", getToolDescription(method));
                        tools.add(tool);
                    }
                }
            }
            result.put("tools", tools);
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    private List<Map<String, Object>> getMethodParameters(Method method) {
        List<Map<String, Object>> params = new ArrayList<>();
        Class<?>[] paramTypes = method.getParameterTypes();
        String[] paramNames = getParameterNames(method);
        
        for (int i = 0; i < paramTypes.length; i++) {
            Map<String, Object> param = new HashMap<>();
            param.put("name", paramNames[i]);
            param.put("type", paramTypes[i].getSimpleName());
            param.put("fullType", paramTypes[i].getName());
            param.put("required", true);
            params.add(param);
        }
        return params;
    }

    private String[] getParameterNames(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        String[] paramNames = new String[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            paramNames[i] = "param" + (i + 1);
        }
        return paramNames;
    }

    private String getRequestPath(Method method) {
        // 尝试从@GetMapping等注解中获取路径
        try {
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            if (getMapping != null && getMapping.value().length > 0) {
                return getMapping.value()[0];
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "/" + method.getName();
    }

    private String getHttpMethod(Method method) {
        // 检查方法上的HTTP注解
        if (method.isAnnotationPresent(GetMapping.class)) {
            return "GET";
        }
        return "POST"; // 默认
    }

    private String getToolDescription(Method method) {
        try {
            Class<? extends Annotation> toolClass = (Class<? extends Annotation>) Class.forName("org.springframework.ai.tool.annotation.Tool");
            Object annotation = method.getAnnotation(toolClass);
            if (annotation != null) {
                // 尝试获取description属性
                Method descMethod = toolClass.getMethod("description");
                return (String) descMethod.invoke(annotation);
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "工具描述";
    }

    private boolean hasToolAnnotation(Method method) {
        try {
            Class<? extends Annotation> toolClass = (Class<? extends Annotation>) Class.forName("org.springframework.ai.tool.annotation.Tool");
            return method.isAnnotationPresent(toolClass);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
