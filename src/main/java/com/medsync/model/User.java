package com.medsync.model;

import com.medsync.model.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")

// Inheritance strategy: each subclass (Doctor, Patient) gets its own table,
// but they share the 'users' table for the common fields below.
// JOINED = most normalized option (no wasted nullable columns).
@Inheritance(strategy = InheritanceType.JOINED)

// Lombok generates all getters, setters, constructors, and builder pattern
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User implements UserDetails {
    // ← implements UserDetails so Spring Security can use this class directly
    //   for authentication (no adapter needed)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY = Postgres auto-increment (SERIAL column)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    // unique = true → Postgres adds a UNIQUE constraint; prevents duplicate emails
    private String email;

    @Column(nullable = false)
    private String password;   // stored as bcrypt hash, NEVER plain text

    @Enumerated(EnumType.STRING)
    // STRING → stores "DOCTOR" in the DB, not an integer.
    // Easier to read in the DB and survives enum reordering.
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;
    // isActive = false means "soft deleted" — the row stays in the DB
    // but we filter it out in queries. Used for patient record deactivation.

    @Column(updatable = false)
    // updatable = false → JPA never includes this in UPDATE statements
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    // @PrePersist runs just before INSERT — sets createdAt automatically
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    // @PreUpdate runs just before UPDATE — keeps updatedAt current
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── UserDetails interface methods ────────────────────────────────
    // Spring Security calls these during authentication.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Converts our Role enum into Spring's authority format.
        // "ROLE_DOCTOR" prefix is Spring Security convention.
        return List.of(new SimpleGrantedAuthority("ROLE_" +  role.name()));
    }

    @Override
    public String getUsername() {
        return email;   // we use email as the username
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isActive; }
    // isEnabled() returning false causes Spring Security to reject login
    // for soft-deleted / deactivated users automatically.
}