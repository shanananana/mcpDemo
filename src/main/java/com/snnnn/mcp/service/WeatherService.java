package com.snnnn.mcp.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * WeatherService
 *
 * @author zj
 * @since 2025/04/22 21:11
 */
@Service
@RestController
@RequestMapping("/weather")
public class WeatherService {

    @Tool(description = "根据城市名称获取天气预报信息")
    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = "application/json")
    public Map<String, Object> getWeather(@ToolParam(description = "城市") @RequestParam String cityName) {
        Map<String, Object> response = new HashMap<>();
        response.put("city", cityName);
        response.put("temperature", 15);
        response.put("unit", "°C");
        response.put("description", "阳光明媚");
        response.put("humidity", 65);
        response.put("windSpeed", 3.2);
        response.put("windUnit", "m/s");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("status", "success");
        response.put("message", cityName + "天气阳光明媚222～");
        return response;
    }

    @Tool(description = "根据城市名称获取空气质量")
    @RequestMapping(value = "/air-quality", method = RequestMethod.GET, produces = "application/json")
    public Map<String, Object> getAirQuality(@ToolParam(description = "城市") @RequestParam String cityName) {
        Map<String, Object> response = new HashMap<>();
        response.put("city", cityName);
        response.put("aqi", 45);
        response.put("level", "优");
        response.put("pm25", 25);
        response.put("pm10", 35);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("status", "success");
        response.put("message", cityName + "空气质量很好 by: zj222～");
        return response;
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET, produces = "application/json")
    public Map<String, Object> getInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("server", "Java MCP Weather Service");
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
            "GET /weather/get?cityName=城市名", "获取指定城市天气",
            "GET /weather/air-quality?cityName=城市名", "获取指定城市空气质量",
            "GET /weather/info", "获取服务器信息"
        ));
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }

}
