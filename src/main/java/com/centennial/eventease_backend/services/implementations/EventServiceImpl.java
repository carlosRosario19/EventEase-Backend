package com.centennial.eventease_backend.services.implementations;

import com.centennial.eventease_backend.dto.CreateEventDto;
import com.centennial.eventease_backend.dto.EventDto;
import com.centennial.eventease_backend.dto.GetEventDto;
import com.centennial.eventease_backend.dto.RenamedMultipartFile;
import com.centennial.eventease_backend.entities.Event;
import com.centennial.eventease_backend.entities.Member;
import com.centennial.eventease_backend.exceptions.*;
import com.centennial.eventease_backend.repository.contracts.EventDao;
import com.centennial.eventease_backend.repository.contracts.MemberDao;
import com.centennial.eventease_backend.services.contracts.EventService;
import com.centennial.eventease_backend.services.contracts.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
public class EventServiceImpl implements EventService {

    private final EventDao eventDao;
    private final StorageService imageStorageService;
    private final MemberDao memberDao;
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    public EventServiceImpl(@Qualifier("eventDaoImpl") EventDao eventDao,
                            @Qualifier("imageStorageService") StorageService imageStorageService,
                            @Qualifier("memberDaoImpl") MemberDao memberDao){
        this.eventDao = eventDao;
        this.memberDao = memberDao;
        this.imageStorageService = imageStorageService;
    }

    @Override
    public Page<EventDto> getAll(int page, int size, String title, String location, String category) throws PageOutOfRangeException {

        if(page < 0){
            throw new PageOutOfRangeException("Page number cannot be negative");
        }
        else if(size <= 0){
            throw new PageOutOfRangeException("Page size must be greater than 0");
        }
        else if(size > MAX_PAGE_SIZE){
            throw new PageOutOfRangeException("Page size cannot exceed " + MAX_PAGE_SIZE);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventPage = eventDao.findAllOrderedByDate(title, location, category, pageable);
        return eventPage.map(eventDtoMapper);
    }

    @Override
    public Optional<GetEventDto> get(int id) throws EventNotFoundException {
        return Optional.ofNullable(eventDao.findById(id)
                .map(getEventDtoMapper)
                .orElseThrow(() -> new EventNotFoundException("The event with id {" + id + "} was not found")));
    }

    @Transactional
    @Override
    public void save(CreateEventDto createEventDto) throws EventConflictException, InvalidDateTimeException, InvalidPriceException, MemberNotFoundException {

        if (eventDao.findByDateAndLocation(createEventDto.dateTime(), createEventDto.location()).isPresent()) {
            throw new EventConflictException("An event already exists at this location and time");
        }
        // Validate dateTime is in the future
        if (createEventDto.dateTime().isBefore(LocalDateTime.now())) {
            throw new InvalidDateTimeException("Event date must be in the future");
        }
        // Validate price is not negative
        if (createEventDto.pricePerTicket() <= 0) {
            throw new InvalidPriceException("Price must be greater than or equal to zero");
        }

        // Handle file upload
        String filename = null;
        if (createEventDto.file() != null && !createEventDto.file().isEmpty()) {
            try {
                // Generate unique filename while preserving extension
                String originalFilename = Objects.requireNonNull(createEventDto.file().getOriginalFilename());
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                filename = UUID.randomUUID() + extension;

                // Create a renamed version of the file
                MultipartFile renamedFile = new RenamedMultipartFile(createEventDto.file(), filename);

                // Store the file
                imageStorageService.store(renamedFile);
            } catch (Exception e) {
                throw new StorageException("Failed to store event image: " + e.getMessage());
            }
        }

        // Map DTO to Event entity
        Event event = createEventDtoMapper.apply(createEventDto);
        event.setImagePath(filename);
        // Set member
        Member member = memberDao.findById(createEventDto.memberId())
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + createEventDto.memberId()));
        event.setMember(member);
        // Set creation timestamp
        event.setCreatedAt(LocalDateTime.now());
        event.setTicketsSold(0); // Initialize tickets sold to 0
        // Save event
        eventDao.save(event);
    }

    @Override
    public Page<EventDto> getAllByUsername(String username, int page, int size) throws PageOutOfRangeException, MemberNotFoundException {
        if(page < 0){
            throw new PageOutOfRangeException("Page number cannot be negative");
        }
        else if(size <= 0){
            throw new PageOutOfRangeException("Page size must be greater than 0");
        }
        else if(size > MAX_PAGE_SIZE){
            throw new PageOutOfRangeException("Page size cannot exceed " + MAX_PAGE_SIZE);
        }
        Member member = memberDao.findByUsername(username)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with username: " + username));

        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventPage = eventDao.findAllByMember(member, pageable);
        return eventPage.map(eventDtoMapper);
    }

    private final Function<Event, EventDto> eventDtoMapper = entity -> {
        int ticketsLeft = entity.getTotalTickets() - entity.getTicketsSold();
        return new EventDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getImagePath(),
                entity.getCategory(),
                entity.getLocation(),
                ticketsLeft,
                entity.getPricePerTicket());
    };

    private final Function<Event, GetEventDto> getEventDtoMapper = entity -> {
        int ticketsLeft = entity.getTotalTickets() - entity.getTicketsSold();
        return new GetEventDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getImagePath(),
                entity.getCategory(),
                entity.getDateTime(),
                entity.getLocation(),
                ticketsLeft,
                entity.getPricePerTicket()
        );
    };


    private final Function<CreateEventDto, Event> createEventDtoMapper = dto -> {
        Event event = new Event();
        event.setTitle(dto.title());
        event.setDescription(dto.description());
        event.setCategory(dto.category());
        event.setDateTime(dto.dateTime());
        event.setLocation(dto.location());
        event.setTotalTickets(dto.totalTickets());
        event.setPricePerTicket(dto.pricePerTicket());

        return event;
    };


}
