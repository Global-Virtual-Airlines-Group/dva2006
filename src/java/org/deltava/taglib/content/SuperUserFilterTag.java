// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.http.*;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import static org.deltava.commands.CommandContext.SU_ATTR_NAME;

/**
 * A JSP tag to filter out content that is displayed for superusers only.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SuperUserFilterTag extends TagSupport {
	
	private String _varName;

	/**
	 * Sets the request attribute to save the impersonated user object into.
	 * @param name the attribute name
	 */
	public void setVar(String name) {
		_varName = name;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_varName = null;
	}
	
	/**
	 * Renders the start of the JSP tag and applies the filter.
	 * @return EVAL_BODY_INCLUDE if acting as a superUser, otherwise SKIP_BODY
	 * @throws JspException if an error occurs
	 */
	public int doStartTag() throws JspException {
		
		// Check if we have the superuser flag
		HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
		HttpSession s = req.getSession(false);
		if (s == null)
			return SKIP_BODY;
		
		// Check for the super user attribute
		Object su = s.getAttribute(SU_ATTR_NAME);
		if (su == null)
			return SKIP_BODY;
		
		// Save the super user in the request
		if (_varName != null)
			req.setAttribute(_varName, su);
		
		return EVAL_BODY_INCLUDE;
	}
	
	/**
	 * Renders the JSP closing tag and releases state.
	 * @return EVAL_PAGE always
	 * @throws JspException never
	 */
	public int doEndTag() throws JspException {
		release();
		return EVAL_PAGE;
	}
}