package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class StockHistoryTest {

    @Test
    void testStockHistory_AllMethods() {
        UUID id = UUID.randomUUID();
        UUID flowerId = UUID.randomUUID();
        
        StockHistory history = new StockHistory(flowerId, "RESTOCK", 5, 10);
        history.onCreate(); 

        assertEquals(flowerId, history.getFlowerId());
        assertEquals("RESTOCK", history.getType());
        assertEquals(5, history.getQuantity());
        assertEquals(10, history.getFinalStock());
        assertNotNull(history.getRecordedAt()); 
    }
    
    @Test
    void testEmptyConstructor() {
        StockHistory history = new StockHistory();
        assertNull(history.getId());
        assertNull(history.getFlowerId());
    }
}