// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to save the current request protocol into a request variable. 
 * @author Luke
 * @version 6.0
 * @since 6.0
 */

public class RequestProtocolTag extends TagSupport {

	private String _varName;
	
	/**
	 * Sets the request variable name to store the protocol in. 
	 * @param name the variable name
	 */
	public void setVar(String name) {
		_varName = name;
	}
	
	/**
	 * Saves the request protocol into a request variable
	 * @return EVAL_PAGE always 
	 */
	@Override
	public int doEndTag() {
		pageContext.setAttribute(_varName, pageContext.getRequest().getScheme(), PageContext.REQUEST_SCOPE);
		return EVAL_PAGE;
	}
}