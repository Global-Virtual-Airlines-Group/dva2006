// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import org.deltava.beans.system.HTTPContextData;

import org.deltava.taglib.BrowserInfoTag;

/**
 * A JSP tag to filter content based on IPv4 or IPv6 addresses. 
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public class IPFilterTag extends BrowserInfoTag {

	private boolean _doIPv4;
	private boolean _doIPv6;
	
	/**
	 * Sets whether to show content to users accessing the page via IPv4.
	 * @param show TRUE if content should be displayed, otherwise FALSE
	 */
	public void setIPv4(boolean show) {
		_doIPv4 = show;
	}
	
	/**
	 * Sets whether to show content to users accessing the page via IPv6.
	 * @param show TRUE if content should be displayed, otherwise FALSE
	 */
	public void setIPv6(boolean show) {
		_doIPv6 = show;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_doIPv4 = false;
		_doIPv6 = false;
	}
	
	/**
	 * Determines whether to include content based on the IP address.
	 * @return EVAL_BODY_INCLUDE or SKIP_BODY
	 */
	@Override
	public int doStartTag() {
		HTTPContextData ctxt = getBrowserContext();
		return ((ctxt.isIPv6() && _doIPv6) || (!ctxt.isIPv6() && _doIPv4)) ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}

	/**
	 * Closes the tag. Included only to reset state.
	 */
	@Override
	public int doEndTag() {
		release();
		return EVAL_PAGE;
	}
}