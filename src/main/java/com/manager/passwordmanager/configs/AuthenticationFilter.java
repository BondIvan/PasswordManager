package com.manager.passwordmanager.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String requestURI = request.getRequestURI();

        if("/notes".equals(requestURI) && (session == null || session.getAttribute("authenticated") == null)) {
            response.sendRedirect("/master-password");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
