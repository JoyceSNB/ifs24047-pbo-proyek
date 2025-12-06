package org.delcom.app.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_history")
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID flowerId;

    @Column(nullable = false)
    private String type; 

    @Column(nullable = false)
    private Integer quantity; 

    @Column(nullable = false)
    private Integer finalStock; 

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        this.recordedAt = LocalDateTime.now();
    }

    // Constructor
    public StockHistory() {}
    public StockHistory(UUID flowerId, String type, Integer quantity, Integer finalStock) {
        this.flowerId = flowerId;
        this.type = type;
        this.quantity = quantity;
        this.finalStock = finalStock;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getFlowerId() { return flowerId; }
    public String getType() { return type; }
    public Integer getQuantity() { return quantity; }
    public Integer getFinalStock() { return finalStock; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}

