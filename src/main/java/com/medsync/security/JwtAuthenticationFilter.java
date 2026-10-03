package com.medsync.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
// OncePerRequestFilter guarantees this filter runs exactly once per request,
// even if the request is forwarded internally within the app.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)        // filterChain = the next filter or the controller
            throws ServletException, IOException {

        try {
            // Step 1: Extract JWT from the Authorization header
            String jwt = extractTokenFromRequest(request);

            // Step 2: Validate the token
            // Also check that SecurityContext is empty — if it already has
            // an authentication, another filter already handled this request.
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                // Step 3: Get the email from the token
                String email = jwtTokenProvider.getEmailFromToken(jwt);

                // Step 4: Load the full User object from the database
                // (we need their authorities/roles for authorization checks)
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Step 5: Create an Authentication object
                // UsernamePasswordAuthenticationToken(principal, credentials, authorities)
                // credentials = null because we don't need the password at this point —
                // the JWT itself proved their identity.
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()  // ["ROLE_DOCTOR"], ["ROLE_PATIENT"], etc.
                    );

                // Attach request metadata (IP address, session info) to the auth object
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Step 6: Store authentication in SecurityContext
                // This is the key step — once this is set, Spring Security
                // considers the user "logged in" for this request.
                // @PreAuthorize and hasRole() checks all read from here.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            // Don't rethrow — just log and let the request continue.
            // If the token was invalid, SecurityContext stays empty,
            // and SecurityConfig will reject the request with 401.
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        // Step 7: Always pass the request to the next filter / controller.
        // Even unauthenticated requests get forwarded — SecurityConfig
        // decides what to do with them (permit or reject).
        filterChain.doFilter(request, response);
    }

    // Extracts the raw token string from the Authorization header.
    // Convention: header looks like "Authorization: Bearer eyJhbGci..."
    // We strip the "Bearer " prefix to get just the token.
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " is 7 characters
        }
        return null;
    }
}