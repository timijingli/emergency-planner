package com.emergency.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * AI 返回的预案（由 DeepSeek 的 JSON 响应反序列化而来）
 *
 * 字段名与提示词中约定的 JSON key 一致。
 * @JsonIgnoreProperties(ignoreUnknown = true) 保证 AI 多返回了字段不会报错。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanResponse {

    /** 预案标题 */
    private String title;

    /** 风险等级：高 / 中 / 低 */
    private String riskLevel;

    /** 一句话概述当前形势 */
    private String overview;

    /** 第一时间行动 */
    private List<ActionItem> firstActions;

    /** 分阶段行动步骤 */
    private List<Phase> steps;

    /** 千万别做的事 */
    private List<String> dontDo;

    /** 事后处理建议 */
    private String afterRescue;

    /** 紧急联系电话 */
    private List<Contact> emergencyContacts;

    // ========== 内部类 ==========

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActionItem {
        private int step;
        private String action;
        private String reason;

        public int getStep() { return step; }
        public void setStep(int step) { this.step = step; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Phase {
        private String phase;
        private List<StepDetail> steps;

        public String getPhase() { return phase; }
        public void setPhase(String phase) { this.phase = phase; }
        public List<StepDetail> getSteps() { return steps; }
        public void setSteps(List<StepDetail> steps) { this.steps = steps; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StepDetail {
        private int step;
        private String action;
        private String reason;
        private String attention;

        public int getStep() { return step; }
        public void setStep(int step) { this.step = step; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getAttention() { return attention; }
        public void setAttention(String attention) { this.attention = attention; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {
        private String name;
        private String number;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }
    }

    // ========== 顶层 Getter / Setter ==========
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }
    public List<ActionItem> getFirstActions() { return firstActions; }
    public void setFirstActions(List<ActionItem> firstActions) { this.firstActions = firstActions; }
    public List<Phase> getSteps() { return steps; }
    public void setSteps(List<Phase> steps) { this.steps = steps; }
    public List<String> getDontDo() { return dontDo; }
    public void setDontDo(List<String> dontDo) { this.dontDo = dontDo; }
    public String getAfterRescue() { return afterRescue; }
    public void setAfterRescue(String afterRescue) { this.afterRescue = afterRescue; }
    public List<Contact> getEmergencyContacts() { return emergencyContacts; }
    public void setEmergencyContacts(List<Contact> emergencyContacts) { this.emergencyContacts = emergencyContacts; }
}
