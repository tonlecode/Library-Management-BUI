package com.example.demo.model;

public enum BookStatus {
    AVAILABLE("Available", "success"),
    CHECKED_OUT("Checked out", "warning"),
    LOST("Lost", "danger"),
    DAMAGED("Damaged", "secondary");

    private final String label;
    private final String bootstrapColor;

    BookStatus(String label, String bootstrapColor) {
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
