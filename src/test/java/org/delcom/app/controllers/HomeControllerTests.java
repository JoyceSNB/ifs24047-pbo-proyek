package org.delcom.app.controllers;

import org.delcom.app.entities.User;
import org.delcom.app.services.FlowerService;
import org.delcom.app.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HomeControllerTests {

    @Mock private FlowerService flowerService;
    @Mock private UserService userService;
    @Mock private Model model;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks private HomeController homeController;

    private User user;

    @BeforeEach
    void setup() {
        SecurityContextHolder.setContext(securityContext);
        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail("test@bunga.com");
    }

    // --- [UPDATE] Test Login Sukses + Sorting Chart ---
    @Test
    void testIndex_Success_WithSorting() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(authentication.getPrincipal()).thenReturn(user); 

        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);

        // Setup Data Dummy Chart
        Map<String, Integer> salesData = new HashMap<>();
        salesData.put("Mawar", 10);
        salesData.put("Melati", 50); 
        
        // PERBAIKAN: Gunakan method ByUser dan masukkan user.getId()
        when(flowerService.getTopSellingFlowersByUser(user.getId())).thenReturn(salesData);
        when(flowerService.getFlowersByUser(user.getId())).thenReturn(Collections.emptyList());

        String view = homeController.index(model);
        
        assertEquals("pages/home", view);
        
        // Verifikasi bahwa model diisi dengan data yang benar
        verify(model).addAttribute(eq("salesNames"), any());
        verify(model).addAttribute(eq("salesValues"), any());
        verify(model).addAttribute(eq("listFlowers"), any());
    }

    // --- Branch: Auth Null ---
    @Test
    void testIndex_AuthNull() {
        when(securityContext.getAuthentication()).thenReturn(null);
        String view = homeController.index(model);
        assertEquals("redirect:/auth/login", view);
    }

    // --- Branch: Auth Exists tapi isAuthenticated() False ---
    @Test
    void testIndex_NotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        String view = homeController.index(model);
        assertEquals("redirect:/auth/login", view);
    }

    // --- Branch: Principal "anonymousUser" ---
    @Test
    void testIndex_AnonymousUserString() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true); 
        when(authentication.getPrincipal()).thenReturn("anonymousUser"); 

        String view = homeController.index(model);
        assertEquals("redirect:/auth/login", view);
    }

    // --- Branch: User Login tapi Data di DB Null ---
    @Test
    void testIndex_UserNotFoundInDB() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user); 
        when(authentication.getName()).thenReturn("deleted@user.com");
        
        when(userService.getUserByEmail("deleted@user.com")).thenReturn(null);

        String view = homeController.index(model);
        assertEquals("redirect:/auth/login", view);
    }
}