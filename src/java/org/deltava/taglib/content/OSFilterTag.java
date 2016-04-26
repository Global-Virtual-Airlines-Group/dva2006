// Copyright 2008, 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import org.deltava.beans.system.*;

import org.deltava.taglib.BrowserInfoTag;

/**
 * A JSP tag to filter content based on the client operating system.
 * @author Luke
 * @version 7.0
 * @since 2.2
 * @see org.deltava.servlet.filter.BrowserTypeFilter
 */

public class OSFilterTag extends BrowserInfoTag {

	private boolean _doWindows;
	private boolean _doMacOS;
	private boolean _doiOS;
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
	 * Sets whether the tag body should be displayed to a browser running iOS.
	 * @param show TRUE if the body should be displayed, otherwise FALSE
	 */
	public void setIOS(boolean show) {
		_doiOS = show;
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
	@Override
	public void release() {
		super.release();
		_doWindows = false;
		_doiOS = false;
		_doMacOS = false;
		_doLinux = false;
	}

	/**
	 * Determines whether the enclosed content should be rendered to the JSP output stream.
	 * @return TagSupport.EVAL_BODY_INCLUDE or TagSupport.SKIP_BODY
	 */
	@Override
	public int doStartTag() {
		HTTPContextData ctxt = getBrowserContext();
		if (ctxt == null)
			return EVAL_BODY_INCLUDE;
		
		if (_doWindows && (ctxt.getOperatingSystem() == OperatingSystem.WINDOWS))
			return EVAL_BODY_INCLUDE;
		else if (_doMacOS && (ctxt.getOperatingSystem() == OperatingSystem.OSX))
			return EVAL_BODY_INCLUDE;
		else if (_doiOS && (ctxt.getOperatingSystem() == OperatingSystem.IOS))
			return EVAL_BODY_INCLUDE;
		else if (_doLinux && (ctxt.getOperatingSystem() == OperatingSystem.LINUX))
			return EVAL_BODY_INCLUDE;
		
		return SKIP_BODY;
	}
	
    /**
     * Closes the JSP, and releases state.
     * @return EVAL_PAGE
     */
	@Override
	public int doEndTag() {
		release();
    	return EVAL_PAGE;
	}
}