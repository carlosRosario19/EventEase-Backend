package com.centennial.eventease_backend.controllers;

import com.centennial.eventease_backend.dto.CreateEventDto;
import com.centennial.eventease_backend.dto.EventDto;
import com.centennial.eventease_backend.dto.GetEventDto;
import com.centennial.eventease_backend.exceptions.*;
import com.centennial.eventease_backend.services.contracts.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

    private MockMultipartFile validFile;
    private LocalDateTime futureDateTime;

    @BeforeEach
    public void setup() {
        validFile = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );
        futureDateTime = LocalDateTime.now().plusDays(1);
    }

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

    @Test
    @WithMockUser(roles = "MEMBER")
    public void saveEvent_ShouldReturnCreated_WhenValidRequest() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1);

        doNothing().when(eventService).save(any(CreateEventDto.class));

        // Act & Assert
        mockMvc.perform(multipart("/api/events")
                        .file(file)
                        .param("title", "Test Event")
                        .param("description", "Test Description")
                        .param("category", "Music")
                        .param("dateTime", futureDateTime.toString())
                        .param("location", "Test Location")
                        .param("totalTickets", "100")
                        .param("pricePerTicket", "25.50")
                        .param("memberId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    @Test
    public void saveEvent_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/events")
                        .file(validFile)
                        .param("title", "Test Event")
                        .param("description", "Test Description")
                        .param("category", "Music")
                        .param("dateTime", futureDateTime.toString())
                        .param("location", "Test Location")
                        .param("totalTickets", "100")
                        .param("pricePerTicket", "25.50")
                        .param("memberId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void saveEvent_ShouldReturnConflict_WhenEventConflictException() throws Exception {
        // Arrange
        doThrow(new EventConflictException("Event conflict"))
                .when(eventService).save(any(CreateEventDto.class));

        // Act & Assert
        mockMvc.perform(multipart("/api/events")
                        .file(validFile)
                        .param("title", "Test Event")
                        .param("description", "Test Description")
                        .param("category", "Music")
                        .param("dateTime", futureDateTime.toString())
                        .param("location", "Test Location")
                        .param("totalTickets", "100")
                        .param("pricePerTicket", "25.50")
                        .param("memberId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void saveEvent_ShouldReturnBadRequest_WhenInvalidDateTime() throws Exception {
        // Arrange
        LocalDateTime pastDateTime = LocalDateTime.now().minusDays(1);
        doThrow(new InvalidDateTimeException("Invalid date time"))
                .when(eventService).save(any(CreateEventDto.class));

        // Act & Assert
        mockMvc.perform(multipart("/api/events")
                        .file(validFile)
                        .param("title", "Test Event")
                        .param("description", "Test Description")
                        .param("category", "Music")
                        .param("dateTime", pastDateTime.toString())
                        .param("location", "Test Location")
                        .param("totalTickets", "100")
                        .param("pricePerTicket", "25.50")
                        .param("memberId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void saveEvent_ShouldReturnBadRequest_WhenInvalidPrice() throws Exception {
        // Arrange
        doThrow(new InvalidPriceException("Invalid price"))
                .when(eventService).save(any(CreateEventDto.class));

        // Act & Assert
        mockMvc.perform(multipart("/api/events")
                        .file(validFile)
                        .param("title", "Test Event")
                        .param("description", "Test Description")
                        .param("category", "Music")
                        .param("dateTime", futureDateTime.toString())
                        .param("location", "Test Location")
                        .param("totalTickets", "100")
                        .param("pricePerTicket", "-10.00")
                        .param("memberId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    public void saveEvent_ShouldReturnNotFound_WhenMemberNotFound() throws Exception {
        // Arrange
        doThrow(new MemberNotFoundException("Member not found"))
                .when(eventService).save(any(CreateEventDto.class));

        // Act & Assert
        mockMvc.perform(multipart("/api/events")
                        .file(validFile)
                        .param("title", "Test Event")
                        .param("description", "Test Description")
                        .param("category", "Music")
                        .param("dateTime", futureDateTime.toString())
                        .param("location", "Test Location")
                        .param("totalTickets", "100")
                        .param("pricePerTicket", "25.50")
                        .param("memberId", "999")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getAllEventsByUsername_WhenUserExists_ShouldReturnPaginatedEvents() throws Exception {
        // Arrange
        String username = "testuser";
        EventDto event1 = new EventDto(1, "User Event 1", "Description 1", null,
                "Category", "Location", 100, 50.0f);
        EventDto event2 = new EventDto(2, "User Event 2", "Description 2", null,
                "Category", "Location", 200, 75.0f);

        Page<EventDto> page = new PageImpl<>(Arrays.asList(event1, event2));

        when(eventService.getAllByUsername(eq(username), anyInt(), anyInt()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/events/member/{username}", username)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("User Event 1"))
                .andExpect(jsonPath("$.content[1].title").value("User Event 2"));
    }

    @Test
    public void getAllEventsByUsername_WithDefaultPagination_ShouldUseDefaults() throws Exception {
        // Arrange
        String username = "testuser";
        Page<EventDto> emptyPage = Page.empty();

        when(eventService.getAllByUsername(eq(username), eq(0), eq(10)))
                .thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/events/member/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    public void getAllEventsByUsername_WhenUserNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        String username = "nonexistent";

        when(eventService.getAllByUsername(eq(username), anyInt(), anyInt()))
                .thenThrow(new MemberNotFoundException("Member not found"));

        // Act & Assert
        mockMvc.perform(get("/api/events/member/{username}", username))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Member Not Found Error"))
                .andExpect(jsonPath("$.detail").value("Member not found"));
    }


}
