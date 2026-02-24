package pesco.example.virtual_card_service.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import pesco.example.virtual_card_service.exceptions.JwtAuthenticationException;
import pesco.example.virtual_card_service.services.JwtService;
import java.util.Map;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    handleAuthenticationError(response, "Invalid or expired token");
                    return;
                }
            }
            
            filterChain.doFilter(request, response);
            
        } catch (JwtAuthenticationException e) {
            handleAuthenticationError(response, e.getMessage());
        } catch (ServletException | IOException | UsernameNotFoundException e) {
            handleAuthenticationError(response, "Authentication failed");
        }
    }

    private void handleAuthenticationError(
            HttpServletResponse response,
            String message
    ) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", "Authentication Failed");
        errorDetails.put("message", message);
        errorDetails.put("timestamp", System.currentTimeMillis());
        errorDetails.put("path", "JWT Authentication Filter");
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/ws/");
    }
}