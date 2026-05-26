package org.delcom.app;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findFirstByEmail("admin@bunga.com").isEmpty()) {
                User admin = new User();
                admin.setName("Admin Toko");
                admin.setEmail("admin@bunga.com");
                admin.setPassword(passwordEncoder.encode("123456")); 
                
                userRepository.save(admin);
                System.out.println("✅ User Admin Berhasil Dibuat: admin@bunga.com / 123456");
            }
        };
    }
}