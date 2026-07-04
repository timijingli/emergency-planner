package com.emergency.controller;

import com.emergency.model.GeoInfo;
import com.emergency.service.AmapService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 地图相关 REST 接口
 */
@RestController
@RequestMapping("/api/map")
public class MapController {

    private final AmapService amapService;

    public MapController(AmapService amapService) {
        this.amapService = amapService;
    }

    /**
     * 地理编码：地址 → 坐标
     * GET /api/map/geocode?address=北京市朝阳区某某路
     */
    @GetMapping("/geocode")
    public GeoInfo geocode(@RequestParam String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("地址不能为空");
        }
        try {
            return amapService.geocode(address.trim());
        } catch (Exception e) {
            throw new RuntimeException("地址解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 逆地理编码：坐标 → 地址
     * GET /api/map/regeocode?lng=116.397&lat=39.908
     */
    @GetMapping("/regeocode")
    public GeoInfo regeocode(@RequestParam double lng, @RequestParam double lat) {
        try {
            return amapService.regeocode(lng, lat);
        } catch (Exception e) {
            throw new RuntimeException("坐标解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 周边安全设施搜索
     * GET /api/map/nearby?lng=116.397&lat=39.908&keywords=避难所|医院|消防站&radius=3000
     */
    @GetMapping("/nearby")
    public List<GeoInfo.NearbyFacility> nearby(
            @RequestParam double lng,
            @RequestParam double lat,
            @RequestParam(defaultValue = "避难所|医院|消防站|派出所") String keywords,
            @RequestParam(defaultValue = "3000") int radius) {
        try {
            return amapService.searchNearby(lng, lat, keywords.trim(), radius);
        } catch (Exception e) {
            throw new RuntimeException("周边搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * IP定位获取城市
     * GET /api/map/city
     */
    @GetMapping("/city")
    public Map<String, String> ipCity() {
        return Map.of("city", amapService.ipCity());
    }

    // ==================== 异常处理 ====================

    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, String> handleBadRequest(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleRuntime(RuntimeException e) {
        return Map.of("error", e.getMessage());
    }
}
