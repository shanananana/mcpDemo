package com.snnnn.mcp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
        
        // 尝试获取真实的参数名
        try {
            // 使用反射获取参数名（需要编译时保留参数名）
            java.lang.reflect.Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                String paramName = parameters[i].getName();
                // 如果参数名是合成的（如 arg0, arg1），则使用默认命名
                if (paramName.startsWith("arg") || paramName.equals("param" + (i + 1))) {
                    // 尝试从注解中获取参数名
                    paramName = getParameterNameFromAnnotation(parameters[i]);
                    if (paramName == null) {
                        paramName = "param" + (i + 1);
                    }
                }
                paramNames[i] = paramName;
            }
        } catch (Exception e) {
            // 如果获取失败，使用默认命名
            for (int i = 0; i < paramTypes.length; i++) {
                paramNames[i] = "param" + (i + 1);
            }
        }
        
        return paramNames;
    }
    
    private String getParameterNameFromAnnotation(java.lang.reflect.Parameter parameter) {
        try {
            // 检查@RequestParam注解
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            if (requestParam != null && !requestParam.value().isEmpty()) {
                return requestParam.value();
            }
            
            // 检查@PathVariable注解
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            if (pathVariable != null && !pathVariable.value().isEmpty()) {
                return pathVariable.value();
            }
            
            // 检查@RequestBody注解（通常用于POST请求的JSON参数）
            RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
            if (requestBody != null) {
                // 对于@RequestBody，通常参数名就是字段名
                return parameter.getName();
            }
            
        } catch (Exception e) {
            // 忽略异常
        }
        
        return null;
    }

    private String getRequestPath(Method method) {
        StringBuilder path = new StringBuilder();
        
        // 获取类级别的@RequestMapping路径
        Class<?> clazz = method.getDeclaringClass();
        RequestMapping classMapping = clazz.getAnnotation(RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            path.append(classMapping.value()[0]);
        }
        
        // 获取方法级别的路径注解
        // 检查@GetMapping
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null && getMapping.value().length > 0) {
            path.append(getMapping.value()[0]);
        } else {
            // 检查@PostMapping
            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            if (postMapping != null && postMapping.value().length > 0) {
                path.append(postMapping.value()[0]);
            } else {
                // 检查@PutMapping
                PutMapping putMapping = method.getAnnotation(PutMapping.class);
                if (putMapping != null && putMapping.value().length > 0) {
                    path.append(putMapping.value()[0]);
                } else {
                    // 检查@DeleteMapping
                    DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
                    if (deleteMapping != null && deleteMapping.value().length > 0) {
                        path.append(deleteMapping.value()[0]);
                    } else {
                        // 检查@RequestMapping
                        RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
                        if (methodMapping != null && methodMapping.value().length > 0) {
                            path.append(methodMapping.value()[0]);
                        } else {
                            // 如果没有找到任何路径注解，使用方法名
                            path.append("/").append(method.getName());
                        }
                    }
                }
            }
        }
        
        // 确保路径以/开头
        String result = path.toString();
        if (!result.startsWith("/")) {
            result = "/" + result;
        }
        
        return result;
    }

    private String getHttpMethod(Method method) {
        // 检查方法上的HTTP注解
        if (method.isAnnotationPresent(GetMapping.class)) {
            return "GET";
        }
        
        if (method.isAnnotationPresent(PostMapping.class)) {
            return "POST";
        }
        
        if (method.isAnnotationPresent(PutMapping.class)) {
            return "PUT";
        }
        
        if (method.isAnnotationPresent(DeleteMapping.class)) {
            return "DELETE";
        }
        
        // 检查@RequestMapping
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null && requestMapping.method().length > 0) {
            return requestMapping.method()[0].name();
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
