package com.example.demo.model;

public enum LoanStatus {
    ACTIVE("Active", "primary"),
    OVERDUE("Overdue", "danger"),
    RETURNED("Returned", "success");

    private final String label;
    private final String bootstrapColor;

    LoanStatus(String label, String bootstrapColor) {
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
