package com.emergency.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * 天气信息数据模型 — 封装和风天气返回的实时天气 + 灾害预警
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherInfo {

    /** 城市名称 */
    private String city;
    /** 天气状况（如：晴、大雨、暴雪） */
    private String weather;
    /** 温度（摄氏度） */
    private String temperature;
    /** 体感温度 */
    private String feelsLike;
    /** 风向 */
    private String windDirection;
    /** 风力等级 */
    private String windScale;
    /** 风速 km/h */
    private String windSpeed;
    /** 相对湿度 % */
    private String humidity;
    /** 能见度 km */
    private String visibility;
    /** 降水量 mm */
    private String precipitation;
    /** 当前生效的预警信号 */
    private List<Warning> warnings;

    // ========== 内部类 ==========

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Warning {
        /** 预警类型（如：暴雨、台风、暴雪） */
        private String type;
        /** 预警等级（如：黄色、橙色、红色） */
        private String level;
        /** 预警标题 */
        private String title;
        /** 预警详情 */
        private String description;
        /** 发布时间 */
        private String pubTime;
        /** 指令来源（如：中央气象台） */
        private String sender;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getPubTime() { return pubTime; }
        public void setPubTime(String pubTime) { this.pubTime = pubTime; }
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
    }

    // ========== Getter / Setter ==========
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }
    public String getTemperature() { return temperature; }
    public void setTemperature(String temperature) { this.temperature = temperature; }
    public String getFeelsLike() { return feelsLike; }
    public void setFeelsLike(String feelsLike) { this.feelsLike = feelsLike; }
    public String getWindDirection() { return windDirection; }
    public void setWindDirection(String windDirection) { this.windDirection = windDirection; }
    public String getWindScale() { return windScale; }
    public void setWindScale(String windScale) { this.windScale = windScale; }
    public String getWindSpeed() { return windSpeed; }
    public void setWindSpeed(String windSpeed) { this.windSpeed = windSpeed; }
    public String getHumidity() { return humidity; }
    public void setHumidity(String humidity) { this.humidity = humidity; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public String getPrecipitation() { return precipitation; }
    public void setPrecipitation(String precipitation) { this.precipitation = precipitation; }
    public List<Warning> getWarnings() { return warnings; }
    public void setWarnings(List<Warning> warnings) { this.warnings = warnings; }
}
