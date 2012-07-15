// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.util.Collections;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * A JSP tag to add an empty list to the request. 
 * @author Luke
 * @version 4.2
 * @since 4.2
 */

public class EmptyListTag extends SimpleTagSupport {

	private String _var;
	
	/**
	 * Sets the page attribute to store the cache info in.
	 * @param varName the page attribute name
	 */
	public void setVar(String varName) {
		_var = varName;
	}
	
	/**
	 * Saves the empty list in the request.  
	 */
	public void doTag() throws JspException {
		JspContext ctx = getJspContext();
		ctx.setAttribute(_var, Collections.emptyList(), PageContext.PAGE_SCOPE);
	}
}