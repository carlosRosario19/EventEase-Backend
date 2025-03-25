package com.centennial.eventease_backend.services.implementations;

import com.centennial.eventease_backend.dto.EventDto;
import com.centennial.eventease_backend.entities.Event;
import com.centennial.eventease_backend.repository.contracts.EventDao;
import com.centennial.eventease_backend.services.contracts.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class EventServiceImpl implements EventService {

    private final EventDao eventDao;

    @Autowired
    public EventServiceImpl(@Qualifier("eventDaoImpl") EventDao eventDao){
        this.eventDao = eventDao;
    }

    @Override
    public Page<EventDto> getAll(Pageable pageable) {
        Page<Event> eventPage = eventDao.findAllOrderedByDate(pageable);
        return eventPage.map(eventDtoMapper);
    }

    private Function<Event, EventDto> eventDtoMapper = entity ->
            new EventDto(entity.getId(),
                    entity.getTitle(),
                    entity.getDescription(),
                    entity.getCategory(),
                    entity.getLocation(),
                    entity.getTotalTickets() - entity.getTicketsSold(),
                    entity.getPricePerTicket());
}
