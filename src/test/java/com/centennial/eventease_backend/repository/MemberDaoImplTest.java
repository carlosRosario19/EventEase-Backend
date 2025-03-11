package com.centennial.eventease_backend.repository;

import com.centennial.eventease_backend.entities.Member;
import com.centennial.eventease_backend.entities.User;
import com.centennial.eventease_backend.repository.contracts.MemberDao;
import com.centennial.eventease_backend.repository.contracts.UserDao;
import com.centennial.eventease_backend.repository.implementations.MemberDaoImpl;
import com.centennial.eventease_backend.repository.implementations.UserDaoImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class MemberDaoImplTest {

    @Autowired
    private EntityManager entityManager;

    private MemberDao memberDao;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        memberDao = new MemberDaoImpl(entityManager);
        userDao = new UserDaoImpl(entityManager);
    }

    @Transactional
    @Test
    void create_shouldPersistMember(){
        // Arrange
        Member member = new Member(
                "Doe",
                "John",
                "6473967414",
                LocalDate.now(),
                "doe123");

        member.setMemberId(105);
        userDao.create(new User(member.getUsername(), "test123", 'Y'));

        //Act
        memberDao.save(member);
        entityManager.flush(); // Ensure the entity is persisted

        // Assert
        Member persistedMember = entityManager.find(Member.class, member.getMemberId());

        assertThat(persistedMember).isNotNull();
        assertThat(persistedMember.getFirstName()).isEqualTo("Doe");
        assertThat(persistedMember.getLastName()).isEqualTo("John");
        assertThat(persistedMember.getPhone()).isEqualTo("6473967414");
        assertThat(persistedMember.getUsername()).isEqualTo("doe123");
    }


    @Transactional
    @Test
    void findByUsername_shouldReturnMember_whenMemberExists(){
        // Arrange
        Member member = new Member(
                "Doe",
                "John",
                "6473967414",
                LocalDate.now(),
                "doe123");

        member.setMemberId(105);
        userDao.create(new User(member.getUsername(), "test123", 'Y'));

        memberDao.save(member);
        entityManager.flush();

        // Act
        Optional<Member> foundMember = memberDao.findByUsername("doe123");

        // Assert
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getFirstName()).isEqualTo("Doe");
    }

}
