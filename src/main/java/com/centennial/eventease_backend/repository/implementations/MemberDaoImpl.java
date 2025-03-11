package com.centennial.eventease_backend.repository.implementations;

import com.centennial.eventease_backend.entities.Member;
import com.centennial.eventease_backend.repository.contracts.MemberDao;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MemberDaoImpl implements MemberDao {

    private final EntityManager entityManager;

    @Autowired
    public MemberDaoImpl(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        String query = "SELECT m FROM Member m WHERE m.username = :username";
        try{
            Member member = entityManager.createQuery(query, Member.class)
                    .setParameter("username", username)
                    .getSingleResult();

            return Optional.ofNullable(member);
        } catch (Exception e){
            return Optional.empty();
        }
    }

    @Override
    public void save(Member member) {
        entityManager.persist(member);
    }
}
