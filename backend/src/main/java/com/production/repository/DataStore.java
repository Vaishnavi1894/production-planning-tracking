package com.production.repository;

import com.production.model.Product;
import com.production.model.ProductionEntry;
import com.production.model.ProductionPlan;
import com.production.model.VarianceReportItem;
import jakarta.inject.Singleton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory DataStore acting as our database repository.
 * Preloaded with realistic manufacturing sample data so the system works out-of-the-box.
 * Thread-safe collections ensure safe concurrent reads and writes.
 */
@Singleton
public class DataStore {

    private final List<Product> products = new CopyOnWriteArrayList<>();
    private final List<ProductionPlan> plans = new CopyOnWriteArrayList<>();
    private final List<ProductionEntry> entries = new CopyOnWriteArrayList<>();

    private final AtomicLong productIdSeq = new AtomicLong(1);
    private final AtomicLong planIdSeq = new AtomicLong(1);
    private final AtomicLong entryIdSeq = new AtomicLong(1);

    public DataStore() {
        seedSampleData();
    }

    /**
     * Seeds initial realistic manufacturing data.
     */
    private void seedSampleData() {
        // 1. Initial Products
        Product p1 = addProduct(new Product(null, "PRD-001", "Alloy Wheel 17-inch", "Casting", "Units", "High-strength aluminum alloy wheels"));
        Product p2 = addProduct(new Product(null, "PRD-002", "Engine Cylinder Block", "Machining", "Units", "Precision-bored 4-cylinder engine blocks"));
        Product p3 = addProduct(new Product(null, "PRD-003", "Brake Rotor Disc", "Fabrication", "Units", "Ventilated front rotor discs"));
        Product p4 = addProduct(new Product(null, "PRD-004", "Steering Gearbox", "Assembly", "Sets", "Hydraulic power steering rack"));

        String currentMonth = LocalDate.now().getYear() + "-" + String.format("%02d", LocalDate.now().getMonthValue());

        // 2. Initial Production Plans
        addPlan(new ProductionPlan(null, p1.getId(), p1.getProductName(), currentMonth, 500, "Regular monthly target"));
        addPlan(new ProductionPlan(null, p2.getId(), p2.getProductName(), currentMonth, 300, "Export order batch"));
        addPlan(new ProductionPlan(null, p3.getId(), p3.getProductName(), currentMonth, 400, "OEM supply target"));
        addPlan(new ProductionPlan(null, p4.getId(), p4.getProductName(), currentMonth, 250, "Assembly line requirement"));

        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();

        // 3. Initial Daily Production Entries
        // P1: Total Produced = 160 + 180 + 180 = 520 (Target 500 => Achieved, 104%)
        addEntry(new ProductionEntry(null, p1.getId(), p1.getProductName(), yesterday, 160, "Morning", "Batch #A1 completed smoothly"));
        addEntry(new ProductionEntry(null, p1.getId(), p1.getProductName(), yesterday, 180, "Evening", "Shift achieved 100% capacity"));
        addEntry(new ProductionEntry(null, p1.getId(), p1.getProductName(), today, 180, "Morning", "Extra units produced"));

        // P2: Total Produced = 120 + 130 = 250 (Target 300 => On Track, 83.3%)
        addEntry(new ProductionEntry(null, p2.getId(), p2.getProductName(), yesterday, 120, "Morning", "Cylinder CNC Line 1"));
        addEntry(new ProductionEntry(null, p2.getId(), p2.getProductName(), today, 130, "Morning", "Standard machining cycle"));

        // P3: Total Produced = 80 + 90 = 170 (Target 400 => Lagging, 42.5%)
        addEntry(new ProductionEntry(null, p3.getId(), p3.getProductName(), yesterday, 80, "Morning", "Minor tooling delay"));
        addEntry(new ProductionEntry(null, p3.getId(), p3.getProductName(), today, 90, "Evening", "Lathe maintenance completed"));

        // P4: Total Produced = 125 + 125 = 250 (Target 250 => Achieved, 100%)
        addEntry(new ProductionEntry(null, p4.getId(), p4.getProductName(), yesterday, 125, "Morning", "Full assembly shift"));
        addEntry(new ProductionEntry(null, p4.getId(), p4.getProductName(), today, 125, "Morning", "Final quality inspection passed"));
    }

    // ================= PRODUCT METHODS =================
    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public Optional<Product> getProductById(Long id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public Product addProduct(Product product) {
        if (product.getId() == null) {
            product.setId(productIdSeq.getAndIncrement());
        }
        products.add(product);
        return product;
    }

    public boolean deleteProduct(Long id) {
        return products.removeIf(p -> p.getId().equals(id));
    }

    // ================= PRODUCTION PLAN METHODS =================
    public List<ProductionPlan> getAllPlans() {
        return new ArrayList<>(plans);
    }

    public ProductionPlan addPlan(ProductionPlan plan) {
        if (plan.getId() == null) {
            plan.setId(planIdSeq.getAndIncrement());
        }
        // Auto fill product name if absent
        if ((plan.getProductName() == null || plan.getProductName().isBlank()) && plan.getProductId() != null) {
            getProductById(plan.getProductId()).ifPresent(p -> plan.setProductName(p.getProductName()));
        }
        plans.add(plan);
        return plan;
    }

    // ================= PRODUCTION ENTRY METHODS =================
    public List<ProductionEntry> getAllEntries() {
        return new ArrayList<>(entries);
    }

    public ProductionEntry addEntry(ProductionEntry entry) {
        if (entry.getId() == null) {
            entry.setId(entryIdSeq.getAndIncrement());
        }
        // Auto fill product name if absent
        if ((entry.getProductName() == null || entry.getProductName().isBlank()) && entry.getProductId() != null) {
            getProductById(entry.getProductId()).ifPresent(p -> entry.setProductName(p.getProductName()));
        }
        entries.add(entry);
        return entry;
    }

    // ================= VARIANCE REPORT GENERATION =================
    /**
     * Generates variance analysis across all products.
     * Computes Planned Quantity vs Produced Quantity, Difference, and Achievement %.
     */
    public List<VarianceReportItem> generateVarianceReport() {
        List<VarianceReportItem> report = new ArrayList<>();

        for (Product product : products) {
            // 1. Calculate total planned quantity for this product
            double totalPlanned = plans.stream()
                    .filter(p -> p.getProductId() != null && p.getProductId().equals(product.getId()))
                    .mapToDouble(ProductionPlan::getPlannedQuantity)
                    .sum();

            // 2. Calculate total actual quantity produced for this product
            double totalProduced = entries.stream()
                    .filter(e -> e.getProductId() != null && e.getProductId().equals(product.getId()))
                    .mapToDouble(ProductionEntry::getProducedQuantity)
                    .sum();

            // 3. Create Variance Item with mathematical difference and achievement %
            VarianceReportItem item = new VarianceReportItem(
                    product.getId(),
                    product.getProductCode(),
                    product.getProductName(),
                    product.getCategory(),
                    product.getUnitOfMeasure(),
                    totalPlanned,
                    totalProduced
            );

            report.add(item);
        }

        return report;
    }
}
