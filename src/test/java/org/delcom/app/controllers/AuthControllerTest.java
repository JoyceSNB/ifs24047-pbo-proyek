package org.delcom.app.controllers;

import org.delcom.app.entities.User;
import org.delcom.app.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setViewResolvers(viewResolver)
                .build();
    }

    // --- 1. Test Halaman Login (GET) ---
    @Test
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/login"))
                .andExpect(model().attributeExists("loginForm"));
    }

    // --- 2. Test Halaman Register (GET) ---
    @Test
    void testRegisterPage() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/register"))
                .andExpect(model().attributeExists("registerForm"));
    }

    // --- 3. Test Register Sukses (POST) ---
    @Test
    void testRegisterProcess_Success() throws Exception {
        // Email belum terdaftar (return null)
        when(userService.getUserByEmail("new@mail.com")).thenReturn(null);

        mockMvc.perform(post("/auth/register/post")
                .param("name", "New User")
                .param("email", "new@mail.com")
                .param("password", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login")) 
                .andExpect(flash().attributeExists("success"));
        verify(userService).createUser("New User", "new@mail.com", "123");
    }

    // --- 4. Test Register Gagal - Email Duplikat (POST) ---
    @Test
    void testRegisterProcess_EmailExists() throws Exception {
        when(userService.getUserByEmail("exist@mail.com")).thenReturn(new User());

        mockMvc.perform(post("/auth/register/post")
                .param("name", "User")
                .param("email", "exist@mail.com")
                .param("password", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/register"))
                .andExpect(flash().attributeExists("error"));
        verify(userService, never()).createUser(anyString(), anyString(), anyString());
    }
}