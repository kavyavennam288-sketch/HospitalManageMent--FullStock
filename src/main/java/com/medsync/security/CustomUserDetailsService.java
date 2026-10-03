package com.medsync.security;

import com.medsync.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// @RequiredArgsConstructor (Lombok) generates a constructor for all
// 'final' fields. Spring sees one constructor → uses it for injection.
// This is constructor injection — preferred over @Autowired field injection
// because it makes dependencies explicit and testable.
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Spring Security calls this method internally during authentication.
    // "username" here is actually the email — that's our identifier.
    // It must return a UserDetails object — our User class implements it,
    // so we can return our User directly (no conversion needed).
    @Override
    @Transactional(readOnly = true)
    // readOnly = true → Hibernate skips dirty-checking (faster, no accidental writes)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        return userRepository.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "User not found with email: " + email
                )
            );
        // If the user doesn't exist, Spring Security catches this exception
        // and converts it to a 401 Unauthorized response automatically.
    }
}