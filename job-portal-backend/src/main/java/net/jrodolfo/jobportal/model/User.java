package net.jrodolfo.jobportal.model;

import net.jrodolfo.jobportal.constant.AuthProvider;
import net.jrodolfo.jobportal.constant.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users") // Avoid using "user" as it's a reserved keyword in MySQL
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Needed for Google users

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true) // OAuth users won't have passwords
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /* Constructor for Normal Users (Requires Password) */
    public User(String name, String email, String password, AuthProvider provider, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.authProvider = provider;
        this.role = role;
    }

    /* Constructor for Google Users (No Password) */
    public User(String name, String email, AuthProvider provider, Role role) {
        this.name = name;
        this.email = email;
        this.password = null; // Google Users Don't Have Passwords
        this.authProvider = provider;
        this.role = role;
    }
}
