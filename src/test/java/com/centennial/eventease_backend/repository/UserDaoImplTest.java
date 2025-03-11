package com.centennial.eventease_backend.repository;

import com.centennial.eventease_backend.entities.Authority;
import com.centennial.eventease_backend.entities.AuthorityId;
import com.centennial.eventease_backend.entities.User;
import com.centennial.eventease_backend.repository.contracts.UserDao;
import com.centennial.eventease_backend.repository.implementations.UserDaoImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserDaoImplTest {
    @Autowired
    private EntityManager entityManager;

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl(entityManager);
    }

    @Transactional
    @Test
    void testCreateUser() {
        // Arrange
        User user = new User("testUser", "testPassword", 'Y');
        Set<Authority> authorities = new HashSet<>();
        Authority authority = new Authority();
        authority.setUser(user);
        authority.setId(new AuthorityId(user.getUsername(), "ROLE_MEMBER"));
        authorities.add(authority);
        user.setAuthorities(authorities);

        // Act
        userDao.create(user);
        User persistedUser = entityManager.find(User.class, "testUser");

        // Assert
        assertNotNull(persistedUser, "User should be persisted in the database.");
        assertEquals("testUser", persistedUser.getUsername());
        assertEquals("testPassword", persistedUser.getPassword());
        assertEquals('Y', persistedUser.getEnabled());
        assertNotNull(persistedUser.getAuthorities());
        assertEquals(1, persistedUser.getAuthorities().size());
    }

    @Transactional
    @Test
    void testFindByUsername_UserExists() {
        // Arrange
        User user = new User("existingUser", "password123", 'Y');
        entityManager.persist(user);
        entityManager.flush();

        // Act
        Optional<User> result = userDao.findByUsername("existingUser");

        // Assert
        assertTrue(result.isPresent(), "User should be found in the database.");
        assertEquals("existingUser", result.get().getUsername());
        assertEquals("password123", result.get().getPassword());
        assertEquals('Y', result.get().getEnabled());
    }

    @Test
    void testFindByUsername_UserDoesNotExist() {
        // Act
        Optional<User> result = userDao.findByUsername("nonExistentUser");

        // Assert
        assertFalse(result.isPresent(), "User should not be found in the database.");
    }
}
