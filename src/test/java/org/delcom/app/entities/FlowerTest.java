package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FlowerTest {

    @Test
    void testFlowerEntity_AllAttributes() {
        Flower flower = new Flower();
        
        // 1. Setup Data Dummy (10 Atribut)
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        flower.setId(id);
        flower.setUserId(userId);
        flower.setFlowerName("Anggrek Bulan");
        flower.setSpecies("Phalaenopsis amabilis");
        flower.setPrice(150000.0);
        flower.setStock(50);
        flower.setImagePath("anggrek.jpg");
        flower.setDescription("Anggrek cantik berwarna putih");
        flower.setCreatedAt(now);
        flower.setUpdatedAt(now);

        // 2. Assertions 
        assertEquals(id, flower.getId());
        assertEquals(userId, flower.getUserId());
        assertEquals("Anggrek Bulan", flower.getFlowerName());
        assertEquals("Phalaenopsis amabilis", flower.getSpecies());
        assertEquals(150000.0, flower.getPrice());
        assertEquals(50, flower.getStock());
        assertEquals("anggrek.jpg", flower.getImagePath());
        assertEquals("Anggrek cantik berwarna putih", flower.getDescription());
        assertEquals(now, flower.getCreatedAt());
        assertEquals(now, flower.getUpdatedAt());
    }

    @Test
    void testLifecycleMethods() {
        Flower flower = new Flower();
        
        // Test onCreate (PrePersist)
        flower.onCreate();
        assertNotNull(flower.getCreatedAt());
        assertNotNull(flower.getUpdatedAt());

        // Test onUpdate (PreUpdate)
        LocalDateTime oldTime = flower.getUpdatedAt();
        
        try { Thread.sleep(10); } catch (InterruptedException e) {}
        
        flower.onUpdate();
        assertNotEquals(oldTime, flower.getUpdatedAt());
    }
}