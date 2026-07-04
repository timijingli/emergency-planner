package com.emergency.model;

/**
 * 前端发送的请求
 */
public class PlanRequest {

    /** 场景描述，如"大学宿舍楼3楼，凌晨突发火灾，楼道有浓烟" */
    private String scenario;

    /** 附加信息，如"室内有2人，行动方便"（可选） */
    private String extraInfo;

    /** 地图上选中的地址 */
    private String address;
    /** 经度 */
    private Double lng;
    /** 纬度 */
    private Double lat;
    /** 城市（用于天气查询） */
    private String city;

    // ========== 构造函数 ==========
    public PlanRequest() {}

    public PlanRequest(String scenario, String extraInfo) {
        this.scenario = scenario;
        this.extraInfo = extraInfo;
    }

    // ========== Getter / Setter ==========
    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
