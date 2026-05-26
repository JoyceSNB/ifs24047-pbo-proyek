package org.delcom.app;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private Application application;

    @Test
    void testInit_WhenAdminNotExists() throws Exception {
        when(userRepository.findFirstByEmail("admin@bunga.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPwd");

        CommandLineRunner runner = application.init(userRepository, passwordEncoder);
        if (runner != null) {
            runner.run();
        }

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testInit_WhenAdminAlreadyExists() throws Exception {
        User existingAdmin = new User();
        existingAdmin.setEmail("admin@bunga.com");
        when(userRepository.findFirstByEmail("admin@bunga.com")).thenReturn(Optional.of(existingAdmin));

        CommandLineRunner runner = application.init(userRepository, passwordEncoder);
        if (runner != null) {
            runner.run();
        }

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testMain() {
        try (var mockedStatic = org.mockito.Mockito.mockStatic(org.springframework.boot.SpringApplication.class)) {
            mockedStatic.when(() -> org.springframework.boot.SpringApplication.run(Application.class, new String[]{}))
                    .thenReturn(null);
            Application.main(new String[]{});
            mockedStatic.verify(() -> org.springframework.boot.SpringApplication.run(Application.class, new String[]{}));
        }
    }
}