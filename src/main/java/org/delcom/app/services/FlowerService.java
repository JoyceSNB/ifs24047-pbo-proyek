package org.delcom.app.services;

import org.delcom.app.entities.Flower;
import org.delcom.app.entities.StockHistory;
import org.delcom.app.repositories.FlowerRepository;
import org.delcom.app.repositories.StockHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FlowerService {

    @Autowired
    private FlowerRepository repository;

    @Autowired
    private StockHistoryRepository historyRepository;

    @Autowired
    private FileStorageService fileStorageService; 

    public List<Flower> getAllFlowers() {
        return repository.findAll();
    }

    public List<Flower> getFlowersByUser(UUID userId) {
        return repository.findAllByUserId(userId);
    }
    
    public Flower getFlowerById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public List<StockHistory> getHistoryByFlower(UUID flowerId) {
        return historyRepository.findByFlowerIdOrderByRecordedAtDesc(flowerId);
    }

    public Map<String, Integer> getTopSellingFlowersByUser(UUID userId) {
        List<Flower> flowers = getFlowersByUser(userId); 
        Map<String, Integer> salesData = new HashMap<>();

        for (Flower flower : flowers) {
            List<StockHistory> histories = historyRepository.findByFlowerIdOrderByRecordedAtDesc(flower.getId());
            
            int totalSold = 0;
            if (histories != null) {
                for (StockHistory h : histories) {
                    if ("PENJUALAN".equalsIgnoreCase(h.getType())) {
                        totalSold += h.getQuantity();
                    }
                }
            }
            salesData.put(flower.getFlowerName(), totalSold);
        }
        return salesData;
    }

    public void saveFlower(Flower flower, MultipartFile imageFile, Integer oldStock) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(imageFile);
            flower.setImagePath(fileName);
        }
        repository.save(flower);

        if (oldStock != null) {
            if (!oldStock.equals(flower.getStock())) {
                int diff = flower.getStock() - oldStock;
                String type = (diff > 0) ? "RESTOCK" : "PENJUALAN";
                StockHistory history = new StockHistory(flower.getId(), type, Math.abs(diff), flower.getStock());
                historyRepository.save(history);
            }
        } else {
            if (flower.getStock() != null) {
                StockHistory history = new StockHistory(flower.getId(), "BARANG MASUK", flower.getStock(), flower.getStock());
                historyRepository.save(history);
            }
        }
    }

    public void deleteFlower(UUID id) {
        repository.deleteById(id);
    }
}