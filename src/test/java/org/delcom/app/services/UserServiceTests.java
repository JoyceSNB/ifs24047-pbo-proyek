package org.delcom.app.services;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // --- 1. LoadUserByUsername ---
    
    @Test
    void loadUserByUsername_Success_WithMixedCase() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setPassword("encoded");
        
        when(userRepository.findFirstByEmail("test@mail.com")).thenReturn(Optional.of(user));
        
        UserDetails result = userService.loadUserByUsername(" Test@Mail.com "); // Input kotor
        assertNotNull(result);
        assertEquals("test@mail.com", result.getUsername());
    }

    @Test
    void loadUserByUsername_NullInput() {
        when(userRepository.findFirstByEmail("")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(null));
    }

    @Test
    void loadUserByUsername_NotFound() {
        when(userRepository.findFirstByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("404@mail.com"));
    }

    // --- 2. CreateUser ---
    
    @Test
    void createUser_Success_TrimsInputs() {
        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
        
        userService.createUser(" Nama ", " Email@Mail.com ", " rawPass ");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertEquals("Nama", savedUser.getName()); // Spasi hilang
        assertEquals("email@mail.com", savedUser.getEmail()); // Lowercase & spasi hilang
        assertEquals("encodedPass", savedUser.getPassword());
    }

    // --- 3. GetUserByEmail ---
    
    @Test
    void getUserByEmail_Found() {
        User user = new User();
        when(userRepository.findFirstByEmail("exist@mail.com")).thenReturn(Optional.of(user));
        // Input kotor, harus tetap ketemu
        assertNotNull(userService.getUserByEmail(" Exist@Mail.com "));
    }

    @Test
    void getUserByEmail_NullInput() {
        assertNull(userService.getUserByEmail(null));
    }

    @Test
    void getUserByEmail_NotFound() {
        when(userRepository.findFirstByEmail("none@mail.com")).thenReturn(Optional.empty());
        assertNull(userService.getUserByEmail("none@mail.com"));
    }

    // --- 4. GetUserById (Standar) ---
    @Test
    void getUserById_Found() {
        User user = new User();
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        assertNotNull(userService.getUserById(id));
    }

    @Test
    void getUserById_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertNull(userService.getUserById(id));
    }

    // --- 5. Authenticate (Penting untuk Null Check) ---
    
    @Test
    void authenticate_Success() {
        User user = new User();
        user.setPassword("encoded");
        when(userRepository.findFirstByEmail("valid@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);
        assertTrue(userService.authenticate(" Valid@Mail.com ", " raw "));
    }

    @Test
    void authenticate_NullInputs() {
        assertFalse(userService.authenticate(null, "pass"));
        assertFalse(userService.authenticate("mail", null));
        assertFalse(userService.authenticate(null, null));
    }

    @Test
    void authenticate_UserNotFound() {
        when(userRepository.findFirstByEmail(anyString())).thenReturn(Optional.empty());
        assertFalse(userService.authenticate("invalid", "pass"));
    }

    @Test
    void authenticate_WrongPassword() {
        User user = new User();
        user.setPassword("encoded");
        when(userRepository.findFirstByEmail("valid@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertFalse(userService.authenticate("valid@mail.com", "wrong"));
    }

    // --- 6. UpdateUser ---
    
    @Test
    void updateUser_Success() {
        UUID id = UUID.randomUUID();
        User existing = new User();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.updateUser(id, " New Name ", " New@Email.com ");
        
        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("new@email.com", result.getEmail());
    }

    @Test
    void updateUser_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertNull(userService.updateUser(id, "Name", "Email"));
    }

    // --- 7. UpdatePassword ---
    
    @Test
    void updatePassword_Success() {
        UUID id = UUID.randomUUID();
        User existing = new User();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.updatePassword(id, " newPass ");
        
        assertNotNull(result);
        assertEquals("encodedNew", result.getPassword());
    }

    @Test
    void updatePassword_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertNull(userService.updatePassword(id, "pass"));
    }
}