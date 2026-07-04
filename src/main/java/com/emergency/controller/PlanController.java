package com.emergency.controller;

import com.emergency.model.GeoInfo;
import com.emergency.model.PlanRequest;
import com.emergency.model.PlanResponse;
import com.emergency.model.WeatherInfo;
import com.emergency.service.AmapService;
import com.emergency.service.DeepSeekService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST 接口 — 接收前端请求，返回 AI 生成的预案
 */
@RestController
@RequestMapping("/api")
public class PlanController {

    private static final Logger log = LoggerFactory.getLogger(PlanController.class);

    @Value("${amap.js-api-key}")
    private String amapJsApiKey;

    private final DeepSeekService deepSeekService;
    private final AmapService amapService;

    public PlanController(DeepSeekService deepSeekService,
                          AmapService amapService) {
        this.deepSeekService = deepSeekService;
        this.amapService = amapService;
    }

    /**
     * 生成应急预案（增强版：自动注入天气+位置+周边设施）
     *
     * POST /api/generate-plan
     * Body: { "scenario": "...", "extraInfo": "...", "address": "...", "lng": 116.4, "lat": 39.9, "city": "北京" }
     */
    @PostMapping("/generate-plan")
    public PlanResponse generatePlan(@RequestBody PlanRequest request) {
        // 参数校验
        if (request.getScenario() == null || request.getScenario().isBlank()) {
            throw new IllegalArgumentException("场景描述不能为空！");
        }

        try {
            // ---- 1. 收集上下文信息 ----
            GeoInfo geoInfo = null;
            List<GeoInfo.NearbyFacility> facilities = List.of();
            WeatherInfo weatherInfo = null;
            String forecastSummary = "";

            // 定位：有坐标用坐标，有地址用地址，都没有用IP
            if (request.getLng() != null && request.getLat() != null) {
                try {
                    geoInfo = amapService.regeocode(request.getLng(), request.getLat());
                    facilities = amapService.searchNearby(
                            request.getLng(), request.getLat(), "避难所|医院|消防站|派出所|安全出口", 3000);
                } catch (Exception e) {
                    log.warn("获取地理信息失败: {}", e.getMessage());
                }
            } else if (request.getAddress() != null && !request.getAddress().isBlank()) {
                try {
                    geoInfo = amapService.geocode(request.getAddress().trim());
                    facilities = amapService.searchNearby(
                            geoInfo.getLng(), geoInfo.getLat(), "避难所|医院|消防站|派出所|安全出口", 3000);
                } catch (Exception e) {
                    log.warn("地理编码失败: {}", e.getMessage());
                }
            }

            // 确定城市名（优先用户指定 → 地图反查 → IP定位）
            String city = request.getCity();
            if ((city == null || city.isBlank()) && geoInfo != null) {
                city = !geoInfo.getCity().isBlank() ? geoInfo.getCity() : null;
            }
            if (city == null || city.isBlank()) {
                city = amapService.ipCity();
            }

            // 天气（使用高德天气 API，无需额外 Key）
            try {
                weatherInfo = amapService.getWeather(city);
                forecastSummary = ""; // 高德 base 接口不含逐小时预报
            } catch (Exception e) {
                log.warn("获取天气失败: {}", e.getMessage());
            }

            // ---- 2. 组装增强上下文 → 调用 AI ----
            String enrichedContext = buildEnrichedContext(request, geoInfo, facilities, weatherInfo, forecastSummary);

            return deepSeekService.generatePlan(
                    request.getScenario().trim(),
                    enrichedContext
            );
        } catch (Exception e) {
            log.error("生成预案失败", e);
            throw new RuntimeException("生成预案失败: " + e.getMessage(), e);
        }
    }

    /**
     * 拼装增强上下文：把位置、天气、周边设施打包成结构化的附加信息
     */
    private String buildEnrichedContext(
            PlanRequest request,
            GeoInfo geoInfo,
            List<GeoInfo.NearbyFacility> facilities,
            WeatherInfo weather,
            String forecast) {

        StringBuilder ctx = new StringBuilder();

        // 用户原始附加信息
        if (request.getExtraInfo() != null && !request.getExtraInfo().isBlank()) {
            ctx.append("## 用户附加信息\n").append(request.getExtraInfo()).append("\n\n");
        }

        // 位置信息
        if (geoInfo != null) {
            ctx.append("## 事发位置\n");
            ctx.append("- 地址：").append(geoInfo.getAddress()).append("\n");
            if (!geoInfo.getCity().isBlank()) ctx.append("- 城市：").append(geoInfo.getCity()).append("\n");
            if (!geoInfo.getDistrict().isBlank()) ctx.append("- 区县：").append(geoInfo.getDistrict()).append("\n");
            ctx.append("- 坐标：").append(geoInfo.getLng()).append(",").append(geoInfo.getLat()).append("\n");
            ctx.append("\n");
        }

        // 周边安全设施
        if (!facilities.isEmpty()) {
            ctx.append("## 周边安全设施（3公里范围内）\n");
            for (GeoInfo.NearbyFacility f : facilities) {
                ctx.append("- ").append(f.getName())
                   .append("（").append(f.getType()).append("）")
                   .append("，距离约").append(f.getDistance());
                if (f.getAddress() != null && !f.getAddress().isBlank()) {
                    ctx.append("，地址：").append(f.getAddress());
                }
                ctx.append("\n");
            }
            ctx.append("\n");
        }

        // 天气
        if (weather != null) {
            ctx.append("## 当前天气\n");
            ctx.append("- 城市：").append(weather.getCity()).append("\n");
            ctx.append("- 天气：").append(weather.getWeather()).append("\n");
            ctx.append("- 温度：").append(weather.getTemperature())
               .append("（体感 ").append(weather.getFeelsLike()).append("）\n");
            ctx.append("- 风力：").append(weather.getWindDirection())
               .append(" ").append(weather.getWindScale())
               .append("（").append(weather.getWindSpeed()).append("）\n");
            ctx.append("- 湿度：").append(weather.getHumidity()).append("\n");
            ctx.append("- 能见度：").append(weather.getVisibility()).append("\n");
            if (!forecast.isEmpty()) {
                ctx.append("- 未来趋势：").append(forecast).append("\n");
            }
            ctx.append("\n");

            // 灾害预警
            if (weather.getWarnings() != null && !weather.getWarnings().isEmpty()) {
                ctx.append("## ⚠️ 当前生效的灾害预警\n");
                for (WeatherInfo.Warning w : weather.getWarnings()) {
                    ctx.append("- 【").append(w.getLevel()).append("】")
                       .append(w.getType()).append("预警：")
                       .append(w.getTitle()).append("\n");
                    ctx.append("  ").append(w.getDescription()).append("\n");
                    ctx.append("  发布方：").append(w.getSender())
                       .append("，时间：").append(w.getPubTime()).append("\n");
                }
                ctx.append("\n");
            }
        }

        return ctx.toString();
    }

    /**
     * 健康检查接口
     * GET /api/health
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "message", "应急预案生成器运行中");
    }

    /**
     * 返回前端需要的配置（高德 JS API Key）
     */
    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of("amapJsApiKey", amapJsApiKey);
    }

    // ==================== 全局异常处理 ====================

    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, String> handleBadRequest(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleRuntime(RuntimeException e) {
        return Map.of("error", e.getMessage());
    }
}
