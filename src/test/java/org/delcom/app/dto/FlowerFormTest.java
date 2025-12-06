package org.delcom.app.dto;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FlowerFormTest {

    @Test
    void testFlowerForm() {
        FlowerForm form = new FlowerForm();
        UUID id = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

        form.setId(id);
        form.setFlowerName("Melati");
        form.setSpecies("Jasminum sambac");
        form.setPrice(25000.0);
        form.setStock(100);
        form.setDescription("Wangi semerbak");
        form.setImage(file);

        assertEquals(id, form.getId());
        assertEquals("Melati", form.getFlowerName());
        assertEquals("Jasminum sambac", form.getSpecies());
        assertEquals(25000.0, form.getPrice());
        assertEquals(100, form.getStock());
        assertEquals("Wangi semerbak", form.getDescription());
        assertNotNull(form.getImage());
        assertEquals("test.jpg", form.getImage().getOriginalFilename());
    }
}