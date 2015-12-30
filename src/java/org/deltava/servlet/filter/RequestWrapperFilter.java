// Copyright 2005, 2008, 2015 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * A servlet filter to wrap HTTP servlet requests with a custom wrapper. This filter will
 * also extract cookies into servlet request attributes.
 * @author Luke
 * @version 6.3
 * @since 1.0
 */

public class RequestWrapperFilter implements Filter {
    
    private static final Logger log = Logger.getLogger(RequestWrapperFilter.class);

    
    /**
     * Called by the servlet container when the filter is started. Logs a message.
     * @param cfg the Filter Configuration
     */
    @Override
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
    @Override
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain fc) throws IOException, ServletException {

       	HttpServletRequest hreq = (HttpServletRequest) req;
        	
       	// Get cookies
       	Cookie[] cookies = hreq.getCookies();
       	if (cookies != null) {
       		for (int x = 0; x < cookies.length; x++) {
       			Cookie c = cookies[x];
       			hreq.setAttribute("COOKIE$" + c.getName(), c);
       		}
       	}
        	
       	// Save HTTP version
       	String p = hreq.getProtocol();
       	hreq.setAttribute("HTTP$Version", p.substring(p.lastIndexOf('/') + 1));
        	
       	// Roll the request wrapper
       	ServletRequest newReq = new CustomRequestWrapper(hreq);
       	fc.doFilter(newReq, rsp);
    }

    /**
     * Called by the servlet container when the filter is stopped. Logs a message. 
     */
    @Override
    public void destroy() {
        log.info("Stopped");
    }
}