// Copyright 2005, 2008, 2015, 2020, 2023, 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.apache.logging.log4j.*;
import org.deltava.util.dns.Resolver;

/**
 * A servlet filter to wrap HTTP servlet requests with a custom wrapper. This filter will also extract cookies into servlet request attributes.
 * @author Luke
 * @version 12.4
 * @since 1.0
 */

public class RequestWrapperFilter extends HttpFilter {
    
    private static final Logger log = LogManager.getLogger(RequestWrapperFilter.class);

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
    	
		// Get remote host name
    	String remoteHost = req.getRemoteHost();
    	if (req.getRemoteAddr().equals(remoteHost)) {
    		String hostHdr = req.getHeader("X-Forwarded-Host");	
    		if ((hostHdr == null) || hostHdr.equals(req.getRemoteAddr())) {
    			String hostName = Resolver.resolve(req.getRemoteAddr(), 250);
    			if (!hostName.equals(req.getRemoteAddr()))
    				remoteHost = hostName;
    		}
    	}

       	// Get cookies
       	Cookie[] cookies = req.getCookies();
       	if (cookies != null) {
       		for (int x = 0; x < cookies.length; x++) {
       			Cookie c = cookies[x];
       			req.setAttribute("COOKIE$" + c.getName(), c);
       		}
       	}
        	
       	// Roll the request wrapper
       	fc.doFilter(new CustomRequestWrapper(req, remoteHost), rsp);
    }

    @Override
    public void destroy() {
        log.info("Stopped");
    }
}