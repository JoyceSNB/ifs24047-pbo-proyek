package org.delcom.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageServiceTests {

    private FileStorageService fileStorageService;
    private MultipartFile mockMultipartFile;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        fileStorageService = new FileStorageService();
        fileStorageService.uploadDir = tempDir.toString();
        mockMultipartFile = mock(MultipartFile.class);
    }

    // ==========================================
    // BAGIAN 1: Test Method Lama (Pakai UUID - Todo)
    // ==========================================

    @Test
    @DisplayName("Store file (UUID) berhasil menyimpan file dengan extension")
    void storeFile_uuid_berhasil() throws Exception {
        UUID todoId = UUID.randomUUID();
        String originalFilename = "image.jpg";
        String expectedFilename = "cover_" + todoId + ".jpg";
        byte[] fileContent = "content".getBytes();

        when(mockMultipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));

        String result = fileStorageService.storeFile(mockMultipartFile, todoId);

        assertEquals(expectedFilename, result);
        assertTrue(Files.exists(tempDir.resolve(expectedFilename)));
    }

    @Test
    @DisplayName("Store file (UUID) berhasil tanpa extension")
    void storeFile_uuid_tanpa_extension() throws Exception {
        UUID todoId = UUID.randomUUID();
        String expectedFilename = "cover_" + todoId.toString();
        
        when(mockMultipartFile.getOriginalFilename()).thenReturn("filetanpatitik");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        String result = fileStorageService.storeFile(mockMultipartFile, todoId);

        assertEquals(expectedFilename, result);
    }

    @Test
    @DisplayName("Store file (UUID) handle null filename")
    void storeFile_uuid_null_filename() throws Exception {
        UUID todoId = UUID.randomUUID();
        String expectedFilename = "cover_" + todoId.toString();
        
        when(mockMultipartFile.getOriginalFilename()).thenReturn(null);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        String result = fileStorageService.storeFile(mockMultipartFile, todoId);

        assertEquals(expectedFilename, result);
    }

    @Test
    @DisplayName("Store file (UUID) IOException")
    void storeFile_uuid_ioexception() throws Exception {
        UUID todoId = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn("test.txt");
        when(mockMultipartFile.getInputStream()).thenThrow(new IOException("Error"));

        assertThrows(IOException.class, () -> {
            fileStorageService.storeFile(mockMultipartFile, todoId);
        });
    }

    @Test
    @DisplayName("Store file (UUID) create dir")
    void storeFile_uuid_create_dir() throws Exception {
        UUID todoId = UUID.randomUUID();
        Path customDir = tempDir.resolve("subfolder");
        fileStorageService.uploadDir = customDir.toString();

        when(mockMultipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        fileStorageService.storeFile(mockMultipartFile, todoId);

        assertTrue(Files.isDirectory(customDir));
    }

    // ==========================================
    // BAGIAN 2: Test Method BARU (Generic - Bunga)
    // INI YANG SEBELUMNYA HILANG & BIKIN ERROR
    // ==========================================

    @Test
    @DisplayName("Store file (Generic) berhasil simpan dengan nama acak")
    void storeFile_generic_berhasil() throws Exception {
        String originalFilename = "bunga.png";
        byte[] content = "data bunga".getBytes();

        when(mockMultipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));

        // Panggil method baru (cuma 1 parameter)
        String result = fileStorageService.storeFile(mockMultipartFile);

        assertNotNull(result);
        assertTrue(result.startsWith("flower_")); // Sesuai format di Service
        assertTrue(result.endsWith(".png"));
        assertTrue(Files.exists(tempDir.resolve(result)));
    }

    @Test
    @DisplayName("Store file (Generic) handle null filename")
    void storeFile_generic_null_filename() throws Exception {
        when(mockMultipartFile.getOriginalFilename()).thenReturn(null);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeFile(mockMultipartFile);

        assertNotNull(result);
        assertTrue(result.startsWith("flower_"));
        // Tanpa extension
        assertFalse(result.contains(".")); 
    }

    @Test
    @DisplayName("Store file (Generic) handle IOException jadi RuntimeException")
    void storeFile_generic_exception() throws Exception {
        when(mockMultipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockMultipartFile.getInputStream()).thenThrow(new IOException("Disk full"));

        // Method baru melempar RuntimeException, bukan IOException biasa
        assertThrows(RuntimeException.class, () -> {
            fileStorageService.storeFile(mockMultipartFile);
        });
    }

    // ==========================================
    // BAGIAN 3: Utilities (Delete, Load, Check)
    // ==========================================

    @Test
    void deleteFile_berhasil() throws Exception {
        String filename = "hapus.txt";
        Files.write(tempDir.resolve(filename), "isi".getBytes());
        assertTrue(fileStorageService.deleteFile(filename));
        assertFalse(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    void deleteFile_gagal_karena_tidak_ada() {
        assertFalse(fileStorageService.deleteFile("ga_ada.txt"));
    }

    @Test
    void deleteFile_ioexception() {
        // Simulasi error permission saat delete
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.deleteIfExists(any())).thenThrow(new IOException("Access Denied"));
            // Kita perlu path yg valid spy masuk ke try block
            assertFalse(fileStorageService.deleteFile("test.txt"));
        }
    }

    @Test
    void loadFile_benar() {
        Path result = fileStorageService.loadFile("test.txt");
        assertEquals(tempDir.resolve("test.txt"), result);
    }

    @Test
    void fileExists_cek() throws Exception {
        String filename = "ada.txt";
        Files.write(tempDir.resolve(filename), "y".getBytes());
        assertTrue(fileStorageService.fileExists(filename));
        assertFalse(fileStorageService.fileExists("tada.txt"));
    }
}