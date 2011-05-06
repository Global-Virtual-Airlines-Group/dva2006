// Copyright 2005, 2006, 2008, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import org.deltava.beans.system.*;
import org.deltava.commands.HTTPContext;
import org.deltava.util.StringUtils;

/**
 * A servlet filter to detect the browser type.
 * @author Luke
 * @version 3.7
 * @since 1.0
 */

public class BrowserTypeFilter implements Filter {

	private static final Logger log = Logger.getLogger(BrowserTypeFilter.class);

	private String _defaultCode;
	
	/**
	 * Called by the servlet container when the filter is started. Logs a message.
	 * @param cfg the Filter Configuration
	 */
	public void init(FilterConfig cfg) throws ServletException {
		_defaultCode = cfg.getInitParameter("default");
		log.info("Started");
	}

	/**
	 * Called by the servlet container on each request. Saves the browser type in the request.
	 * @param req the Servlet Request
	 * @param rsp the Servlet Response
	 * @param fc the Filter Chain
	 * @throws IOException if an I/O error occurs
	 * @throws ServletException if a general error occurs
	 */
	public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain fc) throws IOException, ServletException {

		// Get the user agent
		HttpServletRequest hreq = (HttpServletRequest) req;
		String userAgent = hreq.getHeader("User-Agent");
		if ((userAgent == null) && (_defaultCode != null))
			userAgent = _defaultCode;
		
		// Create the Context data object
		BrowserType.BrowserVersion ver = BrowserType.detect(userAgent);
		HTTPContextData ctxt = new HTTPContextData(OperatingSystem.detect(userAgent), ver.getType());
		int pos = ver.getVersion().indexOf('.');
		ctxt.setVersion(StringUtils.parse(ver.getVersion().substring(0, pos), 0), StringUtils.parse(ver.getVersion().substring(pos + 1), 0));
		req.setAttribute(HTTPContext.HTTPCTXT_ATTR_NAME, ctxt);

		// Execute the next filter in the chain
		fc.doFilter(req, rsp);
	}

	/**
	 * Called by the servlet container when the filter is stopped. Logs a message.
	 */
	public void destroy() {
		log.info("Stopped");
	}
}