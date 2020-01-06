// Copyright 2005, 2008, 2015, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

/**
 * A servlet filter to wrap HTTP servlet requests with a custom wrapper. This filter will
 * also extract cookies into servlet request attributes.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class RequestWrapperFilter extends HttpFilter {
    
    private static final Logger log = Logger.getLogger(RequestWrapperFilter.class);

    @Override
    public void init(FilterConfig cfg) throws ServletException {
        log.info("Started");
    }

    /**
     * Called by the servlet container on each request. Wraps the request with a custom wrapper.
     * @param req the request
     * @param rsp the response
     * @param fc the Filter Chain
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a general error occurs
     */
    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain fc) throws IOException, ServletException {

       	// Get cookies
       	Cookie[] cookies = req.getCookies();
       	if (cookies != null) {
       		for (int x = 0; x < cookies.length; x++) {
       			Cookie c = cookies[x];
       			req.setAttribute("COOKIE$" + c.getName(), c);
       		}
       	}
        	
       	// Save HTTP version
       	String p = req.getProtocol();
       	req.setAttribute("HTTP$Version", p.substring(p.lastIndexOf('/') + 1));
        	
       	// Roll the request wrapper
       	fc.doFilter(new CustomRequestWrapper(req), rsp);
    }

    @Override
    public void destroy() {
        log.info("Stopped");
    }
}