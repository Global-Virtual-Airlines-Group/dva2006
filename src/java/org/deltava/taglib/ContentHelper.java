// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib;

import java.util.*;

import javax.servlet.jsp.PageContext;

/**
 * A Helper class to check wether content has been aded into this request.
 * @author Luke
 * @version 2.1
 * @since 1.0
 * @see org.deltava.servlet.filter.BrowserTypeFilter
 */

public class ContentHelper {

	private static final String CONTENT_MAP_ATTR = "$TagContentNames$";

	// Singleton constructor
	private ContentHelper() {
	}

	/**
	 * Updates a request-located map of all inserted content files.
	 * @param contentType the type of content (css/js)
	 * @param contentName the name of the inserted content
	 * @see ContentHelper#containsContent(PageContext, String, String)
	 */
	@SuppressWarnings("unchecked")
	public static void addContent(PageContext ctx, String contentType, String contentName) {

		// Find the content name map
		Set content = (Set) ctx.findAttribute(CONTENT_MAP_ATTR);
		if (content == null) {
			content = new HashSet();
			ctx.getRequest().setAttribute(CONTENT_MAP_ATTR, content);
		}

		// Add the resource to the content name map
		content.add(contentType + "$" + contentName);
	}

	/**
	 * Determines if a particular content file has been inserted during this request invocation.
	 * @param contentType the type of content (css/js)
	 * @param contentName the name of the content
	 * @return TRUE if the content has been added, otherwise FALSE
	 * @see ContentHelper#addContent(PageContext, String, String)
	 */
	public static boolean containsContent(PageContext ctx, String contentType, String contentName) {
		Set content = (Set) ctx.findAttribute(CONTENT_MAP_ATTR);
		return (content == null) ? false : content.contains(contentType + "$" + contentName);
	}
	
	/**
     * Detects if the browser is Microsoft Internet Explorer 8 or below.
     * @return TRUE if the browser is Internet Explorer 8, otherwise FALSE
     */
	public static boolean isIE8(PageContext ctx) {
		return (ctx.getRequest().getAttribute("browser$ie8") != null);
	}
	
    /**
     * Detects if the browser is Microsoft Internet Explorer 7 or below.
     * @return TRUE if the browser is Internet Explorer 7, otherwise FALSE
     */
    public static boolean isIE7(PageContext ctx) {
    	return (ctx.getRequest().getAttribute("browser$ie7") != null);
    }
    
    /**
     * Detects if the browser is Microsoft Internet Explorer 6 or below.
     * @return TRUE if the browser is Internet Explorer 5 or 6, otherwise FALSE
     */
    public static boolean isIE6(PageContext ctx) {
    	return (ctx.getRequest().getAttribute("browser$ie") != null);
    }
    
    /**
     * Detects if the browser is Mozilla Firefox.
     * @return TRUE if the browser is Firefox, otherwise FALSE
     */
    public static boolean isFirefox(PageContext ctx) {
        return (ctx.getRequest().getAttribute("browser$mozilla") != null);
    }
}