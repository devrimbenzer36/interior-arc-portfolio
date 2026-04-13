package com.portfolio.security.filter;

import com.portfolio.security.service.UserDetailsServiceImpl;
import com.portfolio.security.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Her HTTP isteğinde bir kez çalışır (OncePerRequestFilter).
 *
 * Akış:
 *  1. Authorization header'dan Bearer token al
 *  2. Token'ı parse et, email'i çıkart
 *  3. SecurityContext boşsa UserDetails yükle
 *  4. Token geçerliyse Authentication nesnesini context'e set et
 *
 * Geçersiz token durumunda filtre sessizce geçer (exception fırlatmaz),
 * SecurityContext boş kalır → Spring Security 401 döner.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String email = jwtUtil.extractEmail(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtUtil.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (UsernameNotFoundException e) {
                // Token geçerli ama kullanıcı silinmiş — 401, 500 değil
                log.debug("JWT token refers to non-existent user: {}", e.getMessage());
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("Invalid JWT token: {}", e.getMessage());
            }
            // Her iki durumda da context boş kalır → Spring Security 401 döner
        }

        filterChain.doFilter(request, response);
    }

    /**
     * "Authorization: Bearer <token>" header'ından token'ı çıkartır.
     * Header yoksa veya format yanlışsa null döner.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}