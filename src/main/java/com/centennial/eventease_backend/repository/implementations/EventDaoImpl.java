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


@Repository
public class EventDaoImpl implements EventDao {

    private final EntityManager entityManager;

    @Autowired
    public EventDaoImpl(EntityManager entityManager){
        this.entityManager = entityManager;
    }



    @Override
    public Page<Event> findAllOrderedByDate(Pageable pageable) {
        // Count query for total elements
        Query countQuery = entityManager.createQuery("SELECT COUNT(e) FROM Event e");
        long total = (Long) countQuery.getSingleResult();

        // Main query with forced descending date sorting
        TypedQuery<Event> query = entityManager.createQuery(
                "SELECT e FROM Event e ORDER BY e.dateTime DESC", Event.class);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(query.getResultList(), pageable, total);
    }
}
