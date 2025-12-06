package org.delcom.app.interceptors;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jsonwebtoken.Jwts; 
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTests {

    @Mock private AuthContext authContext;
    @Mock private AuthTokenService authTokenService;
    @Mock private UserService userService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @InjectMocks private AuthInterceptor interceptor;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    @Test
    void preHandle_PublicEndpoint_Auth() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        assertTrue(interceptor.preHandle(request, response, null));
    }

    @Test
    void preHandle_PublicEndpoint_Error() throws Exception {
        when(request.getRequestURI()).thenReturn("/error");
        assertTrue(interceptor.preHandle(request, response, null));
    }

    @Test
    void preHandle_NoHeader() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn(null);
        assertFalse(interceptor.preHandle(request, response, null));
    }

    @Test
    void preHandle_BadHeaderFormat() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Basic 123456"); 
        assertFalse(interceptor.preHandle(request, response, null));
    }

    @Test
    void preHandle_EmptyToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer "); 
        assertFalse(interceptor.preHandle(request, response, null));
    }
    
    @Test
    void preHandle_InvalidJwtSignature() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token.here");
        assertFalse(interceptor.preHandle(request, response, null));
    }

    @Test
    void preHandle_NonUUIDTokenSubject() throws Exception {
        String token = Jwts.builder()
                .subject("bukan-uuid-valid") 
                .signWith(JwtUtil.getKey())
                .compact();
        
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        assertFalse(interceptor.preHandle(request, response, null));
    }

    @Test
    void preHandle_TokenNotFoundInDB() throws Exception {
        UUID uid = UUID.randomUUID();
        String token = JwtUtil.generateToken(uid);
        
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        when(authTokenService.findUserToken(any(), anyString())).thenReturn(null);

        assertFalse(interceptor.preHandle(request, response, null));
    }

    @Test
    void preHandle_UserNotFound() throws Exception {
        UUID uid = UUID.randomUUID();
        String token = JwtUtil.generateToken(uid);
        
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        AuthToken authToken = new AuthToken();
        authToken.setUserId(uid);
        when(authTokenService.findUserToken(any(), anyString())).thenReturn(authToken);
        
        when(userService.getUserById(uid)).thenReturn(null);

        assertFalse(interceptor.preHandle(request, response, null));
    }

    @Test
    void preHandle_Success() throws Exception {
        UUID uid = UUID.randomUUID();
        String token = JwtUtil.generateToken(uid);
        
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        AuthToken authToken = new AuthToken();
        authToken.setUserId(uid);
        when(authTokenService.findUserToken(any(), anyString())).thenReturn(authToken);
        when(userService.getUserById(uid)).thenReturn(new User());

        assertTrue(interceptor.preHandle(request, response, null));
        verify(authContext).setAuthUser(any(User.class));
    }
}

