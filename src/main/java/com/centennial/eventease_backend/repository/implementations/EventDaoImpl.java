package com.centennial.eventease_backend.repository.implementations;

import com.centennial.eventease_backend.entities.Event;
import com.centennial.eventease_backend.repository.contracts.EventDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Repository
public class EventDaoImpl implements EventDao {

    private final EntityManager entityManager;

    @Autowired
    public EventDaoImpl(EntityManager entityManager){
        this.entityManager = entityManager;
    }



    @Override
    public Page<Event> findAllOrderedByDate(String title, String location, String category, Pageable pageable) {
        // Base queries
        String countJpql = "SELECT COUNT(e) FROM Event e WHERE 1=1";
        String selectJpql = "SELECT e FROM Event e WHERE 1=1";

        // Build dynamic WHERE clauses
        Map<String,Object> params = new HashMap<>();

        if (title != null && !title.isBlank()) {
            countJpql += " AND UPPER(e.title) LIKE UPPER(:title)";
            selectJpql += " AND UPPER(e.title) LIKE UPPER(:title)";
            params.put("title", title + "%");
        }

        if (location != null && !location.isBlank()) {
            countJpql += " AND UPPER(e.location) LIKE UPPER(:location)";
            selectJpql += " AND UPPER(e.location) LIKE UPPER(:location)";
            params.put("location", "%" + location + "%");
        }

        if (category != null && !category.isBlank()) {
            countJpql += " AND UPPER(e.category) LIKE UPPER(:category)";
            selectJpql += " AND UPPER(e.category) LIKE UPPER(:category)";
            params.put("category", "%" + category + "%");
        }

        // Add sorting
        selectJpql += " ORDER BY e.dateTime DESC";

        // Execute count query
        Query countQuery = entityManager.createQuery(countJpql);
        params.forEach(countQuery::setParameter);
        long total = (Long) countQuery.getSingleResult();

        // Execute select query
        TypedQuery<Event> query = entityManager.createQuery(selectJpql, Event.class);
        params.forEach(query::setParameter);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    @Override
    public Optional<Event> findById(int id) {
        try {
            Event event = entityManager.find(Event.class, id);
            return Optional.ofNullable(event);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
