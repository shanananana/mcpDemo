package com.snnnn.mcp.controller;

import com.snnnn.mcp.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/get")
    public Map<String, Object> getWeather(@RequestParam String cityName) {
        return weatherService.getWeather(cityName);
    }

    @GetMapping("/air-quality")
    public Map<String, Object> getAirQuality(@RequestParam String cityName) {
        return weatherService.getAirQuality(cityName);
    }

    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        return weatherService.getInfo();
    }
}


