package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UserTests {

    @Test
    void testUserEntity() {
        // Test Constructor & Setter
        User user = new User("Budi", "budi@example.com", "pass123");
        user.setId(UUID.randomUUID());
        
        // Test Getter
        assertNotNull(user.getId());
        assertEquals("Budi", user.getName());
        assertEquals("budi@example.com", user.getEmail());
        assertEquals("pass123", user.getPassword());
        
        // Test Constructor Kosong
        User emptyUser = new User();
        assertNull(emptyUser.getName());

        // 4Test Constructor Partial
        User partialUser = new User("email@test.com", "password");
        assertEquals("email@test.com", partialUser.getEmail());
    }

    @Test
    void testLifecycleMethods() {
        User user = new User();
    
        user.onCreate(); 
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());

        user.onUpdate(); 
        assertNotNull(user.getUpdatedAt());
    }
}