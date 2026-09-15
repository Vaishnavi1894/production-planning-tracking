package com.production.repository;

import com.production.model.Product;
import com.production.model.ProductionEntry;
import com.production.model.ProductionPlan;
import com.production.model.VarianceReportItem;
import jakarta.inject.Singleton;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DataStore Repository connecting directly to MySQL Database (production_db).
 * Also integrates ConcurrentHashMap as a high-speed, thread-safe in-memory cache!
 * 
 * Key Highlights:
 * 1. MySQL Database: Persistent storage of products, plans, and daily logs.
 * 2. ConcurrentHashMap: Thread-safe in-memory cache for O(1) lookups and concurrent reads.
 * 3. Graceful Fallback: Seamlessly falls back to in-memory ConcurrentHashMap if MySQL is temporarily offline.
 */
@Singleton
public class DataStore {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/production_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    // ConcurrentHashMap used as a high-speed, thread-safe cache
    private final Map<Long, Product> productCache = new ConcurrentHashMap<>();
    private final Map<Long, ProductionPlan> planCache = new ConcurrentHashMap<>();
    private final Map<Long, ProductionEntry> entryCache = new ConcurrentHashMap<>();

    private final AtomicLong fallbackIdSeq = new AtomicLong(100);
    private boolean mysqlConnected = false;

    public DataStore() {
        initDatabase();
    }

    /**
     * Initializes MySQL database connection and creates tables if they do not exist.
     */
    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "product_code VARCHAR(50) NOT NULL, " +
                    "product_name VARCHAR(255) NOT NULL, " +
                    "category VARCHAR(100) NOT NULL, " +
                    "unit_of_measure VARCHAR(50) NOT NULL, " +
                    "description TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS production_plans (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "product_id BIGINT NOT NULL, " +
                    "product_name VARCHAR(255), " +
                    "plan_month VARCHAR(20) NOT NULL, " +
                    "planned_quantity DOUBLE NOT NULL, " +
                    "notes TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS production_entries (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "product_id BIGINT NOT NULL, " +
                    "product_name VARCHAR(255), " +
                    "entry_date VARCHAR(20) NOT NULL, " +
                    "produced_quantity DOUBLE NOT NULL, " +
                    "shift VARCHAR(50) NOT NULL, " +
                    "remarks TEXT)");

            mysqlConnected = true;
            System.out.println("✅ [MySQL] Connected successfully to production_db on localhost:3306");
            syncCacheFromDatabase();

        } catch (SQLException e) {
            System.err.println("⚠️ [MySQL] Could not connect to MySQL: " + e.getMessage() + ". Using ConcurrentHashMap fallback.");
            mysqlConnected = false;
            seedFallbackData();
        }
    }

    /**
     * Pre-populates the ConcurrentHashMap cache from MySQL tables.
     */
    private void syncCacheFromDatabase() {
        if (!mysqlConnected) return;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Load products into ConcurrentHashMap
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM products");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product(
                            rs.getLong("id"),
                            rs.getString("product_code"),
                            rs.getString("product_name"),
                            rs.getString("category"),
                            rs.getString("unit_of_measure"),
                            rs.getString("description")
                    );
                    productCache.put(p.getId(), p);
                }
            }

            // Load plans into ConcurrentHashMap
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM production_plans");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductionPlan plan = new ProductionPlan(
                            rs.getLong("id"),
                            rs.getLong("product_id"),
                            rs.getString("product_name"),
                            rs.getString("plan_month"),
                            rs.getDouble("planned_quantity"),
                            rs.getString("notes")
                    );
                    planCache.put(plan.getId(), plan);
                }
            }

            // Load entries into ConcurrentHashMap
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM production_entries");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductionEntry entry = new ProductionEntry(
                            rs.getLong("id"),
                            rs.getLong("product_id"),
                            rs.getString("product_name"),
                            rs.getString("entry_date"),
                            rs.getDouble("produced_quantity"),
                            rs.getString("shift"),
                            rs.getString("remarks")
                    );
                    entryCache.put(entry.getId(), entry);
                }
            }

            System.out.println("✅ [ConcurrentHashMap] Cache synchronized from MySQL: " + productCache.size() + " products.");

        } catch (SQLException e) {
            System.err.println("⚠️ Error syncing cache from MySQL: " + e.getMessage());
        }
    }

    private void seedFallbackData() {
        Product p1 = new Product(1L, "PRD-001", "Alloy Wheel 17-inch", "Casting", "Units", "Aluminum alloy wheels");
        Product p2 = new Product(2L, "PRD-002", "Engine Cylinder Block", "Machining", "Units", "4-cylinder engine blocks");
        productCache.put(p1.getId(), p1);
        productCache.put(p2.getId(), p2);
    }

    // ================= PRODUCT METHODS =================
    public List<Product> getAllProducts() {
        if (mysqlConnected) {
            List<Product> list = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM products ORDER BY id ASC");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Product p = new Product(
                            rs.getLong("id"),
                            rs.getString("product_code"),
                            rs.getString("product_name"),
                            rs.getString("category"),
                            rs.getString("unit_of_measure"),
                            rs.getString("description")
                    );
                    list.add(p);
                    productCache.put(p.getId(), p); // Update ConcurrentHashMap cache
                }
                return list;
            } catch (SQLException e) {
                System.err.println("MySQL Read Error: " + e.getMessage());
            }
        }
        return new ArrayList<>(productCache.values());
    }

    public Optional<Product> getProductById(Long id) {
        // Fast O(1) read from ConcurrentHashMap cache
        Product cached = productCache.get(id);
        if (cached != null) return Optional.of(cached);

        if (mysqlConnected) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM products WHERE id = ?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Product p = new Product(
                                rs.getLong("id"),
                                rs.getString("product_code"),
                                rs.getString("product_name"),
                                rs.getString("category"),
                                rs.getString("unit_of_measure"),
                                rs.getString("description")
                        );
                        productCache.put(p.getId(), p);
                        return Optional.of(p);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error reading product by ID: " + e.getMessage());
            }
        }
        return Optional.empty();
    }

    public Product addProduct(Product product) {
        if (mysqlConnected) {
            String sql = "INSERT INTO products (product_code, product_name, category, unit_of_measure, description) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, product.getProductCode());
                ps.setString(2, product.getProductName());
                ps.setString(3, product.getCategory());
                ps.setString(4, product.getUnitOfMeasure());
                ps.setString(5, product.getDescription());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        product.setId(keys.getLong(1));
                    }
                }
            } catch (SQLException e) {
                System.err.println("MySQL Insert Product Error: " + e.getMessage());
                if (product.getId() == null) product.setId(fallbackIdSeq.getAndIncrement());
            }
        } else {
            if (product.getId() == null) product.setId(fallbackIdSeq.getAndIncrement());
        }

        // Store into ConcurrentHashMap cache
        productCache.put(product.getId(), product);
        return product;
    }

    public boolean deleteProduct(Long id) {
        productCache.remove(id);
        if (mysqlConnected) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {
                ps.setLong(1, id);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("MySQL Delete Product Error: " + e.getMessage());
            }
        }
        return true;
    }

    // ================= PRODUCTION PLAN METHODS =================
    public List<ProductionPlan> getAllPlans() {
        if (mysqlConnected) {
            List<ProductionPlan> list = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM production_plans ORDER BY id ASC");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    ProductionPlan p = new ProductionPlan(
                            rs.getLong("id"),
                            rs.getLong("product_id"),
                            rs.getString("product_name"),
                            rs.getString("plan_month"),
                            rs.getDouble("planned_quantity"),
                            rs.getString("notes")
                    );
                    list.add(p);
                    planCache.put(p.getId(), p);
                }
                return list;
            } catch (SQLException e) {
                System.err.println("MySQL Read Plans Error: " + e.getMessage());
            }
        }
        return new ArrayList<>(planCache.values());
    }

    public ProductionPlan addPlan(ProductionPlan plan) {
        if ((plan.getProductName() == null || plan.getProductName().isBlank()) && plan.getProductId() != null) {
            getProductById(plan.getProductId()).ifPresent(p -> plan.setProductName(p.getProductName()));
        }

        if (mysqlConnected) {
            String sql = "INSERT INTO production_plans (product_id, product_name, plan_month, planned_quantity, notes) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setLong(1, plan.getProductId());
                ps.setString(2, plan.getProductName());
                ps.setString(3, plan.getPlanMonth());
                ps.setDouble(4, plan.getPlannedQuantity());
                ps.setString(5, plan.getNotes());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        plan.setId(keys.getLong(1));
                    }
                }
            } catch (SQLException e) {
                System.err.println("MySQL Insert Plan Error: " + e.getMessage());
                if (plan.getId() == null) plan.setId(fallbackIdSeq.getAndIncrement());
            }
        } else {
            if (plan.getId() == null) plan.setId(fallbackIdSeq.getAndIncrement());
        }

        planCache.put(plan.getId(), plan);
        return plan;
    }

    // ================= PRODUCTION ENTRY METHODS =================
    public List<ProductionEntry> getAllEntries() {
        if (mysqlConnected) {
            List<ProductionEntry> list = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM production_entries ORDER BY id DESC");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    ProductionEntry entry = new ProductionEntry(
                            rs.getLong("id"),
                            rs.getLong("product_id"),
                            rs.getString("product_name"),
                            rs.getString("entry_date"),
                            rs.getDouble("produced_quantity"),
                            rs.getString("shift"),
                            rs.getString("remarks")
                    );
                    list.add(entry);
                    entryCache.put(entry.getId(), entry);
                }
                return list;
            } catch (SQLException e) {
                System.err.println("MySQL Read Entries Error: " + e.getMessage());
            }
        }
        return new ArrayList<>(entryCache.values());
    }

    public ProductionEntry addEntry(ProductionEntry entry) {
        if ((entry.getProductName() == null || entry.getProductName().isBlank()) && entry.getProductId() != null) {
            getProductById(entry.getProductId()).ifPresent(p -> entry.setProductName(p.getProductName()));
        }

        if (mysqlConnected) {
            String sql = "INSERT INTO production_entries (product_id, product_name, entry_date, produced_quantity, shift, remarks) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setLong(1, entry.getProductId());
                ps.setString(2, entry.getProductName());
                ps.setString(3, entry.getEntryDate());
                ps.setDouble(4, entry.getProducedQuantity());
                ps.setString(5, entry.getShift());
                ps.setString(6, entry.getRemarks());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        entry.setId(keys.getLong(1));
                    }
                }
            } catch (SQLException e) {
                System.err.println("MySQL Insert Entry Error: " + e.getMessage());
                if (entry.getId() == null) entry.setId(fallbackIdSeq.getAndIncrement());
            }
        } else {
            if (entry.getId() == null) entry.setId(fallbackIdSeq.getAndIncrement());
        }

        entryCache.put(entry.getId(), entry);
        return entry;
    }

    // ================= VARIANCE REPORT GENERATION =================
    /**
     * Generates variance analysis across all products.
     * Computes Planned Quantity vs Produced Quantity, Difference, and Achievement %.
     */
    public List<VarianceReportItem> generateVarianceReport() {
        List<Product> allProducts = getAllProducts();
        List<ProductionPlan> allPlans = getAllPlans();
        List<ProductionEntry> allEntries = getAllEntries();

        List<VarianceReportItem> report = new ArrayList<>();

        for (Product product : allProducts) {
            // 1. Calculate total planned quantity for this product
            double totalPlanned = allPlans.stream()
                    .filter(p -> p.getProductId() != null && p.getProductId().equals(product.getId()))
                    .mapToDouble(ProductionPlan::getPlannedQuantity)
                    .sum();

            // 2. Calculate total actual quantity produced for this product
            double totalProduced = allEntries.stream()
                    .filter(e -> e.getProductId() != null && e.getProductId().equals(product.getId()))
                    .mapToDouble(ProductionEntry::getProducedQuantity)
                    .sum();

            // 3. Create Variance Item
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