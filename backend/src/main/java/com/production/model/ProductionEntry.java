package com.production.model;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Represents a Daily Production Log Entry.
 * Tracks actual units manufactured on a specific date and shift.
 */
@Serdeable
public class ProductionEntry {
    private Long id;
    private Long productId;
    private String productName;
    private String entryDate;         // e.g. "2026-09-13"
    private double producedQuantity;  // Actual units produced
    private String shift;             // Morning, Evening, Night
    private String remarks;           // Operator comments / status

    public ProductionEntry() {
    }

    public ProductionEntry(Long id, Long productId, String productName, String entryDate, double producedQuantity, String shift, String remarks) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.entryDate = entryDate;
        this.producedQuantity = producedQuantity;
        this.shift = shift;
        this.remarks = remarks;
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

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    public double getProducedQuantity() {
        return producedQuantity;
    }

    public void setProducedQuantity(double producedQuantity) {
        this.producedQuantity = producedQuantity;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
