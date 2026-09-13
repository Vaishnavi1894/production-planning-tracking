package com.production.controller;

import com.production.model.Product;
import com.production.repository.DataStore;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Controller for Product Master operations.
 * Allows retrieving existing products and registering new ones.
 */
@Controller("/api/products")
public class ProductController {

    private final DataStore dataStore;

    @Inject
    public ProductController(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * GET /api/products
     * Returns the full list of products from Product Master.
     */
    @Get
    public List<Product> getAllProducts() {
        return dataStore.getAllProducts();
    }

    /**
     * POST /api/products
     * Creates a new product in the Product Master.
     */
    @Post
    public HttpResponse<Product> createProduct(@Body Product product) {
        if (product == null || product.getProductName() == null || product.getProductName().isBlank()) {
            return HttpResponse.badRequest();
        }
        Product saved = dataStore.addProduct(product);
        return HttpResponse.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * DELETE /api/products/{id}
     * Removes a product by ID.
     */
    @Delete("/{id}")
    public HttpResponse<Void> deleteProduct(@PathVariable Long id) {
        boolean removed = dataStore.deleteProduct(id);
        if (removed) {
            return HttpResponse.noContent();
        } else {
            return HttpResponse.notFound();
        }
    }
}
