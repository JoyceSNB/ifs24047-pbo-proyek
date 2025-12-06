package org.delcom.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageServiceTests {

    private FileStorageService service;
    private MultipartFile mockFile;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        service = new FileStorageService();
        service.uploadDir = tempDir.toString(); // Arahkan upload ke temp folder
        mockFile = mock(MultipartFile.class);
    }

    // --- TEST 1: Normal Upload ---
    @Test
    void testStoreFile_Success() throws Exception {
        when(mockFile.getOriginalFilename()).thenReturn("bunga.jpg");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        
        String result = service.storeFile(mockFile);
        
        assertNotNull(result);
        assertTrue(result.startsWith("flower_"));
        assertTrue(result.endsWith(".jpg"));
        assertTrue(Files.exists(tempDir.resolve(result)));
    }

    // --- TEST 2: File Tanpa Ekstensi (Menutup Branch Coverage) ---
    @Test
    @DisplayName("Store file tanpa ekstensi (README)")
    void testStoreFile_NoExtension() throws Exception {
        // Ini mengetes kondisi: lastIndexOf(".") <= 0
        when(mockFile.getOriginalFilename()).thenReturn("README");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        
        String result = service.storeFile(mockFile);
        
        assertNotNull(result);
        assertFalse(result.contains(".")); // Pastikan tidak ada titik
    }

    // --- TEST 3: Filename Null (Menutup Branch Coverage) ---
    @Test
    void testStoreFile_NullName() throws Exception {
        when(mockFile.getOriginalFilename()).thenReturn(null);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        
        String result = service.storeFile(mockFile);
        
        assertNotNull(result);
        assertFalse(result.contains("."));
    }

    // --- TEST 4: Simulasi Error IO saat Save (Cover Catch Block) ---
    @Test
    void testStoreFile_Exception() throws Exception {
        when(mockFile.getOriginalFilename()).thenReturn("error.png");
        when(mockFile.getInputStream()).thenThrow(new IOException("Disk Full"));
        
        assertThrows(RuntimeException.class, () -> service.storeFile(mockFile));
    }

    // --- TEST 5: Create Directory (Cover IF exists) ---
    @Test
    void testStoreFile_CreateDir() throws Exception {
        Path subDir = tempDir.resolve("subfolder");
        service.uploadDir = subDir.toString();

        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        service.storeFile(mockFile);
        
        assertTrue(Files.isDirectory(subDir));
    }

    // --- TEST 6: Delete Success ---
    @Test
    void testDeleteFile_Success() throws Exception {
        String filename = "delete_me.txt";
        Files.createFile(tempDir.resolve(filename));
        
        assertTrue(service.deleteFile(filename));
        assertFalse(Files.exists(tempDir.resolve(filename)));
    }

    // --- TEST 7: Delete Not Found ---
    @Test
    void testDeleteFile_NotFound() {
        assertFalse(service.deleteFile("ghost.txt"));
    }

    // --- TEST 8: Delete Exception (THE TRICK!) ---
    @Test
    void testDeleteFile_Exception() throws Exception {
        String folderName = "folder_bukan_file";
        Path folderPath = tempDir.resolve(folderName);
        Files.createDirectory(folderPath);
        Files.createFile(folderPath.resolve("isi.txt"));
        boolean result = service.deleteFile(folderName);
        
        assertFalse(result);
    }
}

