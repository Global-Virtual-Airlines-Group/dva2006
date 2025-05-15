// Copyright 2011, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib;

import static javax.servlet.jsp.PageContext.SESSION_SCOPE;
import static org.deltava.commands.HTTPContext.*; 

import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.system.CAPTCHAResult;
import org.deltava.beans.system.HTTPContextData;

/**
 * A JSP tag that operates differently based on browser type. 
 * @author Luke
 * @version 11.6
 * @since 3.7
 * @see org.deltava.servlet.filter.BrowserTypeFilter
 */

public abstract class BrowserInfoTag extends TagSupport {

	/**
	 * Fetches the browser data.
	 * @return an HTTPContextData bean, or none if null
	 */
	protected HTTPContextData getBrowserContext() {
		return (HTTPContextData) pageContext.getRequest().getAttribute(HTTPCTXT_ATTR_NAME);
	}
	
	/**
	 * Returns whether the user has passed CAPTCHA validation.
	 * @return TRUE if the user has passed the CAPTCHA, otherwise FALSE
	 */
	protected boolean passedCAPTCHA() {
		CAPTCHAResult cr = (CAPTCHAResult) pageContext.getAttribute(CAPTCHA_ATTR_NAME, SESSION_SCOPE);
		return (cr != null) && cr.getIsSuccess();
	}
}