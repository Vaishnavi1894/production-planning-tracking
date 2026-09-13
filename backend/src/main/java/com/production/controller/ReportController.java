package com.production.controller;

import com.production.model.VarianceReportItem;
import com.production.repository.DataStore;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Controller for Generating Variance & Achievement Reports.
 * Fulfills the primary project outcome:
 * - Planned Quantity
 * - Produced Quantity
 * - Difference
 * - Achievement %
 */
@Controller("/api/reports")
public class ReportController {

    private final DataStore dataStore;

    @Inject
    public ReportController(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * GET /api/reports/variance
     * Computes and returns the variance report for all products.
     */
    @Get("/variance")
    public List<VarianceReportItem> getVarianceReport() {
        return dataStore.generateVarianceReport();
    }
}
