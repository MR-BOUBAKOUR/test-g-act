package com.payMyBuddy.model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts", schema = "pay_my_buddy")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "senderAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> sentTransactions = new HashSet<>();

    @OneToMany(mappedBy = "receiverAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> receivedTransactions = new HashSet<>();

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : "NULL") +
                ", balance=" + balance +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", sentTransactions=" + (sentTransactions != null ? sentTransactions.size() : "NULL") +
                ", receivedTransactions=" + (receivedTransactions != null ? receivedTransactions.size() : "NULL") +
                '}';
    }

}
