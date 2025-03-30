package com.centennial.eventease_backend.repository;

import com.centennial.eventease_backend.entities.Event;
import com.centennial.eventease_backend.repository.contracts.EventDao;
import com.centennial.eventease_backend.repository.implementations.EventDaoImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class EventDaoImplTest {

    @Autowired
    private EntityManager entityManager;

    private EventDao eventDao;

    @BeforeEach
    void setUp() {
        eventDao = new EventDaoImpl(entityManager);
    }

    @Test
    @Transactional
    void findAllOrderedByDate_ShouldReturnPaginatedEvents() {
        // Arrange
        Event event1 = createTestEvent("Event 1", LocalDateTime.now().plusDays(1));
        Event event2 = createTestEvent("Event 2", LocalDateTime.now().plusDays(2));
        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Event> result = eventDao.findAllOrderedByDate(null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTotalElements());
        assertNotNull(result.getTotalPages());
        assertEquals("Event 2", result.getContent().get(0).getTitle()); // Should be ordered by date descending
        assertEquals("Event 1", result.getContent().get(1).getTitle());
    }

    @Test
    @Transactional
    void findById_WhenEventExists_ShouldReturnEvent() {
        // Arrange
        Event testEvent = createTestEvent("Test Event", LocalDateTime.now());
        entityManager.persist(testEvent);
        entityManager.flush();

        // Act
        Optional<Event> result = eventDao.findById(testEvent.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testEvent.getId(), result.get().getId());
        assertEquals(testEvent.getTitle(), result.get().getTitle());
    }

    @Test
    @Transactional
    void findById_WhenEventNotExists_ShouldReturnEmpty() {
        // Arrange - no event persisted
        int nonExistentId = 999;

        // Act
        Optional<Event> result = eventDao.findById(nonExistentId);

        // Assert
        assertFalse(result.isPresent());
    }

    @Transactional
    @Test
    void save_ShouldPersistEventWithAllFields() {
        // Arrange
        Event testEvent = createTestEvent("New Event", LocalDateTime.now().plusDays(1));

        // Act
        eventDao.save(testEvent); // void method
        entityManager.flush(); // Ensure persistence
        entityManager.clear(); // Clear cache to ensure we get from DB

        // Assert
        Event retrievedEvent = entityManager.find(Event.class, testEvent.getId());
        assertNotNull(retrievedEvent, "Event should be found in database");
        assertAll(
                () -> assertEquals(testEvent.getTitle(), retrievedEvent.getTitle(), "Title should match"),
                () -> assertEquals(testEvent.getDescription(), retrievedEvent.getDescription(), "Description should match"),
                () -> assertEquals(testEvent.getCategory(), retrievedEvent.getCategory(), "Category should match"),
                () -> assertEquals(testEvent.getLocation(), retrievedEvent.getLocation(), "Location should match"),
                () -> assertEquals(testEvent.getTotalTickets(), retrievedEvent.getTotalTickets(), "Total tickets should match"),
                () -> assertEquals(testEvent.getPricePerTicket(), retrievedEvent.getPricePerTicket(), "Price per ticket should match")
        );
    }

    @Transactional
    @Test
    void findByDateAndLocation_ShouldReturnEvent_WhenMatchExists() {
        // Arrange - Use fixed datetime with seconds precision
        LocalDateTime testDateTime = LocalDateTime.now()
                .withNano(0) // Clear nanoseconds
                .plusDays(1);
        String testLocation = "Test Location";

        Event event = createTestEvent("Find Test", testDateTime);
        event.setLocation(testLocation);
        eventDao.save(event);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to ensure fresh load

        // Act
        Optional<Event> result = eventDao.findByDateAndLocation(testDateTime, testLocation);

        // Assert
        assertTrue(result.isPresent(), "Event should be found");
        assertEquals(event.getId(), result.get().getId(), "IDs should match");

        // Debug output if needed
        System.out.println("Saved datetime: " + event.getDateTime());
        System.out.println("Queried datetime: " + testDateTime);
        System.out.println("Found datetime: " + (result.isPresent() ? result.get().getDateTime() : "NOT FOUND"));
    }

    @Transactional
    @Test
    void findByDateAndLocation_ShouldReturnEmpty_WhenNoMatch() {
        // Arrange
        LocalDateTime testDateTime = LocalDateTime.now().plusDays(1);
        String testLocation = "Existing Location";
        Event event = createTestEvent("Existing Event", testDateTime);
        event.setLocation(testLocation);
        eventDao.save(event);
        entityManager.flush();

        // Act
        Optional<Event> result = eventDao.findByDateAndLocation(
                testDateTime.plusDays(1), // Different date
                testLocation
        );

        // Assert
        assertFalse(result.isPresent(), "Should not find event with different date");
    }



    private Event createTestEvent(String title, LocalDateTime dateTime) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription("Description for " + title);
        event.setCategory("Test");
        event.setDateTime(dateTime);
        event.setLocation("Test Location");
        event.setTotalTickets(100);
        event.setTicketsSold(0);
        event.setPricePerTicket(50.0f);
        event.setCreatedAt(LocalDateTime.now());
        return event;
    }
}
