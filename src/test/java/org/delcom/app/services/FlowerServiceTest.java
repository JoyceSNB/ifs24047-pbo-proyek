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

    // --- 1. Basic Tests ---
    @Test
    void testGetAllFlowers() {
        when(repository.findAll()).thenReturn(Arrays.asList(new Flower(), new Flower()));
        assertEquals(2, service.getAllFlowers().size());
    }

    @Test
    void testGetFlowersByUser() {
        UUID userId = UUID.randomUUID();
        when(repository.findAllByUserIdOrderByCreatedAtAsc(userId)).thenReturn(Collections.emptyList());
        assertTrue(service.getFlowersByUser(userId).isEmpty());
    }

    @Test
    void testGetFlowerById() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(new Flower()));
        assertNotNull(service.getFlowerById(id));
        
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertNull(service.getFlowerById(id));
    }

    @Test
    void testGetHistoryByFlower() {
        UUID id = UUID.randomUUID();
        service.getHistoryByFlower(id);
        verify(historyRepository).findByFlowerIdOrderByRecordedAtDesc(id);
    }

    @Test
    void testDeleteFlower() {
        UUID id = UUID.randomUUID();
        service.deleteFlower(id);
        verify(repository).deleteById(id);
    }

    // --- 2. Search Flowers (Branch Coverage) ---
    @Test
    void testSearchFlowers() {
        UUID userId = UUID.randomUUID();
        
        // Branch 1: Keyword Valid 
        when(repository.findByUserIdAndFlowerNameContainingIgnoreCaseOrderByCreatedAtAsc(userId, "Rose"))
            .thenReturn(Collections.singletonList(new Flower()));
        assertEquals(1, service.searchFlowers(userId, "Rose").size());

        // Branch 2: Keyword Null 
        service.searchFlowers(userId, null);
        
        // Branch 3: Keyword Empty 
        service.searchFlowers(userId, "   ");

        // Verify: else dipanggil 2 kali
        verify(repository, times(2)).findAllByUserIdOrderByCreatedAtAsc(userId);
    }

    // --- 3. Top Selling (FULL BRANCH COVERAGE) ---
    @Test
    void testGetTopSellingFlowersByUser() {
        UUID userId = UUID.randomUUID();
        Flower f1 = new Flower(); f1.setId(UUID.randomUUID()); f1.setFlowerName("A");
        Flower f2 = new Flower(); f2.setId(UUID.randomUUID()); f2.setFlowerName("B");
        Flower f3 = new Flower(); f3.setId(UUID.randomUUID()); f3.setFlowerName("C");
        
        when(repository.findAllByUserIdOrderByCreatedAtAsc(userId)).thenReturn(Arrays.asList(f1, f2, f3));

        // F1: History Campuran (PENJUALAN & RESTOCK)
        // Ini MENJAMIN branch 'if (PENJUALAN)' tereksekusi True DAN False
        StockHistory h1 = new StockHistory(f1.getId(), "PENJUALAN", 5, 0); // True Branch
        StockHistory h2 = new StockHistory(f1.getId(), "RESTOCK", 10, 0);  // False Branch (Penting!)
        
        when(historyRepository.findByFlowerIdOrderByRecordedAtDesc(f1.getId())).thenReturn(Arrays.asList(h1, h2));

        // F2: History NULL (Cover Branch: histories != null -> false)
        when(historyRepository.findByFlowerIdOrderByRecordedAtDesc(f2.getId())).thenReturn(null);

        // F3: History KOSONG (Cover Branch: empty loop)
        when(historyRepository.findByFlowerIdOrderByRecordedAtDesc(f3.getId())).thenReturn(Collections.emptyList());

        Map<String, Integer> res = service.getTopSellingFlowersByUser(userId);
        
        // Verify Calculations
        assertEquals(5, res.get("A")); // Hanya 5 (RESTOCK tidak dihitung)
        assertEquals(0, res.get("B")); 
        assertEquals(0, res.get("C")); 
    }

    // --- 4. Save Flower (Branch Coverage) ---
    @Test
    void testSaveFlower_Logic() {
        Flower f = new Flower(); f.setId(UUID.randomUUID()); f.setStock(10);
        
        // Case 1: Data Baru (Stock != null) -> Save History BARANG MASUK
        service.saveFlower(f, null, null);
        verify(historyRepository).save(argThat(h -> h.getType().equals("BARANG MASUK")));
        
        // Case 2: Data Baru (Stock == null) -> No History
        f.setStock(null);
        service.saveFlower(f, null, null);
        
        // Case 3: Edit Data (Stock Sama) -> No History
        f.setStock(10);
        service.saveFlower(f, null, 10);
        
        // Case 4: Edit Data (Stock Berkurang) -> PENJUALAN
        f.setStock(5);
        service.saveFlower(f, null, 10); // Old 10 -> New 5
        verify(historyRepository).save(argThat(h -> h.getType().equals("PENJUALAN")));
        
        // Case 5: Edit Data (Stock Bertambah) -> RESTOCK
        f.setStock(15);
        service.saveFlower(f, null, 10); // Old 10 -> New 15
        verify(historyRepository).save(argThat(h -> h.getType().equals("RESTOCK")));
        
        // Case 6: Upload Image Valid
        MockMultipartFile file = new MockMultipartFile("f", "a.jpg", "type", "content".getBytes());
        service.saveFlower(f, file, null);
        verify(fileStorageService).storeFile(any());

        // Case 7: Upload Image Empty
        MockMultipartFile emptyFile = new MockMultipartFile("f", "", "type", new byte[0]);
        service.saveFlower(f, emptyFile, null);
        // Verify storeFile tetap dipanggil 1x (dari case 6), tidak bertambah
        verify(fileStorageService, times(1)).storeFile(any());
        
        // Case 8: Upload Image Null
        service.saveFlower(f, null, null);
        verify(fileStorageService, times(1)).storeFile(any());
    }
}