// Copyright 2005, 2006, 2008, 2009, 2010, 2011, 2016, 2017, 2020, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib;

import java.util.*;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.system.*;

import org.deltava.commands.HTTPContext;

/**
 * A Helper class to check whether content has been aded into this request. This is also used as a helper to modify the dynamic Content Security Policy.
 * @author Luke
 * @version 12.0
 * @since 1.0
 * @see org.deltava.servlet.filter.BrowserTypeFilter
 * @see org.deltava.taglib.content.BrowserFilterTag
 * @see ContentSecurityPolicy
 */

public class ContentHelper {
	
	private static final Logger log = LogManager.getLogger(ContentHelper.class);
	
	private static final String PUSH_URL_ATTR = "$PushURLs";
	private static final String CONTENT_MAP_ATTR = "$TagContentNames$";

	// Singleton constructor
	private ContentHelper() {
		super();
	}

	/**
	 * Updates a request-located map of all inserted content files.
	 * @param ctx the JSP PageContext
	 * @param contentType the type of content (css/js)
	 * @param contentName the name of the inserted content
	 * @see ContentHelper#containsContent(PageContext, String, String)
	 */
	@SuppressWarnings("unchecked")
	public static void addContent(PageContext ctx, String contentType, String contentName) {

		// Find the content name map
		Set<String> content = (Set<String>) ctx.findAttribute(CONTENT_MAP_ATTR);
		if (content == null) {
			content = new HashSet<String>();
			ctx.setAttribute(CONTENT_MAP_ATTR, content, PageContext.REQUEST_SCOPE);
		}

		// Add the resource to the content name map
		content.add(contentType + "$" + contentName);
	}

	/**
	 * Determines if a particular content file has been inserted during this request invocation.
	 * @param ctx the JSP PageContext
	 * @param contentType the type of content (css/js)
	 * @param contentName the name of the content
	 * @return TRUE if the content has been added, otherwise FALSE
	 * @see ContentHelper#addContent(PageContext, String, String)
	 */
	public static boolean containsContent(PageContext ctx, String contentType, String contentName) {
		Set<?> content = (Set<?>) ctx.findAttribute(CONTENT_MAP_ATTR);
		return (content == null) ? false : content.contains(contentType + "$" + contentName);
	}
	
	/**
	 * Clears any included content with resetting the request, like when forwarding to an error page. The push content
	 * list is not cleared since the headers have already been set.
	 * @param req the ServletRequest
	 */
	public static void clearContent(ServletRequest req) {
		req.removeAttribute(CONTENT_MAP_ATTR);
	}
	
	/**
	 * Fetches the browser data.
	 * @param ctx the PageContext object
	 * @return an HTTPContextData bean, or none if null
	 * @see BrowserInfoTag
	 */
	public static HTTPContextData getBrowserContext(PageContext ctx) {
		return (HTTPContextData) ctx.getAttribute(HTTPContext.HTTPCTXT_ATTR_NAME, PageContext.REQUEST_SCOPE);
	}
	
	/**
	 * Adds an HTTP/2 preload link header to the response.
	 * @param ctx the PageContext object
	 * @param url the URL to push
	 * @param type the type of resource
	 */
	public static void pushContent(PageContext ctx, String url, String type) {
		HTTPContextData bctxt = getBrowserContext(ctx);
		HttpServletResponse rsp = (HttpServletResponse) ctx.getResponse();
		if ((bctxt == null) || !bctxt.isHTTP2() || rsp.isCommitted())
			return;
		
		// Load list of preloaded resources
		@SuppressWarnings("unchecked")
		Collection<Object> pushLinks = (Collection<Object>) ctx.findAttribute(PUSH_URL_ATTR);
		if (pushLinks == null) {
			pushLinks = new HashSet<Object>();
			ctx.setAttribute(PUSH_URL_ATTR, pushLinks, PageContext.REQUEST_SCOPE);
		}
	
		// Build the header
		StringBuilder buf = new StringBuilder();
		if (url.startsWith("/")) {
			buf.append("https://");
			buf.append(ctx.getRequest().getServerName());
		}
		
		buf.append(url);
		
		// Check if we add
		String rsrc = buf.toString();
		if (!pushLinks.add(rsrc))
			return;
		
		// Finish building
		buf.insert(0, '<');
		buf.append(">; rel=preload; as=");
		buf.append(type);
		rsp.addHeader("Link", buf.toString());
	}

	/**
	 * Adds a host to the dynamic Content Security Policy.
	 * @param ctx the PageContext object
	 * @param cs a ContentSecurity enumeration
	 * @param hosts the host names or wildcards
	 */
	public static void addCSP(PageContext ctx, ContentSecurity cs, String... hosts) {
		ContentSecurityPolicy csp = (ContentSecurityPolicy) ctx.getAttribute(HTTPContext.CSP_ATTR_NAME, PageContext.REQUEST_SCOPE);
		if (csp == null)
			throw new IllegalStateException("No Content Security Policy in request");
		
		for (int x = 0; x < hosts.length; x++)
			csp.add(cs, hosts[x]);
	}

	/**
	 * Writes a dynamic Content Security Policy header to the response.
	 * @param ctx the PageContext object
	 */
	public static void flushCSP(PageContext ctx) {
		ContentSecurityPolicy csp = (ContentSecurityPolicy) ctx.getAttribute(HTTPContext.CSP_ATTR_NAME, PageContext.REQUEST_SCOPE);
		if (csp == null)
			throw new IllegalStateException("No Content Security Policy in request");
		
		// Check if header already set
		HttpServletResponse rsp = (HttpServletResponse) ctx.getResponse();
		if (rsp.containsHeader(csp.getHeader())) return;
		
		// Check for committed request that will silent fail header setting
		if (rsp.isCommitted()) {
			HttpServletRequest req = (HttpServletRequest) ctx.getRequest();
			log.warn("Cannot set CSP Header for {} - {} already committed", req.getRemoteUser(), req.getRequestURI());
			return;
		}
		
		rsp.setHeader(csp.getHeader(), csp.getData());
		if (csp.hasReportURI()) {
			rsp.setHeader("Reporting-Endpoints", csp.getReportHeader());
			rsp.setHeader("Report-To", csp.getReportHeader());
		}
	}
}