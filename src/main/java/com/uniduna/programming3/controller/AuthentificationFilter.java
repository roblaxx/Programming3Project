package com.uniduna.programming3.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class AuthentificationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String uri = req.getRequestURI();

        // Ignore the OPTIONS requests from the CORS
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Check the sessions for all roots
        if (uri.equals("/") || uri.equals("/loginAdmin") || uri.startsWith("/css") || uri.startsWith("/js")) {
            chain.doFilter(request, response);
            return;
        }

        // Check that the session is only for Users with admin rights
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            res.sendRedirect("/");
            return;
        }

        chain.doFilter(request, response);
    }
}