package org.delcom.app.controllers;

import org.delcom.app.entities.User;
import org.delcom.app.services.FlowerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private FlowerService flowerService;

    private boolean isAuthenticated(Authentication auth) {
        return auth != null &&
               auth.isAuthenticated() &&
               !(auth instanceof AnonymousAuthenticationToken) &&
               auth.getPrincipal() instanceof User;
    }

    @GetMapping("/")
    public String index(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 1. Cek Login. Jika belum, lempar ke halaman login
        if (!isAuthenticated(auth)) {
            return "redirect:/auth/login";
        }

        // 2. Ambil User untuk navbar
        User user = (User) auth.getPrincipal();
        model.addAttribute("auth", user);

        // 3. Ambil Data Bunga untuk tabel
        model.addAttribute("listFlowers", flowerService.getAllFlowers());

        // 4. Render file home.html
        return "pages/home";
    }
}