package com.centennial.eventease_backend.repository.implementations;

import com.centennial.eventease_backend.entities.User;
import com.centennial.eventease_backend.repository.contracts.UserDao;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {

    private final EntityManager entityManager;

    @Autowired
    public UserDaoImpl(EntityManager entityManager){
        this.entityManager = entityManager;
    }


    @Override
    public void create(User user){
        entityManager.persist(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String query = "SELECT u FROM User u WHERE u.username = :username";
        try{
            User user = entityManager.createQuery(query, User.class)
                    .setParameter("username", username)
                    .getSingleResult();

            return Optional.ofNullable(user);
        } catch (Exception e){
            return Optional.empty();
        }
    }
}
