package com.pswied.loan.awscorepayment.web;

import com.pswied.loan.awscorepayment.security.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Simple API key filter. When security.api-key is empty, the filter allows all requests.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    public ApiKeyFilter(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String expected = securityProperties.getApiKey();
        if (expected == null || expected.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String provided = request.getHeader("X-API-KEY");
        if (expected.equals(provided)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"unauthorized\"}");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Apply only to /api/ paths
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/");
    }
}
