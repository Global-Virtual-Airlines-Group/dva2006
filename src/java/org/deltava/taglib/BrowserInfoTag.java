// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib;

import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.system.HTTPContextData;

import org.deltava.commands.HTTPContext;

/**
 * A JSP tag that operates differently based on browser type. 
 * @author Luke
 * @version 3.7
 * @since 3.7
 * @see org.deltava.servlet.filter.BrowserTypeFilter
 */

public abstract class BrowserInfoTag extends TagSupport {

	/**
	 * Fetches the browser data.
	 * @return an HTTPContextData bean, or none if null
	 */
	protected HTTPContextData getBrowserContext() {
		return (HTTPContextData) pageContext.getRequest().getAttribute(HTTPContext.HTTPCTXT_ATTR_NAME);
	}
}