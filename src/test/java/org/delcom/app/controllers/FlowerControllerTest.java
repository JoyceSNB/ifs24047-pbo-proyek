package org.delcom.app.controllers;

import org.delcom.app.dto.FlowerForm;
import org.delcom.app.entities.Flower;
import org.delcom.app.services.FlowerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile; // Pastikan import ini ada

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FlowerControllerTest {

    @Mock
    private FlowerService flowerService;

    @InjectMocks
    private FlowerController flowerController;

    @Test
    void testSaveFlower_CreateNew() {
        // Setup Dummy File
        MockMultipartFile image = new MockMultipartFile("image", "foto.jpg", "image/jpeg", "dummy-bytes".getBytes());
        
        FlowerForm form = new FlowerForm();
        form.setName("Anggrek");
        form.setPrice(50000.0);
        form.setStock(10);
        form.setImage(image);

        // Action
        String viewName = flowerController.saveFlower(form);

        // Assert
        assertEquals("redirect:/", viewName);
        verify(flowerService).saveFlower(any(Flower.class), eq(image));
    }

    @Test
    void testSaveFlower_UpdateExisting() {
        Long id = 10L;
        FlowerForm form = new FlowerForm();
        form.setId(id);
        form.setName("Mawar Update");
        
        Flower existingFlower = new Flower();
        existingFlower.setId(id);
        
        // Mock service getById
        when(flowerService.getFlowerById(id)).thenReturn(existingFlower);

        String viewName = flowerController.saveFlower(form);

        assertEquals("redirect:/", viewName);
        verify(flowerService).getFlowerById(id);
        verify(flowerService).saveFlower(eq(existingFlower), any());
    }

    @Test
    void testDeleteFlower() {
        FlowerForm form = new FlowerForm();
        form.setId(5L);

        String viewName = flowerController.deleteFlower(form);

        assertEquals("redirect:/", viewName);
        verify(flowerService).deleteFlower(5L);
    }
}