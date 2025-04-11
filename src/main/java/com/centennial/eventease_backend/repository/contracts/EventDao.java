package com.centennial.eventease_backend.repository.contracts;

import com.centennial.eventease_backend.entities.Event;
import com.centennial.eventease_backend.entities.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;


public interface EventDao {
    Page<Event> findAllOrderedByDate(String title, String location, String category, Pageable pageable);
    Optional<Event> findById(int id);
    Optional<Event> findByDateAndLocation(LocalDateTime dateTime, String location);
    void save(Event event);
    Page<Event> findAllByMember(Member member, Pageable pageable);
}
