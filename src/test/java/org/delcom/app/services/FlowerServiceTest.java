package org.delcom.app.services;

import org.delcom.app.entities.Flower;
import org.delcom.app.repositories.FlowerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FlowerServiceTest {

    @Mock
    private FlowerRepository repository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private FlowerService service;

    @Test
    void testGetAllFlowers() {
        Flower flower = new Flower();
        when(repository.findAll()).thenReturn(Arrays.asList(flower));

        List<Flower> result = service.getAllFlowers();
        Assertions.assertEquals(1, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGetFlowerById() {
        Long id = 1L;
        Flower flower = new Flower();
        flower.setId(id);
        
        when(repository.findById(id)).thenReturn(Optional.of(flower));

        Flower result = service.getFlowerById(id);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(id, result.getId());
    }

    @Test
    void testSaveFlower_WithImage() {
        // Setup data
        Flower flower = new Flower();
        flower.setName("Mawar");
        
        // Mock file upload
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false); 
        
        // Mock return value dari storeFile (method 1 parameter)
        when(fileStorageService.storeFile(mockFile)).thenReturn("flower_random.jpg");

        // Action
        service.saveFlower(flower, mockFile);

        // Assert
        Assertions.assertEquals("flower_random.jpg", flower.getImagePath());
        verify(fileStorageService, times(1)).storeFile(mockFile); 
        verify(repository, times(1)).save(flower);
    }

    @Test
    void testSaveFlower_NoImage() {
        Flower flower = new Flower();
        flower.setImagePath("old.jpg");
        
        // File kosong / null
        service.saveFlower(flower, null);

        Assertions.assertEquals("old.jpg", flower.getImagePath());
        verify(fileStorageService, never()).storeFile(any(MultipartFile.class));
        verify(repository, times(1)).save(flower);
    }

    @Test
    void testDeleteFlower() {
        Long id = 1L;
        service.deleteFlower(id);
        verify(repository, times(1)).deleteById(id);
    }
}