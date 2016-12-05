// Copyright 2005, 2006, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.net.*;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.system.*;
import org.deltava.commands.HTTPContext;

import org.deltava.util.StringUtils;

/**
 * A servlet filter to detect the browser type.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class BrowserTypeFilter implements Filter {

	private static final Logger log = Logger.getLogger(BrowserTypeFilter.class);

	private String _defaultCode;

	/**
	 * Called by the servlet container when the filter is started. Logs a message.
	 * @param cfg the Filter Configuration
	 */
	@Override
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
	@Override
	public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain fc) throws IOException, ServletException {

		// Get the user agent
		HttpServletRequest hreq = (HttpServletRequest) req;
		String userAgent = hreq.getHeader("User-Agent");
		if ((userAgent == null) && (_defaultCode != null))
			userAgent = _defaultCode;

		// Create the Context data object
		BrowserType.BrowserVersion ver = BrowserType.detect(userAgent);
		BrowserType bt = ver.getType();
		DeviceType dev = DeviceType.detect(userAgent);
		HTTPContextData ctxt = new HTTPContextData(OperatingSystem.detect(userAgent), ver.getType(), dev);
		int pos = ver.getVersion().indexOf('.');
		ctxt.setVersion(StringUtils.parse(ver.getVersion().substring(0, pos), 0), StringUtils.parse(ver.getVersion().substring(pos + 1), 0));
		ctxt.setHTML5((bt == BrowserType.CHROME) && (ctxt.getMajor() >= 20));

		// Check for native JSON
		ctxt.setJSON((bt == BrowserType.CHROME) || (bt == BrowserType.FIREFOX) || (bt == BrowserType.OPERA) || (bt == BrowserType.WEBKIT));

		// Check for IPv6
		InetAddress addr = InetAddress.getByName(hreq.getRemoteAddr());
		ctxt.setIPv6((addr instanceof Inet6Address));
		
		// Check for HTTP/2
		if (req.isSecure()) {
			String proto = req.getProtocol().substring(req.getProtocol().lastIndexOf('/') + 1);
			ctxt.setHTTP2((proto.length() > 0) && (proto.charAt(0) == '2'));
		}

		// If we're using IE, set the compatability header
		if (ver.getType() == BrowserType.IE) {
			HttpServletResponse hrsp = (HttpServletResponse) rsp;
			hrsp.setHeader("X-UA-Compatible", "IE=11, IE=edge");
			ctxt.setJSON(ctxt.getMajor() > 7);
			ctxt.setHTML5(ctxt.getMajor() > 10);
		}

		// Execute the next filter in the chain
		req.setAttribute(HTTPContext.HTTPCTXT_ATTR_NAME, ctxt);
		fc.doFilter(req, rsp);
	}

	/**
	 * Called by the servlet container when the filter is stopped. Logs a message.
	 */
	@Override
	public void destroy() {
		log.info("Stopped");
	}
}