package org.delcom.app.services;

import org.delcom.app.entities.Flower;
import org.delcom.app.repositories.FlowerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

@Service
public class FlowerService {

    @Autowired
    private FlowerRepository repository;

    @Autowired
    private FileStorageService fileStorageService;

    public List<Flower> getAllFlowers() {
        return repository.findAll();
    }

    public Flower getFlowerById(Long id) {
        Optional<Flower> flower = repository.findById(id);
        return flower.orElse(null);
    }

    public void saveFlower(Flower flower, MultipartFile imageFile) {
        // Cek jika ada file gambar yang diupload
        if (imageFile != null && !imageFile.isEmpty()) {
            // Panggil method storeFile yang baru (hanya 1 parameter)
            String fileName = fileStorageService.storeFile(imageFile);
            flower.setImagePath(fileName);
        }
        
        repository.save(flower);
    }

    public void deleteFlower(Long id) {
        // (Opsional) Hapus file fisik jika perlu, tapi hapus data di DB saja sudah cukup untuk tugas
        repository.deleteById(id);
    }
}