package org.delcom.app.services;

import org.delcom.app.entities.Flower;
import org.delcom.app.entities.StockHistory;
import org.delcom.app.repositories.FlowerRepository;
import org.delcom.app.repositories.StockHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowerServiceTest {

    @Mock private FlowerRepository repository;
    @Mock private StockHistoryRepository historyRepository;
    @Mock private FileStorageService fileStorageService;
    @InjectMocks private FlowerService service;

    @Test
    void testGetAllFlowers() {
        when(repository.findAll()).thenReturn(Arrays.asList(new Flower(), new Flower()));
        assertEquals(2, service.getAllFlowers().size());
    }

    @Test
    void testGetFlowersByUser() {
        UUID userId = UUID.randomUUID();
        when(repository.findAllByUserId(userId)).thenReturn(Arrays.asList(new Flower(), new Flower()));
        assertEquals(2, service.getFlowersByUser(userId).size());
    }

    @Test
    void testGetFlowerById_Found() {
        UUID id = UUID.randomUUID();
        Flower f = new Flower(); f.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(f));
        assertNotNull(service.getFlowerById(id));
    }
    
    @Test
    void testGetFlowerById_NotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertNull(service.getFlowerById(id));
    }

    @Test
    void testGetHistoryByFlower() {
        UUID fid = UUID.randomUUID();
        StockHistory mockHistory = new StockHistory(fid, "TEST", 1, 10);
        
        when(historyRepository.findByFlowerIdOrderByRecordedAtDesc(fid))
            .thenReturn(Collections.singletonList(mockHistory));
        assertEquals(1, service.getHistoryByFlower(fid).size());
    }

    @Test
    void testGetTopSellingFlowersByUser() {
        UUID userId = UUID.randomUUID();

        Flower f1 = new Flower(); f1.setId(UUID.randomUUID()); f1.setFlowerName("Mawar");
        Flower f2 = new Flower(); f2.setId(UUID.randomUUID()); f2.setFlowerName("Melati");
        Flower f3 = new Flower(); f3.setId(UUID.randomUUID()); f3.setFlowerName("Anggrek"); // Bunga ke-3
        
        when(repository.findAllByUserId(userId)).thenReturn(Arrays.asList(f1, f2, f3));

        StockHistory h1 = new StockHistory(f1.getId(), "PENJUALAN", 5, 0);
        StockHistory h2 = new StockHistory(f1.getId(), "RESTOCK", 10, 0); 
        when(historyRepository.findByFlowerIdOrderByRecordedAtDesc(f1.getId()))
            .thenReturn(Arrays.asList(h1, h2));

        when(historyRepository.findByFlowerIdOrderByRecordedAtDesc(f2.getId()))
            .thenReturn(Collections.emptyList());

        when(historyRepository.findByFlowerIdOrderByRecordedAtDesc(f3.getId()))
            .thenReturn(null);

        Map<String, Integer> result = service.getTopSellingFlowersByUser(userId);

        assertEquals(3, result.size());
        assertEquals(5, result.get("Mawar"));
        assertEquals(0, result.get("Melati"));
        assertEquals(0, result.get("Anggrek")); // Pastikan null handled gracefully jadi 0
    }

    @Test
    void testSaveFlower_WithImage() {
        Flower f = new Flower(); f.setStock(10);
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "content".getBytes());
        when(fileStorageService.storeFile(any())).thenReturn("a.jpg");
        service.saveFlower(f, file, null);
        assertEquals("a.jpg", f.getImagePath());
    }

    @Test
    void testSaveFlower_EmptyImage() { 
        Flower f = new Flower(); f.setStock(10);
        MockMultipartFile file = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);
        service.saveFlower(f, file, null);
        verify(fileStorageService, never()).storeFile(any());
    }

    @Test
    void testSaveFlower_NullImage() { 
        Flower f = new Flower(); f.setStock(10);
        service.saveFlower(f, null, null);
        verify(fileStorageService, never()).storeFile(any());
    }

    @Test
    void testSave_NewItem_CreatesHistory() { 
        Flower f = new Flower(); f.setId(UUID.randomUUID()); f.setStock(10);
        service.saveFlower(f, null, null);
        
        ArgumentCaptor<StockHistory> captor = ArgumentCaptor.forClass(StockHistory.class);
        verify(historyRepository).save(captor.capture());
        assertEquals("BARANG MASUK", captor.getValue().getType());
    }

    @Test
    void testSave_NewItem_NullStock_NoHistory() { 
        Flower f = new Flower(); f.setId(UUID.randomUUID()); f.setStock(null);
        service.saveFlower(f, null, null);
        verify(historyRepository, never()).save(any());
    }

    @Test
    void testSave_Edit_StockReduced_Penjualan() { 
        Flower f = new Flower(); f.setId(UUID.randomUUID()); f.setStock(8);
        service.saveFlower(f, null, 10); 
        
        ArgumentCaptor<StockHistory> captor = ArgumentCaptor.forClass(StockHistory.class);
        verify(historyRepository).save(captor.capture());
        assertEquals("PENJUALAN", captor.getValue().getType());
        assertEquals(2, captor.getValue().getQuantity());
    }

    @Test
    void testSave_Edit_StockIncreased_Restock() { 
        Flower f = new Flower(); f.setId(UUID.randomUUID()); f.setStock(15);
        service.saveFlower(f, null, 10); 
        
        ArgumentCaptor<StockHistory> captor = ArgumentCaptor.forClass(StockHistory.class);
        verify(historyRepository).save(captor.capture());
        assertEquals("RESTOCK", captor.getValue().getType());
        assertEquals(5, captor.getValue().getQuantity());
    }

    @Test
    void testSave_Edit_StockSame() { 
        Flower f = new Flower(); f.setStock(10);
        service.saveFlower(f, null, 10);
        verify(historyRepository, never()).save(any());
    }

    @Test
    void testDeleteFlower() {
        UUID id = UUID.randomUUID();
        service.deleteFlower(id);
        verify(repository).deleteById(id);
    }
}