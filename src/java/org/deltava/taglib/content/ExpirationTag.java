// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.http.HttpServletResponse;

import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to set an HTML Expires header.
 * @author Luke
 * @version 7.1
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
	
	/**
	 * Executes the tag.
	 * @return EVAL_PAGE always
	 */
	@Override
	public int doEndTag() {
		
		HttpServletResponse rsp = (HttpServletResponse) pageContext.getResponse();
		if (_expiryTime < 1)
			rsp.setHeader("cache-control", "no-cache");
		else
			rsp.setHeader("cache-control", "max-age=" + _expiryTime);
		
		return EVAL_PAGE;
	}
}