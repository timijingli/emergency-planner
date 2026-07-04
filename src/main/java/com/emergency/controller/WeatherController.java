package com.emergency.controller;

import com.emergency.model.WeatherInfo;
import com.emergency.service.AmapService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 天气相关 REST 接口（使用高德天气 API）
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final AmapService amapService;

    public WeatherController(AmapService amapService) {
        this.amapService = amapService;
    }

    /**
     * 获取实时天气 + 预警
     * GET /api/weather/now?city=北京
     */
    @GetMapping("/now")
    public WeatherInfo now(@RequestParam String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("城市名不能为空");
        }
        try {
            return amapService.getWeather(city.trim());
        } catch (Exception e) {
            throw new RuntimeException("获取天气失败: " + e.getMessage(), e);
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, String> handleBadRequest(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleRuntime(RuntimeException e) {
        return Map.of("error", e.getMessage());
    }
}
