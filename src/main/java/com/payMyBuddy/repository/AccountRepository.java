package com.payMyBuddy.repository;

import com.payMyBuddy.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * The interface Account repository.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    /**
     * Check if account exists by name and user id and return a boolean.
     *
     * @param name   the name
     * @param userId the user id
     * @return the boolean
     */
    boolean existsByNameAndUser_Id(String name, Integer userId);

    /**
     * Find by user id and return a set of account.
     *
     * @param userId the user id
     * @return the set
     */
    Set<Account> findByUserId(Integer userId);
}