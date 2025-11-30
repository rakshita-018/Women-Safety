package com.women.safety.features.smartAlert.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SmartAlertAnalysisDTO {

    private Boolean shouldTriggerAlert;
    private Double overallRiskScore; // 0.0 to 1.0
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private String recommendation;
    private Map<String, Object> detectedActivities;
    private Integer recentActivityCount;
    private Boolean requiresImmediateAction;

    public SmartAlertAnalysisDTO() {}

    public SmartAlertAnalysisDTO(Boolean shouldTriggerAlert, Double overallRiskScore, String riskLevel) {
        this.shouldTriggerAlert = shouldTriggerAlert;
        this.overallRiskScore = overallRiskScore;
        this.riskLevel = riskLevel;
    }

    public Boolean getShouldTriggerAlert() {
        return shouldTriggerAlert;
    }

    public void setShouldTriggerAlert(Boolean shouldTriggerAlert) {
        this.shouldTriggerAlert = shouldTriggerAlert;
    }

    public Double getOverallRiskScore() {
        return overallRiskScore;
    }

    public void setOverallRiskScore(Double overallRiskScore) {
        this.overallRiskScore = overallRiskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public Map<String, Object> getDetectedActivities() {
        return detectedActivities;
    }

    public void setDetectedActivities(Map<String, Object> detectedActivities) {
        this.detectedActivities = detectedActivities;
    }

    public Integer getRecentActivityCount() {
        return recentActivityCount;
    }

    public void setRecentActivityCount(Integer recentActivityCount) {
        this.recentActivityCount = recentActivityCount;
    }

    public Boolean getRequiresImmediateAction() {
        return requiresImmediateAction;
    }

    public void setRequiresImmediateAction(Boolean requiresImmediateAction) {
        this.requiresImmediateAction = requiresImmediateAction;
    }
}