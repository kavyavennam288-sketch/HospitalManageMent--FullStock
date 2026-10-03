package com.medsync.config;

import com.medsync.security.CustomUserDetailsService;
import com.medsync.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
// @EnableMethodSecurity lets us use @PreAuthorize("hasRole('ADMIN')") on
// individual controller methods — fine-grained, method-level access control.
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    // ── Password Encoder ─────────────────────────────────────────────
    // BCrypt is a one-way hash with a built-in salt and work factor.
    // NEVER store plain-text passwords. BCrypt makes brute-force attacks slow.
    // This bean is used in AuthService to hash passwords on register,
    // and by DaoAuthenticationProvider to verify on login.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ── Authentication Provider ───────────────────────────────────────
    // Tells Spring: "to authenticate a user, load them from our DB
    // using CustomUserDetailsService and verify password with BCrypt."
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ── Authentication Manager ────────────────────────────────────────
    // The AuthenticationManager is what actually performs login.
    // We expose it as a bean so AuthService can call
    // authenticationManager.authenticate(email, password) directly.
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ── Security Filter Chain ─────────────────────────────────────────
    // This is the heart of Spring Security configuration.
    // Defines the rules for every HTTP request.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless JWT APIs.
            // CSRF protects session-cookie-based apps; we use tokens instead.
           .csrf(AbstractHttpConfigurer::disable)
.headers(headers -> headers.frameOptions(frame -> frame.disable()))

            // Configure CORS to allow our React frontend (port 3000)
            // to call our API (port 8080) without being blocked by the browser.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // STATELESS = Spring will never create an HttpSession.
            // Every request must carry its own JWT. No server-side sessions.
            // This makes the API horizontally scalable (any server handles any request).
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ── Authorization Rules ───────────────────────────────────
            // Rules are checked TOP TO BOTTOM — first match wins.
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/swagger-ui/**").permitAll()
    .requestMatchers("/v3/api-docs/**").permitAll()
    .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                // Public endpoints — no token needed
                .requestMatchers("/api/auth/**").permitAll()

                // Doctors can be searched publicly (patients browse before booking)
                .requestMatchers(HttpMethod.GET, "/api/doctors/**").permitAll()

                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/resources/**").hasRole("ADMIN")
                .requestMatchers("/api/patients/**").hasRole("ADMIN")      // ← ADD
                .requestMatchers("/api/dashboard/**").hasRole("ADMIN")     // ← ADD

                // Doctor-only endpoints
                .requestMatchers("/api/doctors/schedule/**").hasRole("DOCTOR")

                // All other endpoints require any valid JWT token
                // (fine-grained role checks happen at the method level with @PreAuthorize)
                .anyRequest().authenticated()
            )

            // Register our custom JWT filter BEFORE Spring's default
            // UsernamePasswordAuthenticationFilter. This ensures JWT is
            // validated before any other authentication attempt.
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            )

            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    // ── CORS Configuration ────────────────────────────────────────────
    // Without this, browsers block cross-origin requests from React (3000)
    // to Spring Boot (8080) — even on localhost.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // In production, replace with your actual frontend domain
        config.setAllowedOrigins(List.of("http://localhost:5173"));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow Authorization header so the browser can send our JWT
        config.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, auth headers) in cross-origin requests
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this CORS config to every URL in our API
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}