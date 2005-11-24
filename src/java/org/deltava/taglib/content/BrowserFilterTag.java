// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.JspException;

import org.deltava.taglib.BrowserDetectingTag;

/**
 * A JSP tag to filter content based on the browser type.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class BrowserFilterTag extends BrowserDetectingTag {

	private boolean _showIE;
	private boolean _showMoz;
	
	/**
	 * Marks this content as visible to Internet Explorer users.
	 * @param showIE TRUE if the content should be shown to IE users, otherwise FALSE
	 */
	public void setIe(boolean showIE) {
		_showIE = showIE; 
	}
	
	/**
	 * Marks this content as visible to Mozilla/Firefox users.
	 * @param showMoz TRUE if the content should be shown to Mozilla users, otherwise FALSE
	 */
	public void setMozilla(boolean showMoz) {
		_showMoz = showMoz;
	}
	
	/**
	 * Determines wether the enclosed content should be rendered to the JSP output stream.
	 * @return TagSupport.EVAL_BODY_INCLUDE or TagSupport.SKIP_BODY
	 * @throws JspException never
	 */
	public int doStartTag() throws JspException {
		if (isIE() && _showIE)
			return EVAL_BODY_INCLUDE;
		else if (isFirefox() && _showMoz)
			return EVAL_BODY_INCLUDE;
		else
			return SKIP_BODY;
	}
	
	/**
	 * Closes the tag and releases state.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException never
	 */
	public int doEndTag() throws JspException {
		release();
		return EVAL_PAGE;
	}

	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		_showIE = false;
		_showMoz = false;
		super.release();
	}
}