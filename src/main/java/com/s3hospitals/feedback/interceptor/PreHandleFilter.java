package com.s3hospitals.feedback.interceptor;

import jakarta.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class PreHandleFilter  implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(PreHandleFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (httpRequest.getRequestURI().equals("/actuator/health")) {
            logger.info("Health endpoint called from IP: {}", httpRequest.getRemoteAddr());
        }
        chain.doFilter(request, response);
    }
}
