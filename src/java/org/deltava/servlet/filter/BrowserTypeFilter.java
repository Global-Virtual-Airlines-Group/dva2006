// Copyright 2005, 2006, 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * A servlet filter to detect the browser type.
 * @author Luke
 * @version 3.4
 * @since 1.0
 */

public class BrowserTypeFilter implements Filter {

	private static final Logger log = Logger.getLogger(BrowserTypeFilter.class);

	private enum Browser {
		UNKNOWN, MOZILLA, IE6, IE7, IE8, IE9, WEBKIT, OPERA, FF36
	}
	
	private enum OS {
		UNKNOWN, WINDOWS, OSX, LINUX, IOS
	}
	
	private String _defaultCode;
	private static final String[] MOZILLA_IDENT = { "Firefox", "Gecko" };
	private static final String[] MSIE9_IDENT = { "MSIE 9.0" };
	private static final String[] MSIE8_IDENT = { "MSIE 8.0" };
	private static final String[] MSIE7_IDENT = { "MSIE 7.0" };
	private static final String[] MSIE_IDENT = { "MSIE" };
	private static final String[] WEBKIT_IDENT = { "WebKit", "Chrome", "Safari" };
	private static final String[] OPERA_IDENT = { "Opera" };
	private static final String[] FF36_IDENT = { "Firefox/3.6" };

	private static final String[] LINUX_IDENT = { "Linux", "SunOS", "BSD" };

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

		// Set request attributes based on the browser type
		switch (getBrowserType(userAgent)) {
		case MOZILLA:
			req.setAttribute("browser$ff", Boolean.TRUE);
			req.setAttribute("browser$mozilla", Boolean.TRUE);
			break;
			
		case FF36:
			req.setAttribute("browser$ff36", Boolean.TRUE);
			req.setAttribute("browser$mozilla", Boolean.TRUE);
			break;
			
		case WEBKIT:
			req.setAttribute("browser$webkit", Boolean.TRUE);
			break;
			
		case IE9:
			req.setAttribute("browser$ie9", Boolean.TRUE);
			break;
			
		case IE8:
			req.setAttribute("browser$ie8", Boolean.TRUE);
			break;

		case IE7:
			req.setAttribute("browser$ie7", Boolean.TRUE);
			break;
			
		case OPERA:
			req.setAttribute("browser$opera", Boolean.TRUE);
			break;

		case IE6:
		default:
			req.setAttribute("browser$ie", Boolean.TRUE);
		}

		// Set request attributes based on the operating system
		switch (getOS(userAgent)) {
		case OSX:
			req.setAttribute("os$mac", Boolean.TRUE);
			break;
			
		case IOS:
			req.setAttribute("os$iOS", Boolean.TRUE);
			break;

		case LINUX:
			req.setAttribute("os$linux", Boolean.TRUE);
			break;

		case WINDOWS:
		default:
			req.setAttribute("os$windows", Boolean.TRUE);
		}

		// Execute the next filter in the chain
		fc.doFilter(req, rsp);
	}

	/**
	 * Called by the servlet container when the filter is stopped. Logs a message.
	 */
	public void destroy() {
		log.info("Stopped");
	}

	/**
	 * Helper method to search the ident strings and return the browser type.
	 */
	private Browser getBrowserType(String userAgent) {
		
		// Check for WebKit - do this first because Chrome includes Gecko in its User-Agent
		for (int x = 0; x < WEBKIT_IDENT.length; x++) {
			if (userAgent.indexOf(WEBKIT_IDENT[x]) != -1)
				return Browser.WEBKIT;
		}
		
		// Check for Gecko/Firefox 3.6
		for (int x = 0; x < FF36_IDENT.length; x++) {
			if (userAgent.indexOf(FF36_IDENT[x]) != -1)
				return Browser.FF36;
		}

		// Check for Gecko/Firefox
		for (int x = 0; x < MOZILLA_IDENT.length; x++) {
			if (userAgent.indexOf(MOZILLA_IDENT[x]) != -1)
				return Browser.MOZILLA;
		}
		
		// Check for Internet Explorer 9
		for (int x = 0; x < MSIE9_IDENT.length; x++) {
			if (userAgent.indexOf(MSIE9_IDENT[x]) != -1)
				return Browser.IE9;
		}

		// Check for Internet Explorer 8
		for (int x = 0; x < MSIE8_IDENT.length; x++) {
			if (userAgent.indexOf(MSIE8_IDENT[x]) != -1)
				return Browser.IE8;
		}

		// Check for Internet Explorer 7
		for (int x = 0; x < MSIE7_IDENT.length; x++) {
			if (userAgent.indexOf(MSIE7_IDENT[x]) != -1)
				return Browser.IE7;
		}

		// Check for Internet Explorer 5/6
		for (int x = 0; x < MSIE_IDENT.length; x++) {
			if (userAgent.indexOf(MSIE_IDENT[x]) != -1)
				return Browser.IE6;
		}
		
		// Check for Opera
		for (int x = 0; x < OPERA_IDENT.length; x++) {
			if (userAgent.indexOf(OPERA_IDENT[x]) != -1)
				return Browser.OPERA;
		}

		return Browser.UNKNOWN;
	}

	/**
	 * Helper method to search the ident stirngs and return the operating system.
	 */
	private OS getOS(String userAgent) {
		if (userAgent == null)
			return OS.UNKNOWN;
		if (userAgent.indexOf("Windows") != -1)
			return OS.WINDOWS;
		if (userAgent.indexOf("iPad") != -1)
			return OS.IOS;
		if (userAgent.indexOf("MAC OS") != -1)
			return OS.OSX;
		for (int x = 0; x < LINUX_IDENT.length; x++) {
			if (userAgent.indexOf(LINUX_IDENT[x]) != -1)
				return OS.LINUX;
		}

		return OS.UNKNOWN;
	}
}