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
}