package org.delcom.app.controllers;

import org.delcom.app.dto.LoginForm;
import org.delcom.app.dto.RegisterForm;
import org.delcom.app.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // Halaman Login (GET)
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "pages/auth/login";
    }

    // Halaman Register (GET)
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "pages/auth/register";
    }

    // Proses Register (POST)
    @PostMapping("/register/post")
    public String registerProcess(@ModelAttribute RegisterForm form, RedirectAttributes redirectAttributes) {
        String cleanEmail = form.getEmail().trim().toLowerCase();

        if (userService.getUserByEmail(cleanEmail) != null) {
            redirectAttributes.addFlashAttribute("error", "Pengguna dengan email ini sudah terdaftar");
            return "redirect:/auth/register"; 
        }
        
        // Simpan User
        userService.createUser(form.getName(), form.getEmail(), form.getPassword());
        redirectAttributes.addFlashAttribute("success", "Akun berhasil dibuat! Silakan login.");
        return "redirect:/auth/login";
    }
}