// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib;

import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.system.ContentSecurityPolicy;

import org.deltava.commands.HTTPContext;

/**
 * A JSP tag to generate inline blocks with CSP nonces.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public abstract class CSPNonceTag extends TagSupport {
	
	/**
	 * Returns the nonce from the request's Content Security Policy.
	 * @return the nonce, or null if none
	 */
	protected String getNonce() {
		ContentSecurityPolicy csp = (ContentSecurityPolicy) pageContext.getAttribute(HTTPContext.CSP_ATTR_NAME, PageContext.REQUEST_SCOPE);
		return (csp == null) ? null : csp.getNonce();
	}
}