package org.delcom.app.services;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String cleanEmail = (email != null) ? email.trim().toLowerCase() : "";

        User user = userRepository.findFirstByEmail(cleanEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + cleanEmail));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList()
        );
    }

    @Transactional
    public User createUser(String name, String email, String password) {
        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(password.trim()));
        
        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        if (email == null) return null;
        return userRepository.findFirstByEmail(email.trim().toLowerCase()).orElse(null);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public boolean authenticate(String email, String rawPassword) {
        if (email == null || rawPassword == null) return false;
        return userRepository.findFirstByEmail(email.trim().toLowerCase())
                .map(user -> passwordEncoder.matches(rawPassword.trim(), user.getPassword()))
                .orElse(false);
    }

    @Transactional
    public User updateUser(UUID id, String name, String email) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setName(name.trim());
            user.setEmail(email.trim().toLowerCase());
            return userRepository.save(user);
        }
        return null;
    }

    @Transactional
    public User updatePassword(UUID id, String newPassword) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword.trim()));
            return userRepository.save(user);
        }
        return null;
    }
}