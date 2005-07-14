package org.deltava.servlet.filter;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * A servlet filter to wrap HTTP servlet requests with a custom wrapper.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class RequestWrapperFilter implements Filter {
    
    private static final Logger log = Logger.getLogger(RequestWrapperFilter.class);

    /**
     * Called by the servlet container when the filter is started. Logs a message.
     * @param cfg the Filter Configuration
     */
    public void init(FilterConfig cfg) throws ServletException {
        log.info("Started");
    }

    /**
     * Called by the servlet container on each request. Wraps the request with a custom wrapper.
     * @param req the Servlet Request
     * @param rsp the Servlet Response
     * @param fc the Filter Chain
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a general error occurs
     */
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain fc) throws IOException, ServletException {
        // Invoke the next filter in the chain
        if (req instanceof CustomRequestWrapper) {
            fc.doFilter(req, rsp);
        } else {
            ServletRequest newReq = new CustomRequestWrapper((HttpServletRequest) req);
            fc.doFilter(newReq, rsp);
        }
    }

    /**
     * Called by the servlet container when the filter is stopped. Logs a message. 
     */
    public void destroy() {
        log.info("Stopped");
    }
}