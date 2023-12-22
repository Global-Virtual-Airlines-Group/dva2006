// Copyright 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.http.*;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to set an HTML Expires header.
 * @author Luke
 * @version 11.1
 * @since 7.1
 */

public class ExpirationTag extends TagSupport {

	private int _expiryTime = 0;
	
	/**
	 * Sets the expiration time.
	 * @param sec the expiration time in seconds
	 */
	public void setExpires(int sec) {
		_expiryTime = Math.max(0, sec);
	}
	
	@Override
	public int doEndTag() {
		
		// Detect cache-busting
		HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
		boolean noCB = (req.getParameter("cb") == null);
		
		HttpServletResponse rsp = (HttpServletResponse) pageContext.getResponse();
		if ((_expiryTime > 0) && noCB) {
			StringBuilder buf = new StringBuilder();
			if (req.getUserPrincipal() != null)
				buf.append("private, ");
			
			buf.append("max-age=").append(_expiryTime);
			rsp.setHeader("cache-control", buf.toString());
			rsp.setDateHeader("Expires", System.currentTimeMillis() + (_expiryTime * 1000));
		} else
			rsp.setHeader("cache-control", "no-cache");
		
		return EVAL_PAGE;
	}
}