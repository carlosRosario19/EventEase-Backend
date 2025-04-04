package com.centennial.eventease_backend.controllers;

import com.centennial.eventease_backend.services.contracts.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ImageStorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StorageService imageStorageService;

    @Test
    public void getImage_ShouldReturnImage_WhenFileExists() throws Exception {
        // Arrange
        String filename = "test-image.jpg";
        byte[] imageContent = "fake image content".getBytes();

        // Create a custom Resource implementation that knows its filename
        Resource resource = new ByteArrayResource(imageContent) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        when(imageStorageService.loadAsResource(filename))
                .thenReturn(resource);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/images/{filename}", filename))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\""))
                .andExpect(content().bytes(imageContent));
    }

    @Test
    public void getImage_ShouldReturnNotFound_WhenFileDoesNotExist() throws Exception {
        // Arrange
        String filename = "non-existent-image.jpg";

        when(imageStorageService.loadAsResource(filename))
                .thenReturn(null);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/images/{filename}", filename))
                .andExpect(status().isNotFound());
    }
}
