package com.project.farma.analytics.systemAlert.model;

public enum AlertStatus {
    TRIGGERED,     // System caught it, farm owner hasn't seen it yet
    ACKNOWLEDGED,  // Farm supervisor flagged that they are investigating
    RESOLVED       // Action was taken, metrics are back to normal
}
