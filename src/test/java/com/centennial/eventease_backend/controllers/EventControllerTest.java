package com.centennial.eventease_backend.controllers;

import com.centennial.eventease_backend.dto.EventDto;
import com.centennial.eventease_backend.services.contracts.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

        when(eventService.getAll(any(Pageable.class))).thenReturn(page);

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
        when(eventService.getAll(any(Pageable.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

}
