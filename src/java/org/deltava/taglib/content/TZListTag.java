// Copyright 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.TZInfo;

/**
 * A JSP Tag to save the list of time zones as a request attribute.
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public class TZListTag extends TagSupport {
	
	private String _varName;

	/**
	 * Sets the request attribute name.
	 * @param vName the name of the request attribute to store the data in
	 */
	public void setVar(String vName) {
		_varName = vName;
	}

	/**
	 * Saves the time zone list in the request body.
	 * @return EVAL_PAGE always
	 */
	@Override
	public int doEndTag() {
		pageContext.setAttribute(_varName, TZInfo.getAll(), PageContext.REQUEST_SCOPE);
		return EVAL_PAGE;
	}
}