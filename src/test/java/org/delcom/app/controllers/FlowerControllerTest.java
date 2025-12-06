package org.delcom.app.controllers;

import org.delcom.app.dto.FlowerForm;
import org.delcom.app.entities.Flower;
import org.delcom.app.entities.User;
import org.delcom.app.services.FlowerService;
import org.delcom.app.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FlowerControllerTest {

    @Mock
    private FlowerService flowerService;

    @Mock
    private UserService userService;

    @InjectMocks
    private FlowerController flowerController;

    private MockMvc mockMvc;
    private User mockUser;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(flowerController)
                .setConversionService(new DefaultFormattingConversionService())
                .build();
        
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setName("Tester");
        mockUser.setEmail("tester@mail.com");
    }

    private void setAuthenticatedUser(User user) {
        if (user == null) {
            SecurityContextHolder.clearContext();
            return;
        }
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(user.getEmail());
        when(auth.getPrincipal()).thenReturn(user);
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
    }

    // --- Branch 1: Auth Null ---
    @Test
    void testSaveFlower_Auth_Null() throws Exception {
        SecurityContextHolder.clearContext(); // Pastikan kosong
        mockMvc.perform(multipart("/flowers/save").param("flowerName", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login")); 
    }

    // --- Branch 2: Auth Exists but Not Authenticated ---
    @Test
    void testSaveFlower_Auth_NotAuthenticated() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        mockMvc.perform(multipart("/flowers/save").param("flowerName", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login")); 
    }

    // --- Branch 3: User Not Found in DB ---
    @Test
    void testSaveFlower_UserNotFoundInDB() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("ghost@mail.com");
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        
        // User DB Null
        when(userService.getUserByEmail("ghost@mail.com")).thenReturn(null);

        mockMvc.perform(multipart("/flowers/save").param("flowerName", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
        
        verify(flowerService, never()).saveFlower(any(), any(), any());
    }

    // --- Happy Path: Create New Flower ---
    @Test
    void testSaveFlower_Success_CreateNew() throws Exception {
        setAuthenticatedUser(mockUser);
        MockMultipartFile image = new MockMultipartFile("image", "bunga.jpg", "image/jpeg", "data".getBytes());

        mockMvc.perform(multipart("/flowers/save")
                .file(image)
                .param("flowerName", "Tulip")
                .param("species", "Tulipa")
                .param("price", "50000")
                .param("stock", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(flowerService).saveFlower(any(Flower.class), eq(image), eq(null));
    }

    // --- Happy Path: Edit Existing Flower ---
    // (Branch Coverage: flower != null && flower.getUserId() != null)
    @Test
    void testSaveFlower_Success_EditExisting() throws Exception {
        setAuthenticatedUser(mockUser);
        UUID flowerId = UUID.randomUUID();
        
        Flower existingFlower = new Flower();
        existingFlower.setId(flowerId);
        existingFlower.setUserId(mockUser.getId()); // UserId sudah ada
        existingFlower.setStock(10); 
        
        when(flowerService.getFlowerById(flowerId)).thenReturn(existingFlower);

        mockMvc.perform(multipart("/flowers/save")
                .param("id", flowerId.toString())
                .param("flowerName", "Tulip Edit")
                .param("species", "Tulipa")
                .param("price", "60000")
                .param("stock", "20")) 
                .andExpect(status().is3xxRedirection());

        verify(flowerService).saveFlower(any(Flower.class), any(), eq(10));
    }

    // --- Branch Coverage: Edit Flow but Flower Not Found (ID exists in param, but DB returns null) ---
    // Ini menutup celah di blok if(form.getId() != null) -> else
    @Test
    void testSaveFlower_Edit_FlowerNotFound_CreatesNew() throws Exception {
        setAuthenticatedUser(mockUser);
        UUID fakeId = UUID.randomUUID();
        
        // Simulasi bunga tidak ditemukan meski ID dikirim
        when(flowerService.getFlowerById(fakeId)).thenReturn(null);

        mockMvc.perform(multipart("/flowers/save")
                .param("id", fakeId.toString()) // Kirim ID
                .param("flowerName", "Ghost Flower")
                .param("species", "Unknown")
                .param("price", "1000")
                .param("stock", "1"))
                .andExpect(status().is3xxRedirection());

        // Verifikasi bahwa oldStock null (karena dianggap baru)
        verify(flowerService).saveFlower(any(Flower.class), any(), eq(null));
    }

    // --- Branch Coverage: Edit Flow but UserId is Null ---
    // Ini menutup celah di if(flower.getUserId() == null) -> setUserId
    @Test
    void testSaveFlower_Edit_NullUserId_SetsCurrentUser() throws Exception {
        setAuthenticatedUser(mockUser);
        UUID flowerId = UUID.randomUUID();
        
        Flower existingFlower = new Flower();
        existingFlower.setId(flowerId);
        existingFlower.setStock(5);
        existingFlower.setUserId(null); // Case dimana UserId null

        when(flowerService.getFlowerById(flowerId)).thenReturn(existingFlower);

        mockMvc.perform(multipart("/flowers/save")
                .param("id", flowerId.toString())
                .param("flowerName", "Orchid")
                .param("species", "Vanda")
                .param("price", "75000")
                .param("stock", "5"))
                .andExpect(status().is3xxRedirection());

        // Verifikasi bahwa userId di-set ke current user
        verify(flowerService).saveFlower(argThat(f -> f.getUserId().equals(mockUser.getId())), any(), eq(5));
    }

    // --- Test Delete ---
    @Test
    void testDeleteFlower() throws Exception {
        setAuthenticatedUser(mockUser);
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/flowers/delete").param("id", id.toString()))
                .andExpect(status().is3xxRedirection());
        verify(flowerService).deleteFlower(id);
    }

    // --- Test History API ---
    @Test
    void testGetHistory() throws Exception {
        UUID id = UUID.randomUUID();
        when(flowerService.getHistoryByFlower(id)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/flowers/api/history/" + id))
                .andExpect(status().isOk());
    }
}