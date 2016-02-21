// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to store the host name in a request attribute. 
 * @author Luke
 * @version 6.4
 * @since 6.4
 */

public class ServerNameTag extends TagSupport {

	private String _varName;
	
	/**
	 * Sets the request variable name to store the protocol in. 
	 * @param name the variable name
	 */
	public void setVar(String name) {
		_varName = name;
	}

	/**
	 * Saves the server name into a request variable.
	 * @return EVAL_PAGE always 
	 */
	@Override
	public int doEndTag() {
		pageContext.setAttribute(_varName, pageContext.getRequest().getServerName(), PageContext.REQUEST_SCOPE);
		return EVAL_PAGE;
	}
}