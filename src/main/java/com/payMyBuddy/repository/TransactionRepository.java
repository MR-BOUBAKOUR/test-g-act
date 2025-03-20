package com.payMyBuddy.repository;

import com.payMyBuddy.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Transaction repository.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    /**
     * Find by the sender account user id or receiver account user id and return a list of transaction.
     *
     * @param currentUserId  the current user id
     * @param currentUserId1 the current user id 1
     * @return the list
     */
    List<Transaction> findBySenderAccount_User_IdOrReceiverAccount_User_Id(Integer currentUserId, Integer currentUserId1);
}