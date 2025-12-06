package org.delcom.app.controllers;

import org.delcom.app.dto.FlowerForm;
import org.delcom.app.entities.Flower;
import org.delcom.app.entities.StockHistory;
import org.delcom.app.entities.User;
import org.delcom.app.services.FlowerService;
import org.delcom.app.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/flowers")
public class FlowerController {

    @Autowired
    private FlowerService service;
    
    @Autowired
    private UserService userService;

    @PostMapping("/save")
    public String saveFlower(@ModelAttribute FlowerForm form) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // SAFETY CHECK: Agar Test tidak crash saat auth null
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        
        User currentUser = userService.getUserByEmail(auth.getName());
        // Jika user tidak ditemukan di DB (tapi login), lempar ke login
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        Flower flower = new Flower();
        Integer oldStock = null; 
        
        if (form.getId() != null) {
            flower = service.getFlowerById(form.getId());
            if (flower != null) {
                oldStock = flower.getStock();
            } else {
                flower = new Flower();
            }
        } else {
            flower.setUserId(currentUser.getId());
        }

        flower.setFlowerName(form.getFlowerName());
        flower.setSpecies(form.getSpecies());
        flower.setPrice(form.getPrice());
        flower.setStock(form.getStock());
        flower.setDescription(form.getDescription());

        if (flower.getUserId() == null) {
            flower.setUserId(currentUser.getId());
        }

        service.saveFlower(flower, form.getImage(), oldStock);
        
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String deleteFlower(@RequestParam("id") UUID id) {
        service.deleteFlower(id);
        return "redirect:/";
    }

    @GetMapping("/api/history/{id}")
    @ResponseBody
    public ResponseEntity<List<StockHistory>> getHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getHistoryByFlower(id));
    }
}