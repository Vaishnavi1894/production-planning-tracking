package com.production.model;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Represents a row in the Variance & Achievement Report.
 * Compares Planned Quantity vs Actual Produced Quantity.
 *
 * Formulas:
 * 1. Difference = Produced Quantity - Planned Quantity
 *    - Positive (> 0): Surplus / Ahead of target
 *    - Negative (< 0): Deficit / Behind target
 *    - Zero (0): Exactly on target
 *
 * 2. Achievement % = (Produced Quantity / Planned Quantity) * 100
 *    - >= 100%: Achieved (Target reached/exceeded)
 *    - 80% to 99%: On Track
 *    - < 80%: Lagging behind target
 */
@Serdeable
public class VarianceReportItem {
    private Long productId;
    private String productCode;
    private String productName;
    private String category;
    private String unitOfMeasure;
    private double plannedQuantity;
    private double producedQuantity;
    private double difference;
    private double achievementPercentage;
    private String status; // "ACHIEVED", "ON_TRACK", "LAGGING"

    public VarianceReportItem() {
    }

    public VarianceReportItem(Long productId, String productCode, String productName, String category,
                              String unitOfMeasure, double plannedQuantity, double producedQuantity) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
        this.unitOfMeasure = unitOfMeasure;
        this.plannedQuantity = plannedQuantity;
        this.producedQuantity = producedQuantity;

        // Calculate Difference: Produced - Planned
        this.difference = Math.round((producedQuantity - plannedQuantity) * 100.0) / 100.0;

        // Calculate Achievement Percentage: (Produced / Planned) * 100
        if (plannedQuantity > 0) {
            double rawPercentage = (producedQuantity / plannedQuantity) * 100.0;
            this.achievementPercentage = Math.round(rawPercentage * 10.0) / 10.0; // Round to 1 decimal place
        } else {
            this.achievementPercentage = producedQuantity > 0 ? 100.0 : 0.0;
        }

        // Determine Status based on achievement percentage
        if (this.achievementPercentage >= 100.0) {
            this.status = "ACHIEVED";
        } else if (this.achievementPercentage >= 80.0) {
            this.status = "ON_TRACK";
        } else {
            this.status = "LAGGING";
        }
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public double getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(double plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public double getProducedQuantity() {
        return producedQuantity;
    }

    public void setProducedQuantity(double producedQuantity) {
        this.producedQuantity = producedQuantity;
    }

    public double getDifference() {
        return difference;
    }

    public void setDifference(double difference) {
        this.difference = difference;
    }

    public double getAchievementPercentage() {
        return achievementPercentage;
    }

    public void setAchievementPercentage(double achievementPercentage) {
        this.achievementPercentage = achievementPercentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
