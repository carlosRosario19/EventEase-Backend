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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        Page<Event> result = eventDao.findAllOrderedByDate(pageable);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTotalElements());
        assertNotNull(result.getTotalPages());
        assertEquals("Event 2", result.getContent().get(0).getTitle()); // Should be ordered by date descending
        assertEquals("Event 1", result.getContent().get(1).getTitle());
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
