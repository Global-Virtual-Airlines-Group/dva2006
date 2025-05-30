// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import static org.deltava.commands.HTTPContext.CSP_ATTR_NAME;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.logging.log4j.*;
import org.deltava.beans.system.ContentSecurity;
import org.deltava.beans.system.ContentSecurityPolicy;

/**
 * A servlet filter to add Content Security Policy data to the request and response.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class ContentSecurityFilter extends HttpFilter {

	private static final Logger log = LogManager.getLogger(ContentSecurityFilter.class);
	
	private boolean _enforce;
	
	@Override
	public void init(FilterConfig cfg) throws ServletException {
		log.info("Started");
		_enforce = Boolean.valueOf(cfg.getInitParameter("enforce")).booleanValue();
	}
	
	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain fc) throws IOException, ServletException {
		
		// Add a CSP bean to the request that downstream can play with
		ContentSecurityPolicy csp = new ContentSecurityPolicy(_enforce);
		csp.add(ContentSecurity.SCRIPT, "www.googletagmanager.com");
		csp.add(ContentSecurity.CONNECT, "www.google-analytics.com");
		req.setAttribute(CSP_ATTR_NAME, csp);

		// Pass upstream
		fc.doFilter(req, rsp);
	}
}