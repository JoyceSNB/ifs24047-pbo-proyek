package org.delcom.app.views;

import org.delcom.app.entities.User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeView {

    // HAPUS constructor TodoService karena kita tidak butuh data Todo lagi di sini

    @GetMapping("/")
    public String home(Model model) {
        // 1. Cek apakah user sudah login
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Jika belum login (Anonymous), lempar ke halaman logout/login
        if (authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/auth/login";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/login";
        }

        // 2. Jika sudah login, JANGAN tampilkan halaman Todo.
        // Langsung alihkan (redirect) ke Controller Katalog Bunga kita.
        return "redirect:/catalog"; 
    }
}