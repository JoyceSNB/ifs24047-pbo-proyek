package org.delcom.app.controllers;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
    @Mock private AuthTokenService authTokenService;
    @Mock private AuthContext authContext;

    @InjectMocks private UserController userController;

    @BeforeEach
    void setUp() {
        // Inject Manual untuk menghindari NullPointerException
        userController.authContext = this.authContext;
    }

    // ================= REGISTER TESTS =================

    @Test
    void registerUser_Success() {
        User req = new User("Test Name", "test@mail.com", "password123");
        when(userService.getUserByEmail(req.getEmail())).thenReturn(null);
        
        User createdUser = new User("Test Name", "test@mail.com", "password123");
        createdUser.setId(UUID.randomUUID());
        when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(createdUser);

        ResponseEntity<ApiResponse<Map<String, UUID>>> response = userController.registerUser(req);
        
        // Gunakan .getStatusCode() untuk membandingkan dengan Enum HttpStatus
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getStatus());
    }

    @Test
    void registerUser_InvalidInput() {
        // Gunakan .getStatusCode().value() untuk membandingkan dengan int (400)
        
        // 1. Name Invalid
        assertEquals(400, userController.registerUser(new User(null, "email@mail.com", "pass")).getStatusCode().value());
        assertEquals(400, userController.registerUser(new User("", "email@mail.com", "pass")).getStatusCode().value());
        
        // 2. Email Invalid
        assertEquals(400, userController.registerUser(new User("name", null, "pass")).getStatusCode().value());
        assertEquals(400, userController.registerUser(new User("name", "", "pass")).getStatusCode().value());
        
        // 3. Password Invalid
        assertEquals(400, userController.registerUser(new User("name", "email@mail.com", null)).getStatusCode().value());
        assertEquals(400, userController.registerUser(new User("name", "email@mail.com", "")).getStatusCode().value());
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        User req = new User("Name", "exist@mail.com", "pass");
        when(userService.getUserByEmail(req.getEmail())).thenReturn(new User());
        
        // Bandingkan Enum dengan Enum
        assertEquals(HttpStatus.BAD_REQUEST, userController.registerUser(req).getStatusCode());
    }

    // ================= LOGIN TESTS =================

    @Test
    void loginUser_Success() {
        User req = new User(null, "test@mail.com", "password123");
        User dbUser = new User("Name", "test@mail.com", "ignore");
        dbUser.setId(UUID.randomUUID());
        dbUser.setPassword(new BCryptPasswordEncoder().encode("password123"));

        when(userService.getUserByEmail(req.getEmail())).thenReturn(dbUser);
        
        AuthToken newToken = new AuthToken(dbUser.getId(), "token-abc");
        when(authTokenService.createAuthToken(any(AuthToken.class))).thenReturn(newToken);

        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.loginUser(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData().get("authToken"));
    }

    @Test
    void loginUser_WithExistingToken() {
        User req = new User(null, "test@mail.com", "password123");
        User dbUser = new User("Name", "test@mail.com", "ignore");
        dbUser.setId(UUID.randomUUID());
        dbUser.setPassword(new BCryptPasswordEncoder().encode("password123"));

        when(userService.getUserByEmail(req.getEmail())).thenReturn(dbUser);
        
        // Simulasi token lama ada
        when(authTokenService.findUserToken(eq(dbUser.getId()), anyString())).thenReturn(new AuthToken());
        
        AuthToken newToken = new AuthToken(dbUser.getId(), "token-abc");
        when(authTokenService.createAuthToken(any(AuthToken.class))).thenReturn(newToken);

        userController.loginUser(req);
        
        verify(authTokenService).deleteAuthToken(dbUser.getId());
    }

    @Test
    void loginUser_InvalidInput() {
        assertEquals(400, userController.loginUser(new User(null, null, "pass")).getStatusCode().value());
        assertEquals(400, userController.loginUser(new User(null, "email", null)).getStatusCode().value());
        assertEquals(400, userController.loginUser(new User(null, "", "pass")).getStatusCode().value());
        assertEquals(400, userController.loginUser(new User(null, "email", "")).getStatusCode().value());
    }

    @Test
    void loginUser_UserNotFound() {
        User req = new User(null, "ghost@mail.com", "pass");
        when(userService.getUserByEmail(req.getEmail())).thenReturn(null);
        assertEquals(400, userController.loginUser(req).getStatusCode().value());
    }

    @Test
    void loginUser_WrongPassword() {
        User req = new User(null, "test@mail.com", "wrongpass");
        User dbUser = new User("Name", "test@mail.com", "ignore");
        dbUser.setPassword(new BCryptPasswordEncoder().encode("realpass")); 

        when(userService.getUserByEmail(req.getEmail())).thenReturn(dbUser);
        assertEquals(400, userController.loginUser(req).getStatusCode().value());
    }
    
    @Test
    void loginUser_TokenCreationFail() {
        User req = new User(null, "test@mail.com", "pass");
        User dbUser = new User("Name", "test@mail.com", "ignore");
        dbUser.setId(UUID.randomUUID());
        dbUser.setPassword(new BCryptPasswordEncoder().encode("pass"));

        when(userService.getUserByEmail(req.getEmail())).thenReturn(dbUser);
        when(authTokenService.createAuthToken(any())).thenReturn(null);

        assertEquals(500, userController.loginUser(req).getStatusCode().value());
    }

    // ================= USER INFO TESTS =================

    @Test
    void getUserInfo_Success() {
        lenient().when(authContext.isAuthenticated()).thenReturn(true);
        User authUser = new User("Auth Name", "auth@mail.com", "pass");
        lenient().when(authContext.getAuthUser()).thenReturn(authUser);

        ResponseEntity<ApiResponse<Map<String, User>>> response = userController.getUserInfo();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("auth@mail.com", response.getBody().getData().get("user").getEmail());
    }

    @Test
    void getUserInfo_Unauthenticated() {
        lenient().when(authContext.isAuthenticated()).thenReturn(false);
        assertEquals(401, userController.getUserInfo().getStatusCode().value());
    }

    // ================= UPDATE USER TESTS =================

    @Test
    void updateUser_Success() {
        lenient().when(authContext.isAuthenticated()).thenReturn(true);
        User authUser = new User(); authUser.setId(UUID.randomUUID());
        lenient().when(authContext.getAuthUser()).thenReturn(authUser);

        User req = new User("New Name", "new@mail.com", "pass");
        when(userService.updateUser(eq(authUser.getId()), eq(req.getName()), eq(req.getEmail()))).thenReturn(req);

        // FIX: getStatusCode()
        assertEquals(HttpStatus.OK, userController.updateUser(req).getStatusCode());
    }

    @Test
    void updateUser_Failures() {
        lenient().when(authContext.isAuthenticated()).thenReturn(false);
        assertEquals(401, userController.updateUser(new User()).getStatusCode().value());

        lenient().when(authContext.isAuthenticated()).thenReturn(true);
        lenient().when(authContext.getAuthUser()).thenReturn(new User());
        
        // Invalid inputs
        assertEquals(400, userController.updateUser(new User(null, "mail", "p")).getStatusCode().value());
        assertEquals(400, userController.updateUser(new User("", "mail", "p")).getStatusCode().value());
        assertEquals(400, userController.updateUser(new User("name", null, "p")).getStatusCode().value());
        assertEquals(400, userController.updateUser(new User("name", "", "p")).getStatusCode().value());

        // Not Found
        User req = new User("Valid", "valid@mail.com", "pass");
        User authUser = new User(); authUser.setId(UUID.randomUUID());
        when(authContext.getAuthUser()).thenReturn(authUser);
        when(userService.updateUser(any(), any(), any())).thenReturn(null);
        assertEquals(404, userController.updateUser(req).getStatusCode().value());
    }

    // ================= UPDATE PASSWORD TESTS =================

    @Test
    void updateUserPassword_Success() {
        lenient().when(authContext.isAuthenticated()).thenReturn(true);
        User authUser = new User(); authUser.setId(UUID.randomUUID());
        authUser.setPassword(new BCryptPasswordEncoder().encode("oldPass")); 
        lenient().when(authContext.getAuthUser()).thenReturn(authUser);

        Map<String, String> payload = new HashMap<>();
        payload.put("password", "oldPass");
        payload.put("newPassword", "newPass");

        when(userService.updatePassword(any(), anyString())).thenReturn(authUser);
        
        // FIX: getStatusCode()
        assertEquals(HttpStatus.OK, userController.updateUserPassword(payload).getStatusCode());
    }

    @Test
    void updateUserPassword_Failures() {
        // 1. Unauth
        lenient().when(authContext.isAuthenticated()).thenReturn(false);
        assertEquals(401, userController.updateUserPassword(new HashMap<>()).getStatusCode().value());

        // 2. Invalid Payload
        lenient().when(authContext.isAuthenticated()).thenReturn(true);
        lenient().when(authContext.getAuthUser()).thenReturn(new User());
        
        Map<String, String> payload = new HashMap<>();
        assertEquals(400, userController.updateUserPassword(payload).getStatusCode().value()); 
        
        payload.put("password", "");
        payload.put("newPassword", "new");
        assertEquals(400, userController.updateUserPassword(payload).getStatusCode().value());

        payload.clear();
        payload.put("password", "valid");
        payload.put("newPassword", null); // Missing new pass
        assertEquals(400, userController.updateUserPassword(payload).getStatusCode().value());

        payload.put("newPassword", ""); // Empty new pass
        assertEquals(400, userController.updateUserPassword(payload).getStatusCode().value());

        // 3. Wrong Old Password
        User authUser = new User();
        authUser.setPassword(new BCryptPasswordEncoder().encode("correct"));
        when(authContext.getAuthUser()).thenReturn(authUser);
        
        payload.put("password", "wrong");
        payload.put("newPassword", "new");
        assertEquals(400, userController.updateUserPassword(payload).getStatusCode().value());
        
        // 4. User Not Found
        authUser.setPassword(new BCryptPasswordEncoder().encode("correct"));
        payload.put("password", "correct");
        when(userService.updatePassword(any(), any())).thenReturn(null);
        assertEquals(404, userController.updateUserPassword(payload).getStatusCode().value());
    }
}

