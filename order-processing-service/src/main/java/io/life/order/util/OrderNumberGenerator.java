package io.life.order.util;

import io.life.order.config.OrderProcessingProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for generating unique order numbers.
 * Format: PREFIX-YYYYMMDD-SEQUENCE
 */
@Component
public class OrderNumberGenerator {

    private final OrderProcessingProperties properties;
    private final AtomicLong manufacturingSequence = new AtomicLong(1000);
    private final AtomicLong assemblySequence = new AtomicLong(1000);
    private final AtomicLong controlSequence = new AtomicLong(1000);
    private final AtomicLong supplierSequence = new AtomicLong(1000);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public OrderNumberGenerator(OrderProcessingProperties properties) {
        this.properties = properties;
    }

    /**
     * Generate a unique manufacturing order number
     */
    public String generateManufacturingOrderNumber() {
        return generateOrderNumber(
                properties.getPrefixes().getManufacturing(),
                manufacturingSequence
        );
    }

    /**
     * Generate a unique assembly order number
     */
    public String generateAssemblyOrderNumber() {
        return generateOrderNumber(
                properties.getPrefixes().getAssembly(),
                assemblySequence
        );
    }

    /**
     * Generate a unique production control order number
     */
    public String generateProductionControlOrderNumber() {
        return generateOrderNumber(
                properties.getPrefixes().getControl(),
                controlSequence
        );
    }

    /**
     * Generate a unique supplier order number
     */
    public String generateSupplierOrderNumber() {
        return generateOrderNumber(
                properties.getPrefixes().getSupplier(),
                supplierSequence
        );
    }

    /**
     * Generate order number with format: PREFIX-YYYYMMDD-SEQUENCE
     */
    private String generateOrderNumber(String prefix, AtomicLong sequence) {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        long seqNum = sequence.incrementAndGet();
        return String.format("%s-%s-%d", prefix, dateStr, seqNum);
    }
}
