# Order Processing Service - Developer's Extension Guide

## 📖 How to Extend the Service

This guide explains how to add new features and extend the Order Processing Service.

---

## 🔧 Adding a New Order Type

Suppose you want to add a "Packaging Order" type. Here's how to do it:

### Step 1: Create the Entity Class

Create `src/main/java/io/life/order/entity/PackagingOrder.java`:

```java
package io.life.order.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "packaging_orders")
public class PackagingOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String packagingOrderNumber;
    
    @Column(nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private String status = "CREATED";
    
    @Column(name = "assigned_workstation_id")
    private Long assignedWorkstationId;
    
    @Column(name = "packaging_type")
    private String packagingType;
    
    @Column(name = "boxes_required")
    private Integer boxesRequired;
    
    @Column(name = "boxes_used")
    private Integer boxesUsed;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "expected_completion_date")
    private LocalDate expectedCompletionDate;
    
    @Column(name = "actual_completion_date")
    private LocalDate actualCompletionDate;
    
    @Column(columnDefinition = "TEXT")
    private String operatorNotes;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPackagingOrderNumber() { return packagingOrderNumber; }
    public void setPackagingOrderNumber(String packagingOrderNumber) { 
        this.packagingOrderNumber = packagingOrderNumber; 
    }
    
    // ... (continue for all fields)
}
```

### Step 2: Create the Repository

Create `src/main/java/io/life/order/repository/PackagingOrderRepository.java`:

```java
package io.life.order.repository;

import io.life.order.entity.PackagingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackagingOrderRepository extends JpaRepository<PackagingOrder, Long> {
    
    Optional<PackagingOrder> findByPackagingOrderNumber(String packagingOrderNumber);
    
    List<PackagingOrder> findByStatus(String status);
    
    List<PackagingOrder> findByAssignedWorkstationId(Long workstationId);
    
    List<PackagingOrder> findByStatusAndAssignedWorkstationId(String status, Long workstationId);
}
```

### Step 3: Create the DTO

Create `src/main/java/io/life/order/dto/PackagingOrderDTO.java`:

```java
package io.life.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackagingOrderDTO {
    
    private Long id;
    private String packagingOrderNumber;
    private Long productId;
    private Integer quantity;
    private String status;
    private Long assignedWorkstationId;
    private String packagingType;
    private Integer boxesRequired;
    private Integer boxesUsed;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedCompletionDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualCompletionDate;
    
    private String operatorNotes;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
```

### Step 4: Create the Service

Create `src/main/java/io/life/order/service/PackagingOrderService.java`:

```java
package io.life.order.service;

import io.life.order.dto.PackagingOrderDTO;
import io.life.order.entity.PackagingOrder;
import io.life.order.exception.EntityNotFoundException;
import io.life.order.exception.InvalidOrderStateException;
import io.life.order.repository.PackagingOrderRepository;
import io.life.order.util.OrderNumberGenerator;
import io.life.order.util.OrderStateValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PackagingOrderService {
    
    private final PackagingOrderRepository repository;
    private final OrderNumberGenerator numberGenerator;
    
    public PackagingOrderService(PackagingOrderRepository repository, 
                                OrderNumberGenerator numberGenerator) {
        this.repository = repository;
        this.numberGenerator = numberGenerator;
    }
    
    // CRUD Operations
    public PackagingOrderDTO createOrder(PackagingOrderDTO orderDTO) {
        PackagingOrder order = new PackagingOrder();
        order.setPackagingOrderNumber(numberGenerator.generatePackagingOrderNumber());
        order.setProductId(orderDTO.getProductId());
        order.setQuantity(orderDTO.getQuantity());
        order.setStatus("CREATED");
        order.setAssignedWorkstationId(orderDTO.getAssignedWorkstationId());
        order.setPackagingType(orderDTO.getPackagingType());
        order.setBoxesRequired(orderDTO.getBoxesRequired());
        order.setExpectedCompletionDate(orderDTO.getExpectedCompletionDate());
        
        PackagingOrder savedOrder = repository.save(order);
        return convertToDTO(savedOrder);
    }
    
    public Optional<PackagingOrderDTO> getOrderById(Long id) {
        return repository.findById(id).map(this::convertToDTO);
    }
    
    public Optional<PackagingOrderDTO> getOrderByNumber(String packagingOrderNumber) {
        return repository.findByPackagingOrderNumber(packagingOrderNumber)
                .map(this::convertToDTO);
    }
    
    public List<PackagingOrderDTO> getOrdersByStatus(String status) {
        return repository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<PackagingOrderDTO> getOrdersByWorkstation(Long workstationId) {
        return repository.findByAssignedWorkstationId(workstationId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // State Transitions
    public PackagingOrderDTO startPackaging(Long id) {
        PackagingOrder order = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PackagingOrder", id));
        
        OrderStateValidator.validateTransition(order.getStatus(), "IN_PROGRESS", "PACKAGING");
        order.setStatus("IN_PROGRESS");
        order.setStartDate(LocalDate.now());
        
        return convertToDTO(repository.save(order));
    }
    
    public PackagingOrderDTO completePackaging(Long id) {
        PackagingOrder order = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PackagingOrder", id));
        
        OrderStateValidator.validateTransition(order.getStatus(), "COMPLETED", "PACKAGING");
        order.setStatus("COMPLETED");
        order.setActualCompletionDate(LocalDate.now());
        
        return convertToDTO(repository.save(order));
    }
    
    public PackagingOrderDTO updateBoxesUsed(Long id, Integer boxesUsed) {
        PackagingOrder order = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PackagingOrder", id));
        
        order.setBoxesUsed(boxesUsed);
        return convertToDTO(repository.save(order));
    }
    
    public PackagingOrderDTO updateOperatorNotes(Long id, String notes) {
        PackagingOrder order = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PackagingOrder", id));
        
        order.setOperatorNotes(notes);
        return convertToDTO(repository.save(order));
    }
    
    // Helper method
    private PackagingOrderDTO convertToDTO(PackagingOrder order) {
        return new PackagingOrderDTO(
            order.getId(),
            order.getPackagingOrderNumber(),
            order.getProductId(),
            order.getQuantity(),
            order.getStatus(),
            order.getAssignedWorkstationId(),
            order.getPackagingType(),
            order.getBoxesRequired(),
            order.getBoxesUsed(),
            order.getStartDate(),
            order.getExpectedCompletionDate(),
            order.getActualCompletionDate(),
            order.getOperatorNotes(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}
```

### Step 5: Create the Controller

Create `src/main/java/io/life/order/controller/PackagingOrderController.java`:

```java
package io.life.order.controller;

import io.life.order.dto.PackagingOrderDTO;
import io.life.order.service.PackagingOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/packaging-orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PackagingOrderController {
    
    private final PackagingOrderService service;
    
    public PackagingOrderController(PackagingOrderService service) {
        this.service = service;
    }
    
    @GetMapping
    public ResponseEntity<List<PackagingOrderDTO>> getAllOrders() {
        List<PackagingOrderDTO> orders = service.getAllOrders();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PackagingOrderDTO> getOrderById(@PathVariable Long id) {
        Optional<PackagingOrderDTO> order = service.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<PackagingOrderDTO> createOrder(@RequestBody PackagingOrderDTO orderDTO) {
        try {
            PackagingOrderDTO createdOrder = service.createOrder(orderDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/{id}/start")
    public ResponseEntity<PackagingOrderDTO> startPackaging(@PathVariable Long id) {
        try {
            PackagingOrderDTO order = service.startPackaging(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/{id}/complete")
    public ResponseEntity<PackagingOrderDTO> completePackaging(@PathVariable Long id) {
        try {
            PackagingOrderDTO order = service.completePackaging(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PatchMapping("/{id}/boxes-used")
    public ResponseEntity<PackagingOrderDTO> updateBoxesUsed(
            @PathVariable Long id,
            @RequestBody BoxesUsedRequest request) {
        try {
            PackagingOrderDTO order = service.updateBoxesUsed(id, request.getBoxesUsed());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PatchMapping("/{id}/notes")
    public ResponseEntity<PackagingOrderDTO> updateNotes(
            @PathVariable Long id,
            @RequestBody NotesRequest request) {
        try {
            PackagingOrderDTO order = service.updateOperatorNotes(id, request.getNotes());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    public static class BoxesUsedRequest {
        private Integer boxesUsed;
        public Integer getBoxesUsed() { return boxesUsed; }
        public void setBoxesUsed(Integer boxesUsed) { this.boxesUsed = boxesUsed; }
    }
    
    public static class NotesRequest {
        private String notes;
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
```

### Step 6: Update OrderNumberGenerator

Add to `OrderNumberGenerator.java`:

```java
public String generatePackagingOrderNumber() {
    return generateOrderNumber(
        properties.getPrefixes().getPackaging(),
        packagingSequence
    );
}

// Add this field:
private final AtomicLong packagingSequence = new AtomicLong(1000);
```

### Step 7: Update OrderStateValidator

Add to `OrderStateValidator.java`:

```java
static {
    // ... existing code ...
    
    // Packaging order transitions
    PACKAGING_TRANSITIONS.put("CREATED", new HashSet<>(Arrays.asList("IN_PROGRESS", "CANCELLED")));
    PACKAGING_TRANSITIONS.put("IN_PROGRESS", new HashSet<>(Arrays.asList("COMPLETED", "HALTED")));
    PACKAGING_TRANSITIONS.put("COMPLETED", new HashSet<>(Arrays.asList()));
    PACKAGING_TRANSITIONS.put("HALTED", new HashSet<>(Arrays.asList("IN_PROGRESS", "CANCELLED")));
    PACKAGING_TRANSITIONS.put("CANCELLED", new HashSet<>(Arrays.asList()));
}

public static void validatePackagingTransition(String currentState, String newState) {
    validateTransition(currentState, newState, PACKAGING_TRANSITIONS, "Packaging Order");
}
```

### Step 8: Update Configuration

In `application.properties`, add:

```properties
app.order.packaging.prefix=PKG
```

---

## 🔌 Adding Custom Methods to a Service

### Example: Add Bulk Status Update

Add to any service (e.g., `ManufacturingOrderService`):

```java
@Transactional
public List<ManufacturingOrderDTO> updateStatusBulk(List<Long> ids, String newStatus) {
    return ids.stream()
        .map(id -> {
            ManufacturingOrder order = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ManufacturingOrder", id));
            OrderStateValidator.validateManufacturingTransition(order.getStatus(), newStatus);
            order.setStatus(newStatus);
            return convertToDTO(repository.save(order));
        })
        .collect(Collectors.toList());
}
```

Add endpoint in controller:

```java
@PostMapping("/bulk/status-update")
public ResponseEntity<List<ManufacturingOrderDTO>> updateStatusBulk(
        @RequestBody BulkStatusUpdateRequest request) {
    try {
        List<ManufacturingOrderDTO> orders = service.updateStatusBulk(
            request.getIds(),
            request.getNewStatus()
        );
        return ResponseEntity.ok(orders);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}

public static class BulkStatusUpdateRequest {
    private List<Long> ids;
    private String newStatus;
    
    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }
    
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
}
```

---

## 🔐 Adding Custom Validation

Create `src/main/java/io/life/order/validator/OrderValidator.java`:

```java
package io.life.order.validator;

import io.life.order.exception.InvalidOperationException;

public class OrderValidator {
    
    public static void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidOperationException("Quantity must be greater than 0");
        }
    }
    
    public static void validateWorkstationId(Long workstationId) {
        if (workstationId == null || workstationId <= 0) {
            throw new InvalidOperationException("Invalid workstation ID");
        }
    }
    
    public static void validateOperatorNotes(String notes) {
        if (notes != null && notes.length() > 500) {
            throw new InvalidOperationException("Operator notes cannot exceed 500 characters");
        }
    }
}
```

Use in service:

```java
public ManufacturingOrderDTO createOrder(ManufacturingOrderDTO orderDTO) {
    OrderValidator.validateQuantity(orderDTO.getQuantity());
    OrderValidator.validateWorkstationId(orderDTO.getAssignedWorkstationId());
    
    // ... rest of creation logic
}
```

---

## 📊 Adding Database Migrations (Future Feature)

When ready to use Flyway/Liquibase:

Create `src/main/resources/db/migration/V2__CreatePackagingOrders.sql`:

```sql
CREATE TABLE packaging_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    packaging_order_number VARCHAR(50) UNIQUE NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    assigned_workstation_id BIGINT,
    packaging_type VARCHAR(50),
    boxes_required INT,
    boxes_used INT,
    start_date DATE,
    expected_completion_date DATE,
    actual_completion_date DATE,
    operator_notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_status (status),
    INDEX idx_workstation (assigned_workstation_id),
    INDEX idx_created_at (created_at)
);
```

---

## 📝 Best Practices for Extensions

### 1. **Follow Naming Conventions**
- Entity: `YourOrderType.java`
- Repository: `YourOrderTypeRepository.java`
- Service: `YourOrderTypeService.java`
- Controller: `YourOrderTypeController.java`
- DTO: `YourOrderTypeDTO.java`

### 2. **Maintain Consistency**
- Use same status values where appropriate
- Follow existing code style
- Include Javadoc comments

### 3. **Add Proper Indexing**
```java
@Column(name = "field_name")
@Index(name = "idx_field_name")
private String fieldName;
```

### 4. **Handle Transactions**
```java
@Transactional
public void complexOperation() {
    // All database operations here are atomic
}
```

### 5. **Use DTOs**
- Never expose entities directly in APIs
- Always use DTOs for request/response

### 6. **Add Tests**
For each new feature, create:
- Unit tests
- Integration tests
- Controller tests

---

## 🚀 Testing Your Extension

```bash
# Build
mvn clean package

# Run tests
mvn test

# Run specific test
mvn test -Dtest=PackagingOrderControllerTest

# Generate coverage
mvn jacoco:report
```

---

## 📚 Common Extension Scenarios

### Scenario 1: Add Order Filtering by Date Range
```java
// In Repository
List<PackagingOrder> findByCreatedAtBetween(
    LocalDateTime start, 
    LocalDateTime end
);

// In Service
public List<PackagingOrderDTO> getOrdersByDateRange(
    LocalDateTime start, 
    LocalDateTime end) {
    return repository.findByCreatedAtBetween(start, end).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
}

// In Controller
@GetMapping("/date-range")
public ResponseEntity<List<PackagingOrderDTO>> getByDateRange(
        @RequestParam String startDate,
        @RequestParam String endDate) {
    // Parse dates and call service
}
```

### Scenario 2: Add Order Search
```java
// In Repository
@Query("SELECT o FROM PackagingOrder o WHERE " +
       "LOWER(o.packagingOrderNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
       "OR o.productId = :productId")
List<PackagingOrder> search(@Param("search") String search, 
                           @Param("productId") Long productId);

// In Service
public List<PackagingOrderDTO> search(String searchTerm, Long productId) {
    return repository.search(searchTerm, productId).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
}
```

---

**Happy extending!** 🎉
