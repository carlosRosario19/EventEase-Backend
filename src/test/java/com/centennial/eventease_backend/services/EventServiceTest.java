package com.centennial.eventease_backend.services;

import com.centennial.eventease_backend.dto.CreateEventDto;
import com.centennial.eventease_backend.dto.EventDto;
import com.centennial.eventease_backend.dto.GetEventDto;
import com.centennial.eventease_backend.entities.Event;
import com.centennial.eventease_backend.entities.Member;
import com.centennial.eventease_backend.exceptions.*;
import com.centennial.eventease_backend.repository.contracts.EventDao;
import com.centennial.eventease_backend.repository.contracts.MemberDao;
import com.centennial.eventease_backend.services.contracts.StorageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventDao eventDao;

    @Mock
    private StorageService imageStorageService;

    @Mock
    private MemberDao memberDao;

    @Mock
    private Resource mockResource;


    @InjectMocks
    private EventServiceImpl eventService;

    private Event testEvent;
    private Page<Event> eventPage;
    private CreateEventDto validCreateEventDto;
    private Member testMember;

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
        testMember = new Member();
        testMember.setMemberId(1);

        validCreateEventDto = new CreateEventDto(
                "Test Event",
                "Test Description",
                mock(MultipartFile.class),
                "Test Category",
                LocalDateTime.now().plusDays(1),
                "Test Location",
                100,
                25.0f,
                1
        );
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
        assertEquals("Test Event", result.getContent().getFirst().title());
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
    public void get_WhenEventExistsWithoutImage_ShouldReturnDtoWithNullImage() throws Exception {
        // Arrange
        int eventId = 1;
        testEvent.setImagePath(null);

        when(eventDao.findById(eventId)).thenReturn(Optional.of(testEvent));

        // Act
        Optional<GetEventDto> result = eventService.get(eventId);

        // Assert
        assertTrue(result.isPresent());
        assertNull(result.get().imagePath());
        verify(imageStorageService, never()).load(anyString());
    }

    @Test
    public void get_WhenEventNotExists_ShouldThrowEventNotFoundException() {
        // Arrange
        int nonExistentId = 999;

        when(eventDao.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EventNotFoundException.class, () -> {
            eventService.get(nonExistentId);
        });
    }

    @Test
    void save_WhenEventConflict_ShouldThrowEventConflictException() {
        // Arrange
        when(eventDao.findByDateAndLocation(any(), any())).thenReturn(Optional.of(testEvent));

        // Act & Assert
        assertThrows(EventConflictException.class, () -> eventService.save(validCreateEventDto));
        verify(eventDao, never()).save(any());
    }

    @Test
    void save_WhenDateIsPast_ShouldThrowInvalidDateTimeException() {
        // Arrange
        CreateEventDto pastEventDto = new CreateEventDto(
                "Test Event", "Description", mock(MultipartFile.class), "Test Category",
                LocalDateTime.now().minusDays(1), "Location", 100, 20.0f, 1);

        // Act & Assert
        assertThrows(InvalidDateTimeException.class, () -> eventService.save(pastEventDto));
        verify(eventDao, never()).save(any());
    }

    @Test
    void save_WhenPriceIsNegative_ShouldThrowInvalidPriceException() {
        // Arrange
        CreateEventDto invalidPriceEventDto = new CreateEventDto(
                "Test Event", "Description", mock(MultipartFile.class), "Test Category",
                LocalDateTime.now().plusDays(1), "Location", 100, -10.0f, 1);

        // Act & Assert
        assertThrows(InvalidPriceException.class, () -> eventService.save(invalidPriceEventDto));
        verify(eventDao, never()).save(any());
    }

    @Test
    void save_WhenFileIsUploaded_ShouldStoreFile() throws Exception {
        // Arrange
        MultipartFile fileMock = mock(MultipartFile.class);
        when(fileMock.getOriginalFilename()).thenReturn("test.jpg");
        when(fileMock.isEmpty()).thenReturn(false);
        CreateEventDto dtoWithFile = new CreateEventDto(
                "Test Event", "Description", fileMock, "Test Category",
                LocalDateTime.now().plusDays(1), "Location", 100, 20.0f, 1);

        when(eventDao.findByDateAndLocation(any(), any())).thenReturn(Optional.empty());
        when(memberDao.findById(anyInt())).thenReturn(Optional.of(testMember));

        // Act
        eventService.save(dtoWithFile);

        // Assert
        verify(imageStorageService).store(any(MultipartFile.class));
    }

}
