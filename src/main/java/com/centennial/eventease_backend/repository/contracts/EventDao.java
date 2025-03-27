package com.centennial.eventease_backend.repository.contracts;

import com.centennial.eventease_backend.entities.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


public interface EventDao {
    Page<Event> findAllOrderedByDate(String title, String location, String category, Pageable pageable);
    Optional<Event> findById(int id);
}
