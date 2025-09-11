package com.snnnn.mcp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.aop.support.AopUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import org.springframework.ai.tool.annotation.ToolParam;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ToolDiscoveryService {

    @Autowired
    private ApplicationContext applicationContext;

    public Map<String, Object> listTools() {
        Map<String, Object> result = new HashMap<>();
        Map<String, List<Map<String, Object>>> groupedTools = new HashMap<>();
        
        try {
            String[] beanNames = applicationContext.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                Object bean = applicationContext.getBean(beanName);
                Class<?> targetClass = AopUtils.getTargetClass(bean);
                Method[] methods = targetClass.getMethods();
                for (Method method : methods) {
                    if (hasToolAnnotation(method)) {
                        Map<String, Object> tool = new HashMap<>();
                        tool.put("name", method.getName());
                        tool.put("bean", beanName);
                        tool.put("className", targetClass.getSimpleName());
                        tool.put("methodSignature", method.toString());
                        tool.put("returnType", method.getReturnType().getSimpleName());
                        tool.put("parameters", getMethodParameters(method));
                        tool.put("requestPath", getRequestPath(method));
                        tool.put("httpMethod", getHttpMethod(method));
                        tool.put("description", getToolDescription(method));

                        groupedTools.computeIfAbsent(targetClass.getSimpleName(), k -> new ArrayList<>()).add(tool);
                    }
                }
            }
            result.put("groupedTools", groupedTools);
            
            // 添加 MCP 任务修订文档
            Map<String, Object> mcpTaskRevisions = scanMcpTaskRevisions();
            if (!mcpTaskRevisions.isEmpty()) {
                result.put("mcpTaskRevisions", mcpTaskRevisions);
            }
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
            Class<?> type = paramTypes[i];
            String baseName = (i < paramNames.length ? paramNames[i] : ("param" + (i + 1)));

            if (isSimpleType(type)) {
                Map<String, Object> param = new HashMap<>();
                param.put("name", baseName);
                param.put("type", type.getSimpleName());
                param.put("fullType", type.getName());
                param.put("required", true);
                param.put("example", "");
                params.add(param);
            } else {
                // 仅展开带有 @ToolParam 的字段
                for (var field : type.getDeclaredFields()) {
                    if (!field.isAnnotationPresent(ToolParam.class)) {
                        continue;
                    }
                    Map<String, Object> sub = new HashMap<>();
                    sub.put("name", baseName + "." + field.getName());
                    sub.put("type", field.getType().getSimpleName());
                    sub.put("fullType", field.getType().getName());
                    sub.put("required", true);
                    // 从注解中带出描述
                    ToolParam tp = field.getAnnotation(ToolParam.class);
                    sub.put("description", tp != null ? tp.description() : "");
                    sub.put("example", "");
                    params.add(sub);
                }
            }
        }
        return params;
    }

    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type.equals(String.class)
                || Number.class.isAssignableFrom(type)
                || type.equals(Boolean.class)
                || type.equals(Character.class)
                || type.getName().startsWith("java.time.")
                || type.getName().startsWith("java.lang.");
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
            org.springframework.web.bind.annotation.PostMapping postMapping = method.getAnnotation(org.springframework.web.bind.annotation.PostMapping.class);
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
                        org.springframework.web.bind.annotation.RequestMapping methodMapping = method.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
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
        
        if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class)) {
            return "POST";
        }
        
        if (method.isAnnotationPresent(PutMapping.class)) {
            return "PUT";
        }
        
        if (method.isAnnotationPresent(DeleteMapping.class)) {
            return "DELETE";
        }
        
        // 检查@RequestMapping
        org.springframework.web.bind.annotation.RequestMapping requestMapping = method.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
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

    /**
     * 扫描 MCP task revision 文档文件夹，读取工具使用说明
     */
    private Map<String, Object> scanMcpTaskRevisions() {
        Map<String, Object> revisions = new HashMap<>();
        try {
            java.nio.file.Path docPath = java.nio.file.Paths.get("doc", "MCP task revision");
            if (java.nio.file.Files.exists(docPath)) {
                java.nio.file.Files.list(docPath)
                    .filter(path -> path.toString().endsWith(".md"))
                    .forEach(path -> {
                        try {
                            String fileName = path.getFileName().toString();
                            String toolName = fileName.replace(".md", "");
                            String content = java.nio.file.Files.readString(path);
                            revisions.put(toolName, content);
                        } catch (Exception e) {
                            // 忽略单个文件读取错误
                        }
                    });
            }
        } catch (Exception e) {
            // 忽略文档扫描错误
        }
        return revisions;
    }
}
