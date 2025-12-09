package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class StockHistoryTest {

    @Test
    void testStockHistoryEntity() {
        // Test Constructor Lengkap & Getters
        UUID flowerId = UUID.randomUUID();
        StockHistory h1 = new StockHistory(flowerId, "TEST", 5, 10);
        
        assertEquals(flowerId, h1.getFlowerId());
        assertEquals("TEST", h1.getType());
        assertEquals(5, h1.getQuantity());
        assertEquals(10, h1.getFinalStock());

        // Test Constructor Kosong, Setters & Getters
        StockHistory h2 = new StockHistory();
        UUID id = UUID.randomUUID();
        UUID fId = UUID.randomUUID();
        LocalDateTime time = LocalDateTime.now();

        h2.setId(id);
        h2.setFlowerId(fId);
        h2.setType("RESTOCK");
        h2.setQuantity(100);
        h2.setFinalStock(50);
        h2.setRecordedAt(time);

        assertEquals(id, h2.getId());
        assertEquals(fId, h2.getFlowerId());
        assertEquals("RESTOCK", h2.getType());
        assertEquals(100, h2.getQuantity());
        assertEquals(50, h2.getFinalStock());
        assertEquals(time, h2.getRecordedAt());

        // Test @PrePersist onCreate
        StockHistory h3 = new StockHistory();
        h3.onCreate(); 
        assertNotNull(h3.getRecordedAt());
    }
}