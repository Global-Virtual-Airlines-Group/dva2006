// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP tag to filter content based on the client operating system.
 * @author Luke
 * @version 2.2
 * @since 2.2
 * @see org.deltava.servlet.filter.BrowserTypeFilter
 */

public class OSFilterTag extends TagSupport {

	private boolean _doWindows;
	private boolean _doMacOS;
	private boolean _doLinux;
	
	/**
	 * Sets whether the tag body should be displayed to a browser running Microsoft Windows.
	 * @param show TRUE if the body should be displayed, otherwise FALSE
	 */
	public void setWindows(boolean show) {
		_doWindows = show;
	}
	
	/**
	 * Sets whether the tag body should be displayed to a browser running Mac OS.
	 * @param show TRUE if the body should be displayed, otherwise FALSE
	 */
	public void setMac(boolean show) {
		_doMacOS = show;
	}
	
	/**
	 * Sets whether the tag body should be displayed to a browser running Linux.
	 * @param show TRUE if the body should be displayed, otherwise FALSE
	 */
	public void setLinux(boolean show) {
		_doLinux = show;
	}
	
    /**
     * Clears state by reseting the display list.
     */
	public void release() {
		super.release();
		_doWindows = false;
		_doMacOS = false;
		_doLinux = false;
	}

	/**
	 * Determines wether the enclosed content should be rendered to the JSP output stream.
	 * @return TagSupport.EVAL_BODY_INCLUDE or TagSupport.SKIP_BODY
	 */
	public int doStartTag() {
		if (_doWindows && ContentHelper.isWindows(pageContext))
			return EVAL_BODY_INCLUDE;
		else if (_doMacOS && ContentHelper.isMac(pageContext))
			return EVAL_BODY_INCLUDE;
		else if (_doLinux && ContentHelper.isLinux(pageContext))
			return EVAL_BODY_INCLUDE;
		
		return SKIP_BODY;
	}
	
    /**
     * Closes the JSP, and releases state.
     * @return EVAL_PAGE
     */
	public int doEndTag() {
		release();
    	return EVAL_PAGE;
	}
}