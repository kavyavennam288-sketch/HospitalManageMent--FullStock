// UserRepository.java
package com.medsync.repository;

import com.medsync.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<User, Long> → entity type + primary key type
// Spring generates: findAll(), findById(), save(), deleteById(), count(), etc.
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring parses method names into SQL:
    // findBy + Email → SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}