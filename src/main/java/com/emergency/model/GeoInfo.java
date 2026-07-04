package com.emergency.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * 地理信息数据模型 — 封装高德地图返回的坐标、地址、周边POI
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoInfo {

    /** 经度 */
    private double lng;
    /** 纬度 */
    private double lat;
    /** 结构化地址（如：北京市朝阳区某某路XX号） */
    private String address;
    /** 城市 */
    private String city;
    /** 区县 */
    private String district;
    /** 周边安全设施列表 */
    private List<NearbyFacility> facilities;

    // ========== 内部类 ==========

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NearbyFacility {
        private String name;       // 名称
        private String type;       // 类型：避难所/医院/消防站/安全出口
        private String address;    // 地址
        private double lng;
        private double lat;
        private String distance;   // 距离（如 "350米"）

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public String getDistance() { return distance; }
        public void setDistance(String distance) { this.distance = distance; }
    }

    // ========== Getter / Setter ==========
    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public List<NearbyFacility> getFacilities() { return facilities; }
    public void setFacilities(List<NearbyFacility> facilities) { this.facilities = facilities; }
}
