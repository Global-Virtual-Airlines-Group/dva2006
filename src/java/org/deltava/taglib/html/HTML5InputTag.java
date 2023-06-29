// Copyright 2012, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.deltava.beans.Pilot;
import org.deltava.util.system.SystemData;

/**
 * An abstract utility class for HTML5 form input elements. 
 * @author Luke
 * @version 11.0
 * @since 5.0
 */

abstract class HTML5InputTag extends InputTag {
	
	private final boolean _html5Enabled = SystemData.getBoolean("html.v5");
	
	protected String _dateFmt;
	protected String _timeFmt;

	/**
	 * Strips out HTML5-specific attributes.
	 */
	protected void removeHTML5Attributes() {
		_data.remove("max");
		_data.remove("min");
		_data.remove("step");
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_dateFmt = null;
		_timeFmt = null;
	}

	/**
	 * Helper method to determine whether HTML5 support is enabled and the browser is comptable.
	 * @return TRUE if HTML5 elements should be rendered, otherwise FALSE
	 */
	protected boolean isHTML5() {
		return _html5Enabled && getBrowserContext().isHTML5();  
	}
	
	/**
	 * Updates this tag's page context and extracts HTML4-specific formatting codes.
	 * @param ctxt the JSP page context
	 */
	@Override
	public void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		if (!isHTML5()) return;
		HttpServletRequest req = (HttpServletRequest) ctxt.getRequest();
		Principal p = req.getUserPrincipal();
		if (p instanceof Pilot usr) {
			_dateFmt = usr.getDateFormat();
			_timeFmt = usr.getTimeFormat();
		}
	}
}