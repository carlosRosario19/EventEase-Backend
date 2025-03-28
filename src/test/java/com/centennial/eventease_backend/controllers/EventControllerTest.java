package com.centennial.eventease_backend.controllers;

import com.centennial.eventease_backend.dto.EventDto;
import com.centennial.eventease_backend.dto.GetEventDto;
import com.centennial.eventease_backend.exceptions.PageOutOfRangeException;
import com.centennial.eventease_backend.services.contracts.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getAllEvents_ShouldReturnPaginatedEvents() throws Exception {
        // Arrange
        EventDto event1 = new EventDto(1, "Tech Conference", "Annual tech event", null,
                "Technology", "Convention Center", 250, 199.99f);
        EventDto event2 = new EventDto(2, "Music Festival", "Summer music festival", null,
                "Music", "Central Park", 5000, 89.99f);

        List<EventDto> events = Arrays.asList(event1, event2);
        Page<EventDto> page = new PageImpl<>(events);

        when(eventService.getAll(anyInt(), anyInt(), isNull(), isNull(), isNull())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Tech Conference"))
                .andExpect(jsonPath("$.content[1].title").value("Music Festival"));
    }

    @Test
    public void getAllEvents_WithDefaultPagination_ShouldUseDefaults() throws Exception {
        // Arrange
        Page<EventDto> emptyPage = Page.empty();
        when(eventService.getAll(anyInt(), anyInt(), isNull(), isNull(), isNull())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    public void getAllEvents_WithNegativePage_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(eventService.getAll(-1, 10, null, null, null))
                .thenThrow(new PageOutOfRangeException("Page number cannot be negative"));

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .param("page", "-1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Page Out of Range Error"))
                .andExpect(jsonPath("$.detail").value("Page number cannot be negative"));
    }

    @Test
    public void getAllEvents_WithZeroSize_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(eventService.getAll(0, 0, null, null, null))
                .thenThrow(new PageOutOfRangeException("Page size must be greater than 0"));

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .param("page", "0")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Page Out of Range Error"))
                .andExpect(jsonPath("$.detail").value("Page size must be greater than 0"));
    }

    @Test
    public void getAllEvents_WithExcessiveSize_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(eventService.getAll(0, 101, null, null, null))
                .thenThrow(new PageOutOfRangeException("Page size cannot exceed 100"));

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .param("page", "0")
                        .param("size", "101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Page Out of Range Error"))
                .andExpect(jsonPath("$.detail").value("Page size cannot exceed 100"));
    }

    @Test
    public void getEvents_WithTitleFilter_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        EventDto techEvent = new EventDto(1, "Tech Conference", "Annual tech event", null,
                "Technology", "Convention Center", 250, 199.99f);
        Page<EventDto> page = new PageImpl<>(List.of(techEvent));

        when(eventService.getAll(anyInt(), anyInt(), eq("Tech"), isNull(), isNull()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .param("page", "0")
                        .param("size", "10")
                        .param("title", "Tech")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Tech Conference"));
    }

    @Test
    public void getEvent_WhenEventExists_ShouldReturnEvent() throws Exception {
        // Arrange
        int eventId = 1;
        GetEventDto mockEvent = new GetEventDto(eventId, "Test Event", "Description", null, "category test", LocalDateTime.now(), "test location", 30, 200);

        when(eventService.get(eventId))
                .thenReturn(Optional.of(mockEvent));

        // Act & Assert
        mockMvc.perform(get("/api/events/{id}", eventId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.title").value("Test Event"));
    }

    @Test
    public void getEvent_WhenEventNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        int nonExistentId = 999;

        when(eventService.get(nonExistentId))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/events/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getEvent_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert for non-numeric ID
        mockMvc.perform(get("/api/events/{id}", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
