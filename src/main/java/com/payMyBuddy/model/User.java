package com.payMyBuddy.model;

import com.payMyBuddy.exception.AddContactException;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", schema = "pay_my_buddy")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private Set<Account> accounts = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_contacts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    private Set<User> contacts = new HashSet<>();

    public void addContact(User contact) {
        if (this.equals(contact)) {
            throw new AddContactException("Un utilisateur ne peut pas s'ajouter lui-même.");
        }

        if (!contacts.contains(contact)) {
            contacts.add(contact);
            contact.getContacts().add(this);
        } else {
            throw new AddContactException("Ce contact est déjà dans votre liste.");
        }
    }

    public void removeContact(User contact) {
        if (contacts.contains(contact)) {
            contacts.remove(contact);
            contact.getContacts().remove(this);
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", accounts=" + (accounts != null ? accounts.size() : "NULL") +
                ", contacts=" + (contacts != null ? contacts.size() : "NULL") +
                '}';
    }

}
