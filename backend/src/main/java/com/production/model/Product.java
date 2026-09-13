package com.production.model;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Represents a Product in the Product Master.
 * Each product has a unique ID, product code, name, category, and unit of measure.
 */
@Serdeable
public class Product {
    private Long id;
    private String productCode;   // e.g. "PRD-101"
    private String productName;   // e.g. "Brake Disc Assembly"
    private String category;      // e.g. "Machining", "Automotive"
    private String unitOfMeasure; // e.g. "Units", "Pcs", "Sets"
    private String description;   // Optional product notes

    // Default constructor needed for JSON deserialization
    public Product() {
    }

    public Product(Long id, String productCode, String productName, String category, String unitOfMeasure, String description) {
        this.id = id;
        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
        this.unitOfMeasure = unitOfMeasure;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
