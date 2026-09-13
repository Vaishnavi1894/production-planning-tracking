package com.production.controller;

import com.production.model.ProductionPlan;
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
 * Controller for Production Planning.
 * Handles setting production targets and listing active targets.
 */
@Controller("/api/plans")
public class PlanController {

    private final DataStore dataStore;

    @Inject
    public PlanController(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * GET /api/plans
     * Returns all production plans.
     */
    @Get
    public List<ProductionPlan> getAllPlans() {
        return dataStore.getAllPlans();
    }

    /**
     * POST /api/plans
     * Creates a new production plan.
     */
    @Post
    public HttpResponse<ProductionPlan> createPlan(@Body ProductionPlan plan) {
        if (plan == null || plan.getProductId() == null || plan.getPlannedQuantity() <= 0) {
            return HttpResponse.badRequest();
        }
        ProductionPlan saved = dataStore.addPlan(plan);
        return HttpResponse.status(HttpStatus.CREATED).body(saved);
    }
}
