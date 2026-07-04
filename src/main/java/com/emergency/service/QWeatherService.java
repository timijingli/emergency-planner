package com.emergency.service;

import com.emergency.model.WeatherInfo;
import com.emergency.model.WeatherInfo.Warning;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 和风天气 API 代理
 *
 * 封装城市搜索、实时天气、灾害预警、短期预报等能力。
 * 免费订阅使用 devapi.qweather.com 域名。
 */
@Service
public class QWeatherService {

    private static final Logger log = LoggerFactory.getLogger(QWeatherService.class);

    /** 免费订阅用 devapi，付费订阅改为 api.qweather.com */
    private static final String WEATHER_BASE = "https://devapi.qweather.com/v7";
    private static final String GEO_BASE = "https://geoapi.qweather.com/v2";

    private final String apiKey;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public QWeatherService(
            @Value("${qweather.api-key}") String apiKey,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    // ==================== 城市搜索 — 城市名 → LocationID ====================

    /**
     * 根据城市名查询 LocationID（和风天气几乎所有接口都需要 LocationID）
     * 会自动处理"北京市"→"北京"这类后缀问题
     */
    public String getLocationId(String cityName) throws IOException {
        // 尝试多种城市名格式
        String[] candidates = {
            cityName,
            cityName.replaceAll("[市区县]$", ""),       // 去"市/区/县"后缀
            cityName.replaceAll("[省市区县]+$", ""),     // 去所有行政区后缀
            cityName.replaceAll("市$", ""),              // 只去"市"
        };

        IOException lastError = null;
        for (String candidate : candidates) {
            if (candidate.isBlank()) continue;
            String url = GEO_BASE + "/city/lookup"
                    + "?location=" + encode(candidate)
                    + "&key=" + apiKey;

            try {
                JsonNode root = get(url);
                JsonNode locations = root.get("location");
                if (locations != null && !locations.isEmpty()) {
                    String id = locations.get(0).get("id").asText();
                    log.info("城市 {} → LocationID = {}", candidate, id);
                    return id;
                }
            } catch (IOException e) {
                lastError = e;
                // 继续尝试下一个候选
            }
        }

        throw new IOException("和风天气：未找到城市 — " + cityName
                + "（已尝试多种格式）", lastError);
    }

    // ==================== 实时天气（坐标直查，推荐方式） ====================

    /**
     * 通过经纬度直接查询天气（无需城市搜索API权限！）
     * 和风天气原生支持 location=经度,纬度 格式
     */
    public WeatherInfo getWeatherByCoord(double lng, double lat, String cityName) throws IOException {
        String locParam = String.format("%.2f,%.2f", lng, lat);
        log.info("坐标天气查询: {} ({})", locParam, cityName);

        String url = WEATHER_BASE + "/weather/now"
                + "?location=" + locParam
                + "&key=" + apiKey;

        JsonNode root = get(url);
        JsonNode now = root.get("now");
        if (now == null) {
            throw new IOException("和风天气：无实时天气数据");
        }

        WeatherInfo info = new WeatherInfo();
        info.setCity(cityName);
        info.setWeather(optStr(now, "text"));
        info.setTemperature(optStr(now, "temp") + "°C");
        info.setFeelsLike(optStr(now, "feelsLike") + "°C");
        info.setWindDirection(optStr(now, "windDir"));
        info.setWindScale(optStr(now, "windScale") + "级");
        info.setWindSpeed(optStr(now, "windSpeed") + "km/h");
        info.setHumidity(optStr(now, "humidity") + "%");
        info.setVisibility(optStr(now, "vis") + "km");
        info.setPrecipitation(optStr(now, "precip") + "mm");

        // 灾害预警
        try {
            info.setWarnings(getWarnings(locParam));
        } catch (Exception e) {
            log.warn("获取预警信息失败: {}", e.getMessage());
            info.setWarnings(List.of());
        }

        return info;
    }

    /**
     * 通过坐标获取24小时预报
     */
    public String get24hForecastByCoord(double lng, double lat) throws IOException {
        String locParam = String.format("%.2f,%.2f", lng, lat);
        String url = WEATHER_BASE + "/weather/24h"
                + "?location=" + locParam
                + "&key=" + apiKey;

        JsonNode root = get(url);
        JsonNode hourly = root.get("hourly");
        if (hourly == null || hourly.isEmpty()) {
            return "暂无预报数据";
        }

        StringBuilder sb = new StringBuilder();
        int[] offsets = {0, 2, 5, 11};
        for (int idx : offsets) {
            if (idx < hourly.size()) {
                JsonNode h = hourly.get(idx);
                sb.append(String.format("[%s] %s，%s°C，风力%s级；",
                        optStr(h, "fxTime"),
                        optStr(h, "text"),
                        optStr(h, "temp"),
                        optStr(h, "windScale")));
            }
        }
        return sb.toString();
    }

    // ==================== 实时天气（城市名查询，需要Geo API权限） ====================

    /**
     * 获取指定城市的实时天气
     */
    public WeatherInfo getWeatherNow(String cityName) throws IOException {
        // 城市名预处理：去掉"市"等后缀以更好匹配
        String cleanName = cityName.trim().replaceAll("[省市区县]$", "");
        if (cleanName.isEmpty()) cleanName = cityName.trim();

        String locId;
        try {
            locId = getLocationId(cleanName);
        } catch (IOException e) {
            log.warn("城市查找失败 '{}', 尝试原始名称 '{}'", cleanName, cityName);
            locId = getLocationId(cityName.trim());
        }

        String url = WEATHER_BASE + "/weather/now"
                + "?location=" + locId
                + "&key=" + apiKey;

        JsonNode root = get(url);
        JsonNode now = root.get("now");
        if (now == null) {
            throw new IOException("和风天气：无实时天气数据");
        }

        WeatherInfo info = new WeatherInfo();
        info.setCity(cleanName);
        info.setWeather(optStr(now, "text"));          // 如 "大雨"
        info.setTemperature(optStr(now, "temp") + "°C");
        info.setFeelsLike(optStr(now, "feelsLike") + "°C");
        info.setWindDirection(optStr(now, "windDir"));
        info.setWindScale(optStr(now, "windScale") + "级");
        info.setWindSpeed(optStr(now, "windSpeed") + "km/h");
        info.setHumidity(optStr(now, "humidity") + "%");
        info.setVisibility(optStr(now, "vis") + "km");
        info.setPrecipitation(optStr(now, "precip") + "mm");

        // 同时获取灾害预警
        try {
            info.setWarnings(getWarnings(locId));
        } catch (Exception e) {
            log.warn("获取预警信息失败: {}", e.getMessage());
            info.setWarnings(List.of());
        }

        return info;
    }

    // ==================== 灾害预警 ====================

    /**
     * 获取当前生效的灾害预警信号
     */
    public List<Warning> getWarnings(String locationId) throws IOException {
        String url = WEATHER_BASE + "/warning/now"
                + "?location=" + locationId
                + "&key=" + apiKey;

        JsonNode root = get(url);
        JsonNode warningList = root.get("warning");
        if (warningList == null || warningList.isEmpty()) {
            return List.of();
        }

        List<Warning> list = new ArrayList<>();
        for (JsonNode w : warningList) {
            Warning warning = new Warning();
            warning.setType(optStr(w, "typeName"));          // 如 "暴雨"
            warning.setLevel(optStr(w, "level"));            // 如 "橙色"
            warning.setTitle(optStr(w, "title"));
            warning.setDescription(optStr(w, "text"));
            warning.setPubTime(optStr(w, "pubTime"));
            warning.setSender(optStr(w, "sender"));
            list.add(warning);
        }
        return list;
    }

    // ==================== 24小时预报（用于判断趋势） ====================

    /**
     * 获取24小时天气预报（每小时一条），用于判断天气趋势
     */
    public String get24hForecast(String cityName) throws IOException {
        String locId = getLocationId(cityName);
        String url = WEATHER_BASE + "/weather/24h"
                + "?location=" + locId
                + "&key=" + apiKey;

        JsonNode root = get(url);
        JsonNode hourly = root.get("hourly");
        if (hourly == null || hourly.isEmpty()) {
            return "暂无预报数据";
        }

        // 取未来几个关键时段：+1h, +3h, +6h, +12h
        StringBuilder sb = new StringBuilder();
        int[] offsets = {0, 2, 5, 11}; // 索引偏移
        for (int idx : offsets) {
            if (idx < hourly.size()) {
                JsonNode h = hourly.get(idx);
                sb.append(String.format("[%s] %s，%s°C，风力%s级；",
                        optStr(h, "fxTime"),
                        optStr(h, "text"),
                        optStr(h, "temp"),
                        optStr(h, "windScale")));
            }
        }
        return sb.toString();
    }

    // ==================== 工具方法 ====================

    private JsonNode get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("和风天气 HTTP " + response.code());
            }
            String body = response.body().string();
            JsonNode root = objectMapper.readTree(body);
            String code = root.has("code") ? root.get("code").asText() : "";
            if (!"200".equals(code)) {
                // 和风天气错误码参考：https://dev.qweather.com/docs/resource/status-code/
                throw new IOException("和风天气 API 错误 code=" + code);
            }
            return root;
        }
    }

    private String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    private String optStr(JsonNode node, String field) {
        JsonNode f = node.get(field);
        if (f == null || f.isNull()) return "";
        return f.asText();
    }
}
