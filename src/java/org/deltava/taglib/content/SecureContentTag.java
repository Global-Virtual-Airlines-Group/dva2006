// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to include content based on SSL.
 * @author Luke
 * @version 6.0
 * @since 6.0
 */

public class SecureContentTag extends TagSupport {
	
	private boolean _needSecure;
	
	/**
	 * Sets whether the content requires an SSL or non-SSL connection.
	 * @param needSecure TRUE if HTTPS required, otherwise FALSE
	 */
	public void setSecure(boolean needSecure) {
		_needSecure = needSecure;
	}

	/**
	 * Checks whether the page protocol allows this content to be included.
	 * @return SKIP_BODY or EVAL_BODY_INCLUDE 
	 */
	@Override
	public int doStartTag() {
		return (_needSecure == pageContext.getRequest().isSecure()) ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}
}