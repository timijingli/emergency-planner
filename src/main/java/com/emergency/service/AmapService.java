package com.emergency.service;

import com.emergency.model.GeoInfo;
import com.emergency.model.GeoInfo.NearbyFacility;
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
 * 高德地图 Web 服务 API 代理
 *
 * 封装了地理编码、逆地理编码、周边搜索、IP定位等能力。
 * API Key 从 application.yml 注入，不暴露给前端。
 */
@Service
public class AmapService {

    private static final Logger log = LoggerFactory.getLogger(AmapService.class);
    private static final String BASE_URL = "https://restapi.amap.com/v3";

    private final String apiKey;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AmapService(
            @Value("${amap.api-key}") String apiKey,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    // ==================== 地理编码 — 地址 → 坐标 ====================

    /**
     * 将中文地址转换为经纬度坐标 + 结构化地址
     */
    public GeoInfo geocode(String address) throws IOException {
        String url = BASE_URL + "/geocode/geo"
                + "?key=" + apiKey
                + "&address=" + encode(address)
                + "&output=JSON";

        JsonNode root = get(url);

        JsonNode geocodes = root.get("geocodes");
        if (geocodes == null || geocodes.isEmpty()) {
            throw new IOException("高德地理编码失败：未找到该地址 — " + address);
        }

        JsonNode first = geocodes.get(0);
        String[] lngLat = first.get("location").asText().split(",");

        GeoInfo info = new GeoInfo();
        info.setLng(Double.parseDouble(lngLat[0]));
        info.setLat(Double.parseDouble(lngLat[1]));
        info.setAddress(first.has("formatted_address")
                ? first.get("formatted_address").asText() : address);
        info.setCity(first.has("city") ? first.get("city").asText() : "");
        info.setDistrict(first.has("district") ? first.get("district").asText() : "");

        return info;
    }

    // ==================== 逆地理编码 — 坐标 → 地址 ====================

    /**
     * 将经纬度转换为结构化地址
     */
    public GeoInfo regeocode(double lng, double lat) throws IOException {
        String url = BASE_URL + "/geocode/regeo"
                + "?key=" + apiKey
                + "&location=" + lng + "," + lat
                + "&extensions=all"
                + "&output=JSON";

        JsonNode root = get(url);
        JsonNode regeo = root.get("regeocode");
        if (regeo == null) {
            throw new IOException("高德逆地理编码失败");
        }

        JsonNode addrComp = regeo.get("addressComponent");
        GeoInfo info = new GeoInfo();
        info.setLng(lng);
        info.setLat(lat);
        info.setAddress(regeo.has("formatted_address")
                ? regeo.get("formatted_address").asText() : lng + "," + lat);
        if (addrComp != null) {
            info.setCity(optStr(addrComp, "city"));
            info.setDistrict(optStr(addrComp, "district"));
        }
        return info;
    }

    // ==================== 周边搜索 ====================

    /**
     * 搜索坐标周边的安全设施
     *
     * @param lng      经度
     * @param lat      纬度
     * @param keywords 搜索关键词，用 | 分隔，如 "避难所|医院|消防站"
     * @param radius   搜索半径（米），默认 3000
     */
    public List<NearbyFacility> searchNearby(double lng, double lat,
                                              String keywords, int radius) throws IOException {
        // 用 HttpUrl.Builder 避免 | 等特殊字符被错误编码
        okhttp3.HttpUrl url = okhttp3.HttpUrl.parse(BASE_URL + "/place/around").newBuilder()
                .addQueryParameter("key", apiKey)
                .addQueryParameter("location", lng + "," + lat)
                .addQueryParameter("keywords", keywords)           // 不手动编码！
                .addQueryParameter("radius", String.valueOf(radius))
                .addQueryParameter("offset", "20")
                .addQueryParameter("output", "JSON")
                .build();

        JsonNode root = get(url);
        JsonNode pois = root.get("pois");
        if (pois == null || pois.isEmpty()) {
            return List.of();
        }

        List<NearbyFacility> list = new ArrayList<>();
        for (JsonNode poi : pois) {
            NearbyFacility f = new NearbyFacility();
            f.setName(optStr(poi, "name"));
            f.setType(optStr(poi, "type"));
            f.setAddress(optStr(poi, "address"));
            f.setDistance(optStr(poi, "distance") + "米");

            String[] loc = poi.get("location").asText().split(",");
            f.setLng(Double.parseDouble(loc[0]));
            f.setLat(Double.parseDouble(loc[1]));
            list.add(f);
        }
        return list;
    }

    // ==================== IP 定位 ====================

    /**
     * 根据 IP 获取当前城市名称（用于默认定位）
     */
    public String ipCity() {
        try {
            String url = BASE_URL + "/ip?key=" + apiKey + "&output=JSON";
            JsonNode root = get(url);

            // city 可能是空数组 [] 或字符串，需要区分处理
            JsonNode cityNode = root.get("city");
            String city = "";
            if (cityNode != null && cityNode.isArray()) {
                if (cityNode.size() > 0) {
                    city = cityNode.get(0).asText();  // 取数组第一个元素
                }
            } else if (cityNode != null) {
                city = cityNode.asText();
            }

            // city 为空或无效时回退到 province
            if (city.isEmpty() || city.equals("[]") || city.length() < 2) {
                JsonNode provNode = root.get("province");
                if (provNode != null && provNode.isArray() && provNode.size() > 0) {
                    city = provNode.get(0).asText();
                } else if (provNode != null && !provNode.isArray()) {
                    city = provNode.asText();
                }
            }

            // 最终回退
            if (city.isEmpty() || city.equals("[]") || city.length() < 2) {
                city = "北京";
            }

            log.info("IP定位城市: {}", city);
            return city;
        } catch (Exception e) {
            log.warn("IP定位失败: {}", e.getMessage());
            return "北京";
        }
    }

    // ==================== 天气查询（高德自带，无需额外API Key） ====================

    /**
     * 通过城市名或 adcode 获取实时天气
     * 高德天气 API：/v3/weather/weatherInfo
     */
    public WeatherInfo getWeather(String cityOrAdcode) throws IOException {
        String url = BASE_URL + "/weather/weatherInfo"
                + "?key=" + apiKey
                + "&city=" + encode(cityOrAdcode)
                + "&extensions=base"
                + "&output=JSON";

        JsonNode root = get(url);
        JsonNode lives = root.get("lives");
        if (lives == null || lives.isEmpty()) {
            throw new IOException("高德天气：无数据");
        }

        JsonNode live = lives.get(0);
        WeatherInfo info = new WeatherInfo();
        info.setCity(optStr(live, "city"));
        info.setWeather(optStr(live, "weather"));
        info.setTemperature(optStr(live, "temperature") + "°C");
        info.setWindDirection(optStr(live, "winddirection"));
        info.setWindScale(optStr(live, "windpower") + "级");
        info.setHumidity(optStr(live, "humidity") + "%");
        info.setVisibility("—");
        info.setPrecipitation(optStr(live, "rain") + "mm");
        info.setFeelsLike("—°C");
        info.setWindSpeed("—");

        try {
            // 获取预报（含预警信息）
            info.setWarnings(getWarnings(cityOrAdcode));
        } catch (Exception e) {
            log.warn("获取预警失败: {}", e.getMessage());
            info.setWarnings(List.of());
        }

        return info;
    }

    /**
     * 获取天气预警（高德 all 扩展包含预警）
     */
    private List<WeatherInfo.Warning> getWarnings(String city) throws IOException {
        String url = BASE_URL + "/weather/weatherInfo"
                + "?key=" + apiKey
                + "&city=" + encode(city)
                + "&extensions=all"
                + "&output=JSON";

        JsonNode root = get(url);
        JsonNode forecasts = root.get("forecasts");
        if (forecasts == null || forecasts.isEmpty()) return List.of();

        JsonNode casts = forecasts.get(0).get("casts");
        if (casts == null || casts.isEmpty()) return List.of();

        // 查找今日是否有预警
        JsonNode today = casts.get(0);
        List<WeatherInfo.Warning> warnings = new ArrayList<>();

        // 高德可能返回 dayweather 包含"暴雨"等极端天气关键词
        String dayWeather = optStr(today, "dayweather");
        String nightWeather = optStr(today, "nightweather");

        for (String w : new String[]{dayWeather, nightWeather}) {
            if (w.contains("雨") && (w.contains("暴") || w.contains("大"))) {
                WeatherInfo.Warning warning = new WeatherInfo.Warning();
                warning.setType("暴雨");
                warning.setLevel("注意");
                warning.setTitle("预计" + (w.equals(dayWeather) ? "白天" : "夜间") + "有" + w);
                warning.setDescription("当日预报：" + w + "，请注意防范");
                warning.setPubTime(optStr(today, "date"));
                warning.setSender("高德天气");
                warnings.add(warning);
            }
            if (w.contains("雪") && (w.contains("暴") || w.contains("大"))) {
                WeatherInfo.Warning warning = new WeatherInfo.Warning();
                warning.setType("暴雪");
                warning.setLevel("注意");
                warning.setTitle("预计有" + w);
                warning.setDescription("当日预报：" + w + "，请注意防寒保暖");
                warning.setPubTime(optStr(today, "date"));
                warning.setSender("高德天气");
                warnings.add(warning);
            }
        }
        return warnings;
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
                throw new IOException("高德 API HTTP " + response.code());
            }
            String body = response.body().string();
            JsonNode root = objectMapper.readTree(body);
            int status = root.has("status") ? Integer.parseInt(root.get("status").asText()) : 0;
            if (status != 1) {
                String info = root.has("info") ? root.get("info").asText() : "未知错误";
                throw new IOException("高德 API 返回错误: " + info);
            }
            return root;
        }
    }

    /** 直接接收 HttpUrl，避免 URL 字符串被二次解析/编码（用于 nearby 等场景） */
    private JsonNode get(okhttp3.HttpUrl url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("高德 API HTTP " + response.code());
            }
            String body = response.body().string();
            JsonNode root = objectMapper.readTree(body);
            int status = root.has("status") ? Integer.parseInt(root.get("status").asText()) : 0;
            if (status != 1) {
                String info = root.has("info") ? root.get("info").asText() : "未知错误";
                throw new IOException("高德 API 返回错误: " + info);
            }
            return root;
        }
    }

    private String encode(String s) {
        return okhttp3.HttpUrl.parse("http://dummy") != null
                ? java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8)
                : s;
    }

    private String optStr(JsonNode node, String field) {
        JsonNode f = node.get(field);
        if (f == null || f.isNull()) return "";
        // 高德 API 某些字段在无数据时返回空数组 [] 而非 null
        if (f.isArray()) {
            if (f.size() == 0) return "";
            return f.get(0).asText();
        }
        String text = f.asText();
        // 过滤掉无效值
        if ("[]".equals(text)) return "";
        return text;
    }
}
