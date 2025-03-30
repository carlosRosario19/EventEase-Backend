package com.centennial.eventease_backend.services;


import com.centennial.eventease_backend.exceptions.StorageException;
import com.centennial.eventease_backend.exceptions.StorageFileNotFoundException;
import com.centennial.eventease_backend.security.StorageProperties;
import com.centennial.eventease_backend.services.implementations.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImageStorageServiceTest {


    @TempDir
    Path tempDir;

    private ImageStorageService storageService;
    private StorageProperties properties;

    @BeforeEach
    void setUp() {
        properties = new StorageProperties();
        properties.setLocation(tempDir.toString());
        storageService = new ImageStorageService(properties);
        storageService.init();
    }

    @Test
    void init_ShouldCreateStorageDirectory() {
        assertTrue(Files.exists(tempDir));
    }

    @Test
    void store_WithValidFile_ShouldStoreFile() throws IOException {
        // Arrange
        String content = "test content";
        String fileName = "test.txt";
        MultipartFile file = new MockMultipartFile(fileName, fileName, "text/plain", content.getBytes());

        // Act
        storageService.store(file);

        // Assert
        Path storedFile = tempDir.resolve(fileName);
        assertTrue(Files.exists(storedFile));
        assertEquals(content, Files.readString(storedFile));
    }

    @Test
    void store_WithEmptyFile_ShouldThrowException() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile("empty", new byte[0]);

        // Act & Assert
        assertThrows(StorageException.class, () -> storageService.store(emptyFile));
    }

    @Test
    void store_WithRelativePath_ShouldThrowSecurityException() {
        // Arrange
        MultipartFile maliciousFile = mock(MultipartFile.class);
        when(maliciousFile.getOriginalFilename()).thenReturn("../malicious.txt");
        when(maliciousFile.isEmpty()).thenReturn(false);

        // Act & Assert
        assertThrows(StorageException.class, () -> storageService.store(maliciousFile));
    }

    @Test
    void loadAll_ShouldReturnAllStoredFiles() throws IOException {
        // Arrange
        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(tempDir.resolve("file2.txt"));

        // Act
        Stream<Path> loadedFiles = storageService.loadAll();

        // Assert
        assertEquals(2, loadedFiles.count());
    }

    @Test
    void load_ShouldReturnCorrectPath() {
        // Arrange
        String fileName = "test.txt";

        // Act
        Path loadedPath = storageService.load(fileName);

        // Assert
        assertEquals(tempDir.resolve(fileName), loadedPath);
    }

    @Test
    void loadAsResource_WithExistingFile_ShouldReturnResource() throws IOException {
        // Arrange
        String fileName = "test.txt";
        Files.createFile(tempDir.resolve(fileName));

        // Act
        Resource resource = storageService.loadAsResource(fileName);

        // Assert
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void loadAsResource_WithNonExistingFile_ShouldThrowException() {
        // Arrange
        String fileName = "nonexistent.txt";

        // Act & Assert
        assertThrows(StorageFileNotFoundException.class, () -> storageService.loadAsResource(fileName));
    }

    @Test
    void constructor_WithEmptyLocation_ShouldThrowException() {
        // Arrange
        StorageProperties emptyProperties = new StorageProperties();
        emptyProperties.setLocation("");

        // Act & Assert
        assertThrows(StorageException.class, () -> new ImageStorageService(emptyProperties));
    }
}
