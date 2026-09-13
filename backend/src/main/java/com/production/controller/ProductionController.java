package com.production.controller;

import com.production.model.ProductionEntry;
import com.production.repository.DataStore;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Controller for Daily Production Logging.
 * Handles recording actual units manufactured on a daily/shift basis.
 */
@Controller("/api/production")
public class ProductionController {

    private final DataStore dataStore;

    @Inject
    public ProductionController(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * GET /api/production
     * Returns all recorded daily production entries.
     */
    @Get
    public List<ProductionEntry> getAllEntries() {
        return dataStore.getAllEntries();
    }

    /**
     * POST /api/production
     * Records a new daily production entry.
     */
    @Post
    public HttpResponse<ProductionEntry> recordProduction(@Body ProductionEntry entry) {
        if (entry == null || entry.getProductId() == null || entry.getProducedQuantity() < 0) {
            return HttpResponse.badRequest();
        }
        ProductionEntry saved = dataStore.addEntry(entry);
        return HttpResponse.status(HttpStatus.CREATED).body(saved);
    }
}
