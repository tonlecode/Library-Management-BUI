package com.example.demo.model;

public enum MemberStatus {
    ACTIVE("Active", "success"),
    EXPIRED("Expired", "secondary"),
    SUSPENDED("Suspended", "warning"),
    BLOCKED("Blocked", "danger");

    private final String label;
    private final String bootstrapColor;

    MemberStatus(String label, String bootstrapColor) {
        this.label = label;
        this.bootstrapColor = bootstrapColor;
    }

    public String getLabel() {
        return label;
    }

    public String getBootstrapColor() {
        return bootstrapColor;
    }
}
