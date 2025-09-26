package com.women.safety.features.authentication.filter;

import com.women.safety.features.authentication.security.CustomUserDetailService;
import com.women.safety.features.authentication.security.CustomUserDetails;
import com.women.safety.features.authentication.utils.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailService userDetailService;

    JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailService userDetailService){
        this.jwtService = jwtService;
        this.userDetailService = userDetailService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try{
            String authHeader = request.getHeader("Authorization");

            // Skip if no header or not Bearer
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                throw new ServletException("Token missing");
               filterChain.doFilter(request, response);
               return;
            }

            String token = authHeader.substring(7);

            if (jwtService.isTokenExpired(token)) {
                throw new ServletException("Invalid token");
            }

            String email = jwtService.extractEmailFromToken(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                CustomUserDetails userDetails = (CustomUserDetails) userDetailService.loadUserByUsername(email);

                if (jwtService.validateToken(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        }catch (Exception e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\" : \"Invalid authentication, or token is missing.\"}");
        }
    }

}
