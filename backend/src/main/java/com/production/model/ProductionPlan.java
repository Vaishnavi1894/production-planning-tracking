package com.production.model;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Represents a Production Target Plan for a product.
 * Defines the planned quantity for a specific target month/period.
 */
@Serdeable
public class ProductionPlan {
    private Long id;
    private Long productId;
    private String productName;
    private String planMonth;       // e.g. "2026-09"
    private double plannedQuantity; // Target units to produce
    private String notes;           // Target notes or instructions

    public ProductionPlan() {
    }

    public ProductionPlan(Long id, Long productId, String productName, String planMonth, double plannedQuantity, String notes) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.planMonth = planMonth;
        this.plannedQuantity = plannedQuantity;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPlanMonth() {
        return planMonth;
    }

    public void setPlanMonth(String planMonth) {
        this.planMonth = planMonth;
    }

    public double getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(double plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
