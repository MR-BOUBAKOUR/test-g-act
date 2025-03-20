package com.payMyBuddy.repository;

import com.payMyBuddy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The interface User repository.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Find by email and return a user - optional.
     *
     * @param email the email
     * @return the optional
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email and return a boolean.
     *
     * @param email the email
     * @return the boolean
     */
    boolean existsByEmail(String email);

}