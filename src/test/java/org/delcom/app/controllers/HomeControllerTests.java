package org.delcom.app.controllers;

import org.delcom.app.entities.Flower;
import org.delcom.app.entities.User;
import org.delcom.app.services.FlowerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HomeControllerTests {

    @Mock private FlowerService flowerService;
    @Mock private Model model;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks private HomeController homeController;

    private User user;

    @BeforeEach
    void setup() {
        SecurityContextHolder.setContext(securityContext);
        user = new User();
        user.setId(UUID.randomUUID()); // Pakai UUID sesuai Entity User
        user.setName("Test User");
    }

    @Test
    void testIndex_Authenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(flowerService.getAllFlowers()).thenReturn(Collections.emptyList());

        String view = homeController.index(model);

        assertEquals("pages/home", view);
        verify(model).addAttribute(eq("listFlowers"), any());
    }

    @Test
    void testIndex_NotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);
        String view = homeController.index(model);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void testIndex_AnonymousUser() {
        // Simulasi user belum login (Anonymous)
        AnonymousAuthenticationToken anon = new AnonymousAuthenticationToken(
            "key", "anon", AuthorityUtils.createAuthorityList("ROLE_ANON")
        );
        when(securityContext.getAuthentication()).thenReturn(anon);
        
        String view = homeController.index(model);
        assertEquals("redirect:/auth/login", view);
    }
}