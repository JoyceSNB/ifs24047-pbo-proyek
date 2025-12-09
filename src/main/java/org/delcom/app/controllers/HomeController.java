package org.delcom.app.controllers;

import org.delcom.app.entities.User;
import org.delcom.app.services.FlowerService;
import org.delcom.app.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private FlowerService flowerService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }

        String email = auth.getName(); 
        User currentUser = userService.getUserByEmail(email);
        
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("auth", currentUser);
        model.addAttribute("listFlowers", flowerService.getFlowersByUser(currentUser.getId()));

        Map<String, Integer> salesMap = flowerService.getTopSellingFlowersByUser(currentUser.getId());
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(salesMap.entrySet());
        
        sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<String> salesNames = new ArrayList<>();
        List<Integer> salesValues = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : sortedList) {
            salesNames.add(entry.getKey());
            salesValues.add(entry.getValue());
        }

        model.addAttribute("salesNames", salesNames);
        model.addAttribute("salesValues", salesValues);

        return "pages/home";
    }
}