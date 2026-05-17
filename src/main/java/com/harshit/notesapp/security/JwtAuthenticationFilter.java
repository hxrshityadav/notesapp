package com.harshit.notesapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Check if Authorization header exists and is a Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Pass down the chain without authenticating
            return;
        }

        // 2. Extract JWT token (skip the "Bearer " prefix which is 7 characters long)
        jwt = authHeader.substring(7);
        
        // 3. Extract the username (email) from the token
        userEmail = jwtService.extractUsername(jwt);

        // 4. If we have a username and the user is not yet authenticated in the SecurityContext
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Fetch user details from database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Validate the token against the user details
            if (jwtService.isTokenValid(jwt, userDetails)) {
                
                // Create an Authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Credentials (password) not needed here since JWT is already verified
                        userDetails.getAuthorities()
                );
                
                // Attach details (like IP address, session ID) to the authentication token
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // 5. Update the SecurityContextHolder to officially authenticate the request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // 6. Continue executing the rest of the filter chain
        filterChain.doFilter(request, response);
    }
}
