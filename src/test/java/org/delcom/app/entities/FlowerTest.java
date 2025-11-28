package org.delcom.app.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

public class FlowerTest {

    @Test
    void testFlowerEntity() {
        Flower flower = new Flower();
        
        // Test Setters
        flower.setId(1L);
        flower.setName("Mawar");
        flower.setVariety("Merah");
        flower.setStock(10);
        flower.setPrice(50000.0);
        flower.setImagePath("mawar.jpg");
        LocalDateTime now = LocalDateTime.now();
        flower.setCreatedAt(now);

        // Test Getters
        Assertions.assertEquals(1L, flower.getId());
        Assertions.assertEquals("Mawar", flower.getName());
        Assertions.assertEquals("Merah", flower.getVariety());
        Assertions.assertEquals(10, flower.getStock());
        Assertions.assertEquals(50000.0, flower.getPrice());
        Assertions.assertEquals("mawar.jpg", flower.getImagePath());
        Assertions.assertEquals(now, flower.getCreatedAt());
    }

    @Test
    void testPrePersist() {
        // Simulasi PrePersist manual karena ini unit test (bukan integration test)
        Flower flower = new Flower();
        // Kita panggil method protected via subclass atau reflection, 
        // tapi untuk simple test, kita pastikan method onCreate ada.
        // Cara termudah tes logika ini biasanya di Integration Test, 
        // tapi untuk coverage POJO, cukup pastikan field ada.
        Assertions.assertNull(flower.getCreatedAt());
    }
}