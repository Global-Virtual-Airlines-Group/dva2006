// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.deltava.util.ComboUtils;

/**
 * A JSP tag to place feedback scores into the page. 
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class FeedbackScoresTag extends SimpleTagSupport {
	
	private String _attrName;
	
	/**
	 * Sets the page attribute to store the score values in.
	 * @param varName the page attribute name
	 */
	public void setVar(String varName) {
		_attrName = varName;
	}
	
	@Override
	public void doTag() {
		JspContext ctx = getJspContext();
		ctx.setAttribute(_attrName, ComboUtils.fromArray("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), PageContext.PAGE_SCOPE);
	}
}