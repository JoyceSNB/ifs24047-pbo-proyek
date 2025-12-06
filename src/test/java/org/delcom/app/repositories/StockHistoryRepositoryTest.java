package org.delcom.app.repositories;

import org.delcom.app.entities.StockHistory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional 
class StockHistoryRepositoryTest {

    @Autowired
    private StockHistoryRepository repository;

    @Test
    void testFindByFlowerIdOrderByRecordedAtDesc() {
        UUID flowerId = UUID.randomUUID();
    
        StockHistory h1 = new StockHistory(flowerId, "PENJUALAN", 2, 8);
        StockHistory h2 = new StockHistory(flowerId, "RESTOCK", 5, 13);
        
        repository.save(h1);
        repository.save(h2); 

        List<StockHistory> list = repository.findByFlowerIdOrderByRecordedAtDesc(flowerId);

        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(h -> h.getType().equals("RESTOCK")));
    }
}

