// Copyright 2005, 2006, 2008, 2009, 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib;

import java.util.*;

import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletResponse;

import org.deltava.beans.system.HTTPContextData;

import org.deltava.commands.HTTPContext;

/**
 * A Helper class to check whether content has been aded into this request.
 * @author Luke
 * @version 7.2
 * @since 1.0
 * @see org.deltava.servlet.filter.BrowserTypeFilter
 * @see org.deltava.taglib.content.BrowserFilterTag
 */

public class ContentHelper {

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
	 * Fetches the browser data.
	 * @param ctx the PageContext object
	 * @return an HTTPContextData bean, or none if null
	 * @see BrowserInfoTag
	 */
	public static HTTPContextData getBrowserContext(PageContext ctx) {
		return (HTTPContextData) ctx.getRequest().getAttribute(HTTPContext.HTTPCTXT_ATTR_NAME);
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
			buf.append(ctx.getRequest().isSecure() ? "https" : "http");
			buf.append("://");
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
}