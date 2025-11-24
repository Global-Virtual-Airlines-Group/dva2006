package org.gvagroup.mockservlet;

import jakarta.servlet.*;

public class MockFilterChain implements FilterChain {

    private boolean invoked = false;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) {
        invoked = true;
    }

    public boolean wasInvoked() { return invoked; }
}
