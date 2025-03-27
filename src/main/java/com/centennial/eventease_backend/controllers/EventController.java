package com.centennial.eventease_backend.controllers;

import com.centennial.eventease_backend.dto.EventDto;
import com.centennial.eventease_backend.exceptions.PageOutOfRangeException;
import com.centennial.eventease_backend.services.contracts.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(@Qualifier("eventServiceImpl") EventService eventService){
        this.eventService = eventService;
    }

    @GetMapping("/events")
    public Page<EventDto> getAllEvents(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) String title,
                                       @RequestParam(required = false) String location,
                                       @RequestParam(required = false) String category) throws PageOutOfRangeException {
        return eventService.getAll(page, size, title, location, category);
    }

}
