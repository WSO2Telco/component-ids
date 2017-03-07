package com.wso2telco.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * This filter forwards the requests coming for /wt request path to
 * /tnspoints/endpoint/sms/response. The purpose of this is, to make the link
 * sent to a user in SMS authenticator needs to be short as possible
 */
public class SMSApprovalRequestForwardingFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
        // Doesn't need implementation
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/tnspoints/endpoint/sms/response");
        requestDispatcher.forward(servletRequest, servletResponse);
    }

    public void destroy() {
        // Doesn't need implementation
    }
}
