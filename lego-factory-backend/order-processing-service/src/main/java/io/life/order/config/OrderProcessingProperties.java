package io.life.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the Order Processing Service.
 */
@Component
@ConfigurationProperties(prefix = "app.order")
public class OrderProcessingProperties {

    private OrderPrefixes prefixes = new OrderPrefixes();

    public OrderPrefixes getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(OrderPrefixes prefixes) {
        this.prefixes = prefixes;
    }

    /**
     * Order number prefixes for different order types
     */
    public static class OrderPrefixes {
        private String manufacturing = "MFG";
        private String assembly = "ASM";
        private String control = "CTL";
        private String supplier = "SUP";

        public String getManufacturing() { return manufacturing; }
        public void setManufacturing(String manufacturing) { this.manufacturing = manufacturing; }

        public String getAssembly() { return assembly; }
        public void setAssembly(String assembly) { this.assembly = assembly; }

        public String getControl() { return control; }
        public void setControl(String control) { this.control = control; }

        public String getSupplier() { return supplier; }
        public void setSupplier(String supplier) { this.supplier = supplier; }
    }
}
