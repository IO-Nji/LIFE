package io.life.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for communicating with the inventory-service.
 * Handles stock checks and updates during order fulfillment.
 */
@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final RestTemplate restTemplate;

    @Value("${inventory.service.url:http://localhost:8014}")
    private String inventoryServiceUrl;

    public InventoryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Check if sufficient stock is available for a product at a workstation.
     *
     * @param workstationId The workstation ID
     * @param itemId        The product/item ID
     * @param quantity      The quantity needed
     * @return true if stock is available, false otherwise
     */
    public boolean checkStock(Long workstationId, Long itemId, Integer quantity) {
        try {
            String url = inventoryServiceUrl + "/api/stock/check?workstationId=" + workstationId
                    + "&itemId=" + itemId + "&quantity=" + quantity;
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            logger.info("Stock check for workstation {} item {} qty {}: {}", workstationId, itemId, quantity, result);
            return result != null && result;
        } catch (RestClientException e) {
            logger.error("Failed to check stock with inventory-service", e);
            return false;
        }
    }

    /**
     * Update inventory after fulfillment.
     *
     * @param workstationId The workstation ID
     * @param itemId        The product/item ID
     * @param quantity      The quantity to deduct (positive number)
     * @return true if update was successful, false otherwise
     */
    public boolean updateStock(Long workstationId, Long itemId, Integer quantity) {
        try {
            String url = inventoryServiceUrl + "/api/stock/update";
            Map<String, Object> request = new HashMap<>();
            request.put("workstationId", workstationId);
            request.put("itemId", itemId);
            request.put("quantity", -quantity); // Negative to deduct from stock
            
            restTemplate.postForObject(url, request, String.class);
            logger.info("Stock updated for workstation {} item {} qty {}", workstationId, itemId, quantity);
            return true;
        } catch (RestClientException e) {
            logger.error("Failed to update stock with inventory-service", e);
            return false;
        }
    }

    /**
     * Get available stock for an item at a workstation.
     *
     * @param workstationId The workstation ID
     * @param itemId        The product/item ID
     * @return Available quantity, or -1 if unable to check
     */
    public Integer getAvailableStock(Long workstationId, Long itemId) {
        try {
            String url = inventoryServiceUrl + "/api/stock/available?workstationId=" + workstationId
                    + "&itemId=" + itemId;
            Integer available = restTemplate.getForObject(url, Integer.class);
            logger.info("Available stock for workstation {} item {}: {}", workstationId, itemId, available);
            return available != null ? available : 0;
        } catch (RestClientException e) {
            logger.error("Failed to get available stock from inventory-service", e);
            return -1;
        }
    }
}
