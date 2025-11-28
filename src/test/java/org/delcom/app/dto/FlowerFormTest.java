package org.delcom.app.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class FlowerFormTest {

    @Test
    void testFlowerForm() {
        FlowerForm form = new FlowerForm();
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

        form.setId(1L);
        form.setName("Anggrek");
        form.setVariety("Bulan");
        form.setStock(5);
        form.setPrice(100000.0);
        form.setImage(file);

        Assertions.assertEquals(1L, form.getId());
        Assertions.assertEquals("Anggrek", form.getName());
        Assertions.assertEquals("Bulan", form.getVariety());
        Assertions.assertEquals(5, form.getStock());
        Assertions.assertEquals(100000.0, form.getPrice());
        Assertions.assertEquals(file, form.getImage());
    }
}