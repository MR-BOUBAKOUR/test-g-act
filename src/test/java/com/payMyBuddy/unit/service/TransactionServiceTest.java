package com.payMyBuddy.unit.service;

import com.payMyBuddy.dto.transaction.TransactionCreateDTO;
import com.payMyBuddy.dto.transaction.TransactionResponseDTO;
import com.payMyBuddy.exception.InsufficientBalanceException;
import com.payMyBuddy.exception.SelfSendingAmountException;
import com.payMyBuddy.mapper.TransactionMapper;
import com.payMyBuddy.model.Account;
import com.payMyBuddy.model.Transaction;
import com.payMyBuddy.model.User;
import com.payMyBuddy.repository.TransactionRepository;
import com.payMyBuddy.service.AccountService;
import com.payMyBuddy.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionService transactionService;


    @Test
    void findTransactionsForCurrentUser_whenValidData_shouldReturnTransactions() {

        // Given
        Integer currentUserId = 1;
        int limit = 5;

        Transaction transaction1 = new Transaction();
        transaction1.setCreatedAt(Instant.now().minusSeconds(60));
        Transaction transaction2 = new Transaction();
        transaction2.setCreatedAt(Instant.now());

        List<Transaction> transactions = List.of(transaction1, transaction2);

        TransactionResponseDTO transactionResponseDTO1 = new TransactionResponseDTO();
        transactionResponseDTO1.setCreatedAt(transaction1.getCreatedAt());
        TransactionResponseDTO transactionResponseDTO2 = new TransactionResponseDTO();
        transactionResponseDTO2.setCreatedAt(transaction2.getCreatedAt());

        when(transactionRepository.findBySenderAccount_User_IdOrReceiverAccount_User_Id(currentUserId, currentUserId))
                .thenReturn(transactions);
        when(transactionMapper.toResponseDTO(transaction1)).thenReturn(transactionResponseDTO1);
        when(transactionMapper.toResponseDTO(transaction2)).thenReturn(transactionResponseDTO2);

        // When
        List<TransactionResponseDTO> result = transactionService.findTransactionsForCurrentUser(currentUserId, limit);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreatedAt().isAfter(result.get(1).getCreatedAt()));

        verify(transactionRepository, times(1))
                .findBySenderAccount_User_IdOrReceiverAccount_User_Id(currentUserId, currentUserId);
    }

    @Test
    void createTransaction_whenValidData_shouldCreateTransaction() {

        // Given
        TransactionCreateDTO transactionCreateDTO = new TransactionCreateDTO();
        transactionCreateDTO.setSenderAccountId(1);
        transactionCreateDTO.setReceiverAccountId(2);
        transactionCreateDTO.setAmount(BigDecimal.valueOf(50));

        User senderUser = new User();
        senderUser.setId(1);

        User receiverUser = new User();
        receiverUser.setId(2);

        Account senderAccount = new Account();
        senderAccount.setId(1);
        senderAccount.setUser(senderUser);
        senderAccount.setBalance(BigDecimal.valueOf(200));

        Account receiverAccount = new Account();
        receiverAccount.setId(2);
        receiverAccount.setUser(receiverUser);
        receiverAccount.setBalance(BigDecimal.valueOf(100));

        Transaction transaction = new Transaction();
        transaction.setAmount(transactionCreateDTO.getAmount());

        when(accountService.findAccountByIdInternalUse(1)).thenReturn(senderAccount);
        when(accountService.findAccountByIdInternalUse(2)).thenReturn(receiverAccount);
        when(transactionMapper.toEntityFromCreateDTO(transactionCreateDTO)).thenReturn(transaction);

        // When
        transactionService.createTransaction(transactionCreateDTO);

        // Then
        assertEquals(BigDecimal.valueOf(150), senderAccount.getBalance());
        assertEquals(BigDecimal.valueOf(150), receiverAccount.getBalance());

        verify(transactionRepository, times(1)).save(transaction);
        verify(accountService, times(1)).saveAccount(senderAccount);
        verify(accountService, times(1)).saveAccount(receiverAccount);
    }

    @Test
    void createTransaction_whenSenderIsReceiver_shouldThrowSelfSendingAmountException() {

        // Given
        TransactionCreateDTO transactionCreateDTO = new TransactionCreateDTO();
        transactionCreateDTO.setSenderAccountId(1);
        transactionCreateDTO.setReceiverAccountId(1);
        transactionCreateDTO.setAmount(BigDecimal.valueOf(50));

        User senderUser = new User();
        senderUser.setId(1);

        Account senderAccount = new Account();
        senderAccount.setId(1);
        senderAccount.setUser(senderUser);
        senderAccount.setBalance(BigDecimal.valueOf(200));

        Transaction transaction = new Transaction();
        transaction.setAmount(transactionCreateDTO.getAmount());

        when(accountService.findAccountByIdInternalUse(1)).thenReturn(senderAccount);

        // When
        SelfSendingAmountException exception = assertThrows(
                SelfSendingAmountException.class,
                () -> transactionService.createTransaction(transactionCreateDTO)
        );

        // Then
        assertEquals("Virement interdit sur le même compte.", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransaction_whenInsufficientBalance_shouldThrowInsufficientBalanceException() {

        // Given
        TransactionCreateDTO transactionCreateDTO = new TransactionCreateDTO();
        transactionCreateDTO.setSenderAccountId(1);
        transactionCreateDTO.setReceiverAccountId(2);
        transactionCreateDTO.setAmount(BigDecimal.valueOf(200));

        Account senderAccount = new Account();
        senderAccount.setId(1);
        senderAccount.setBalance(BigDecimal.valueOf(100));

        Account receiverAccount = new Account();
        receiverAccount.setId(2);
        receiverAccount.setBalance(BigDecimal.valueOf(50));

        Transaction transaction = new Transaction();
        transaction.setAmount(transactionCreateDTO.getAmount());

        when(accountService.findAccountByIdInternalUse(1)).thenReturn(senderAccount);
        when(accountService.findAccountByIdInternalUse(2)).thenReturn(receiverAccount);
        when(transactionMapper.toEntityFromCreateDTO(transactionCreateDTO)).thenReturn(transaction);

        // When
        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.createTransaction(transactionCreateDTO)
        );

        // Then
        assertEquals("Solde insuffisant. Veuillez alimenter votre compte.", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }



}