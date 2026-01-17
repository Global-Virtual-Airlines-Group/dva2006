// Copyright 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import static org.deltava.commands.HTTPContext.CSP_ATTR_NAME;

import java.io.IOException;
import java.util.HexFormat;
import java.security.SecureRandom;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.system.*;

/**
 * A servlet filter to add Content Security Policy data to the request and response.
 * @author Luke
 * @version 12.4
 * @since 12.0
 */

public class ContentSecurityFilter extends HttpFilter {

	private static final Logger log = LogManager.getLogger(ContentSecurityFilter.class);
	
	private final SecureRandom _rnd = new SecureRandom();
	private final HexFormat _fmt = HexFormat.of();
	
	private boolean _enforce;
	private String _reportURI;
	
	@Override
	public void init(FilterConfig cfg) throws ServletException {
		log.info("Started");
		_enforce = Boolean.valueOf(cfg.getInitParameter("enforce")).booleanValue();
		_reportURI = cfg.getInitParameter("reportURI");
	}
	
	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain fc) throws IOException, ServletException {
		
		// Generate the CSP nonce
		byte[] nonce = new byte[8];
		_rnd.nextBytes(nonce);
		
		// Add a CSP bean to the request that downstream can play with
		ContentSecurityPolicy csp = new ContentSecurityPolicy(_enforce, _fmt.formatHex(nonce));
		csp.add(ContentSecurity.SCRIPT, "www.googletagmanager.com");
		csp.add(ContentSecurity.CONNECT, "*.google-analytics.com");
		
		// Calculate the reporting URI
		if (_reportURI != null) {
			if (!_reportURI.startsWith("http")) {
				StringBuilder buf = new StringBuilder(req.getScheme());
				buf.append("://").append(req.getServerName());
				buf.append(_reportURI);
				csp.setReportURI("default-report", buf.toString());
			} else
				csp.setReportURI("default-report", _reportURI);
		}
		
		// Save and Pass upstream
		req.setAttribute(CSP_ATTR_NAME, csp);
		fc.doFilter(req, rsp);
	}
}