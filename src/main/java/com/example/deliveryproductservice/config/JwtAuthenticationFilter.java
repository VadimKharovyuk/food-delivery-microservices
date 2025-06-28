package com.example.deliveryproductservice.Utility;

import com.example.deliveryproductservice.config.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements Filter {

    private final JwtUtil jwtUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = extractTokenFromRequest(httpRequest);

        if (token != null) {
            log.info("🔍 JWT token found for: {}", httpRequest.getRequestURI());

            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String email = jwtUtil.getEmailFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                log.info("✅ JWT validated - UserId: {}, Email: {}, Role: {}", userId, email, role);

                HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(httpRequest) {
                    @Override
                    public String getHeader(String name) {
                        switch (name) {
                            case "X-User-Id":
                                return userId != null ? userId.toString() : null;
                            case "X-User-Email":
                                return email;
                            case "X-User-Role":
                                return role;
                            default:
                                return super.getHeader(name);
                        }
                    }
                };

                chain.doFilter(requestWrapper, response);
            } else {
                log.warn("❌ Invalid JWT token");
                chain.doFilter(request, response);
            }
        } else {
            log.debug("No JWT token found for: {}", httpRequest.getRequestURI());
            chain.doFilter(request, response);
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}