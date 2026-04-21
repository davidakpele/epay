package com.pesco.wallet_service.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.pesco.wallet_service.bootstrap.UsersDetailsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesco.wallet_service.dtos.UserDTO;
import com.pesco.wallet_service.exceptions.JwtAuthenticationException;
import com.pesco.wallet_service.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AdminDetailsService adminDetailsService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService,
                                   AdminDetailsService adminDetailsService,
                                   ObjectMapper objectMapper) {
        this.jwtService          = jwtService;
        this.userDetailsService  = userDetailsService;
        this.adminDetailsService = adminDetailsService;
        this.objectMapper        = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt      = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                List<String> roles = jwtService.extractRoles(jwt);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                boolean isAdmin = roles.stream()
                        .anyMatch(r -> r.equalsIgnoreCase("ADMIN")
                                    || r.equalsIgnoreCase("SUPER_ADMIN")
                                    || r.equalsIgnoreCase("ROLE_ADMIN")
                                    || r.equalsIgnoreCase("ROLE_SUPER_ADMIN"));

                UserDetails userDetails = isAdmin
                        ? adminDetailsService.loadUserByUsername(username)
                        : userDetailsService.loadUserByUsername(username);
                
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    UserDTO userDTO = new UserDTO();

                    if (userDetails instanceof UsersDetailsDTO details) {
                        userDTO.setId(details.getId());
                        userDTO.setEmail(details.getEmail());
                        userDTO.setUsername(details.getUsername());
                        userDTO.setEnabled(details.isEnabled());
                    } else if (userDetails instanceof UserDTO dto) {
                        userDTO = dto;
                    } else {
                        userDTO.setUsername(username);
                    }

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDTO, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                } else {
                    handleAuthenticationError(response, "Invalid or expired token");
                    return;
                }
            }

            filterChain.doFilter(request, response);

        } catch (UsernameNotFoundException e) {
            handleAuthenticationError(response, "User account not found");
        } catch (JwtAuthenticationException e) {
            handleAuthenticationError(response, e.getMessage());
        } catch (ServletException | IOException e) {
            throw e;
        } catch (Exception e) {
            handleAuthenticationError(response, "Authentication failed");
        }
    }

    private void handleAuthenticationError(HttpServletResponse response,
                                           String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("path",      "JWT Authentication Filter");
        errorDetails.put("error",     "Authentication Failed");
        errorDetails.put("message",   message);
        errorDetails.put("timestamp", System.currentTimeMillis());
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/ws/");
    }
}