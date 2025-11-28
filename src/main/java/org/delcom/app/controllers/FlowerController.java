package org.delcom.app.controllers;

import org.delcom.app.dto.FlowerForm;
import org.delcom.app.entities.Flower;
import org.delcom.app.services.FlowerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/flowers")
public class FlowerController {

    @Autowired
    private FlowerService service;

    @PostMapping("/save")
    public String saveFlower(@ModelAttribute FlowerForm form) {
        Flower flower = new Flower();
        
        // Cek apakah ini edit data lama atau buat baru
        if (form.getId() != null) {
            flower = service.getFlowerById(form.getId());
            // Jika ID tidak ditemukan (security check sederhana), buat baru
            if (flower == null) flower = new Flower(); 
        }

        // Update data objek
        flower.setName(form.getName());
        flower.setVariety(form.getVariety());
        flower.setStock(form.getStock());
        flower.setPrice(form.getPrice());

        // Kirim ke service untuk disimpan (termasuk handle gambarnya)
        service.saveFlower(flower, form.getImage());
        
        return "redirect:/"; // Kembali ke halaman utama
    }

    @PostMapping("/delete")
    public String deleteFlower(@ModelAttribute FlowerForm form) {
        service.deleteFlower(form.getId());
        return "redirect:/";
    }
}