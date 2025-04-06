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
import java.time.LocalDateTime;
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
                "John",
                "Doe",
                "6473179845",
                LocalDate.now(),
                "doe",
                "john.doe@example.com",
                "123456789012",
                "123456789",
                "Royal Bank of Canada",
                "Canada"
        );

        userDao.create(new User(member.getUsername(), "test123", 'Y'));

        //Act
        memberDao.save(member);
        entityManager.flush(); // Ensure the entity is persisted

        // Assert
        Member persistedMember = entityManager.find(Member.class, member.getMemberId());

        assertThat(persistedMember).isNotNull();
        assertThat(persistedMember.getFirstName()).isEqualTo("John");
        assertThat(persistedMember.getLastName()).isEqualTo("Doe");
        assertThat(persistedMember.getPhone()).isEqualTo("6473179845");
        assertThat(persistedMember.getUsername()).isEqualTo("doe");
    }


    @Transactional
    @Test
    void findByUsername_shouldReturnMember_whenMemberExists(){
        // Arrange
        Member member = new Member(
                "John",
                "Doe",
                "6473179845",
                LocalDate.now(),
                "doe",
                "john.doe@example.com",
                "123456789012",
                "123456789",
                "Royal Bank of Canada",
                "Canada"
        );

        userDao.create(new User(member.getUsername(), "test123", 'Y'));

        memberDao.save(member);
        entityManager.flush();

        // Act
        Optional<Member> foundMember = memberDao.findByUsername("doe");

        // Assert
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getFirstName()).isEqualTo("John");
    }

    @Transactional
    @Test
    void findById_shouldReturnMember_whenMemberExists() {
        // Arrange
        Member member = new Member(
                "John",
                "Doe",
                "6473179845",
                LocalDate.now(),
                "doe",
                "john.doe@example.com",
                "123456789012",
                "123456789",
                "Royal Bank of Canada",
                "Canada"
        );

        // Create associated User
        userDao.create(new User(member.getUsername(), "test123", 'Y'));

        // Save member
        memberDao.save(member);
        entityManager.flush();

        // Get the ID of the saved member
        int memberId = member.getMemberId();

        // Act
        Optional<Member> foundMember = memberDao.findById(memberId);

        // Assert
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getFirstName()).isEqualTo("John");
        assertThat(foundMember.get().getLastName()).isEqualTo("Doe");
        assertThat(foundMember.get().getPhone()).isEqualTo("6473179845");
    }

    @Transactional
    @Test
    void findById_shouldReturnEmpty_whenMemberDoesNotExist() {
        // Arrange - no setup needed

        // Act
        Optional<Member> foundMember = memberDao.findById(999); // Non-existent ID

        // Assert
        assertThat(foundMember).isEmpty();
    }

    @Transactional
    @Test
    void update_shouldMergeMemberChanges() {
        // Arrange
        // First create and persist a member
        Member member = new Member(
                "John",
                "Doe",
                "6473179845",
                LocalDate.now(),
                "doe",
                "john.doe@example.com",
                "123456789012",
                "123456789",
                "Royal Bank of Canada",
                "Canada"
        );

        userDao.create(new User(member.getUsername(), "test123", 'Y'));
        memberDao.save(member);
        entityManager.flush();
        entityManager.detach(member); // Detach to simulate a detached entity

        // Modify the detached member
        member.setFirstName("UpdatedJohn");
        member.setLastName("UpdatedDoe");
        member.setPhone("555-555-5555");
        member.setEmail("updated@example.com");

        // Act
        memberDao.update(member);
        entityManager.flush();

        // Assert
        Member updatedMember = entityManager.find(Member.class, member.getMemberId());
        assertThat(updatedMember).isNotNull();
        assertThat(updatedMember.getFirstName()).isEqualTo("UpdatedJohn");
        assertThat(updatedMember.getLastName()).isEqualTo("UpdatedDoe");
        assertThat(updatedMember.getPhone()).isEqualTo("555-555-5555");
        assertThat(updatedMember.getEmail()).isEqualTo("updated@example.com");
        // Verify username wasn't changed
        assertThat(updatedMember.getUsername()).isEqualTo("doe");
    }
}
