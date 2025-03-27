package com.centennial.eventease_backend.services;

import com.centennial.eventease_backend.dto.EventDto;
import com.centennial.eventease_backend.entities.Event;
import com.centennial.eventease_backend.exceptions.PageOutOfRangeException;
import com.centennial.eventease_backend.repository.contracts.EventDao;
import com.centennial.eventease_backend.services.contracts.ImageStorageService;
import com.centennial.eventease_backend.services.implementations.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventDao eventDao;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private Resource mockResource;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event testEvent;
    private Page<Event> eventPage;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setId(1);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setCategory("Test");
        testEvent.setLocation("Test Location");
        testEvent.setTotalTickets(100);
        testEvent.setTicketsSold(50);
        testEvent.setPricePerTicket(25.0f);

        eventPage = new PageImpl<>(List.of(testEvent));
    }

    @Test
    void getAll_WithValidPagination_ShouldReturnPageOfEvents() throws PageOutOfRangeException {
        // Arrange
        when(eventDao.findAllOrderedByDate(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(eventPage);

        // Act
        Page<EventDto> result = eventService.getAll(0, 10, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Event", result.getContent().get(0).title());
        verify(eventDao).findAllOrderedByDate(null, null, null, PageRequest.of(0, 10));
    }

    @Test
    void getAll_WithNegativePage_ShouldThrowException() {
        // Act & Assert
        PageOutOfRangeException exception = assertThrows(PageOutOfRangeException.class,
                () -> eventService.getAll(-1, 10, null, null, null));

        assertEquals("Page number cannot be negative", exception.getMessage());
        verifyNoInteractions(eventDao);
    }

    @Test
    void getAll_WithZeroSize_ShouldThrowException() {
        // Act & Assert
        PageOutOfRangeException exception = assertThrows(PageOutOfRangeException.class,
                () -> eventService.getAll(0, 0, null, null, null));

        assertEquals("Page size must be greater than 0", exception.getMessage());
        verifyNoInteractions(eventDao);
    }

    @Test
    void getAll_WithExcessiveSize_ShouldThrowException() {
        // Act & Assert
        PageOutOfRangeException exception = assertThrows(PageOutOfRangeException.class,
                () -> eventService.getAll(0, 101, null, null, null));

        assertEquals("Page size cannot exceed 100", exception.getMessage());
        verifyNoInteractions(eventDao);
    }

    @Test
    void getAll_WithImagePath_ShouldIncludeImageResource() throws PageOutOfRangeException {
        // Arrange
        testEvent.setImagePath("test.jpg");
        when(eventDao.findAllOrderedByDate(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(eventPage);
        when(imageStorageService.load("test.jpg")).thenReturn(mockResource);

        // Act
        Page<EventDto> result = eventService.getAll(0, 10, null, null, null);

        // Assert
        assertNotNull(result.getContent().get(0).image());
        assertEquals(mockResource, result.getContent().get(0).image());
    }
}
