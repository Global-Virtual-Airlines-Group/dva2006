// Copyright 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to filter out content that is displayed for authenticated users only.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class AuthenticatedUserFilterTag extends TagSupport {

	private String _varName;
	private boolean _isAnonymous;
	
	/**
	 * Sets the request attribute to save the user object into.
	 * @param name the attribute name
	 */
	public void setVar(String name) {
		_varName = name;
	}
	
	/**
	 * Toggles the reverse switch for this tag and includes content for anonymous users only.
	 * @param isAnon TRUE if tag behavior should be reversed, otherwise FALSE
	 */
	public void setAnonymous(boolean isAnon) {
		_isAnonymous = isAnon;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_varName = null;
		_isAnonymous = false;
	}
	
	/**
	 * Renders the start of the JSP tag and applies the filter.
	 * @return EVAL_BODY_INCLUDE if authenticated, otherwise SKIP_BODY
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doStartTag() throws JspException {
		
		// Get the user object
		HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
		Principal usr = req.getUserPrincipal();
		
		// If we're anonymous and the flag is set, include
		if (usr == null)
			return _isAnonymous ? EVAL_BODY_INCLUDE : SKIP_BODY;
		
		// Save the user attribute
		if (_varName != null)
			req.setAttribute(_varName, usr);
		
		return _isAnonymous ? SKIP_BODY : EVAL_BODY_INCLUDE;
	}
	
	/**
	 * Renders the JSP closing tag and releases state.
	 * @return EVAL_PAGE always
	 * @throws JspException never
	 */
	@Override
	public int doEndTag() throws JspException {
		release();
		return EVAL_PAGE;
	}
}