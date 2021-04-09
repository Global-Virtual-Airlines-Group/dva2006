// Copyright 2005, 2006, 2008, 2009, 2010, 2011, 2012, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.JspException;

import org.deltava.beans.system.*;

import org.deltava.taglib.*;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag to filter content based on the browser type.
 * @author Luke
 * @version 10.0
 * @since 1.0
 * @see org.deltava.servlet.filter.BrowserTypeFilter
 */

public class BrowserFilterTag extends BrowserInfoTag {
	
	private final boolean _html5Enabled = SystemData.getBoolean("html.v5");

	private boolean _showMoz;
	private boolean _showWebKit;
	private boolean _showOpera;
	private boolean _showHuman;
	private boolean _showHTML4;
	private boolean _showHTML5;
	
	/**
	 * Marks this content as visible to WebKit/Safari/Chrome users.
	 * @param showWebKit TRUE if the content should be shown to WebKit users, otherwise FALSE
	 */
	public void setWebKit(boolean showWebKit) {
		_showWebKit = showWebKit;
	}
	
	/**
	 * Marks this content as visible to Opera users.
	 * @param showOpera TRUE if content should be shown to Opera users, otherwise FALSE
	 */
	public void setOpera(boolean showOpera) {
		_showOpera = showOpera;
	}
	
	/**
	 * Marks this content as visible to Mozilla/Firefox users.
	 * @param showMoz TRUE if the content should be shown to Mozilla users, otherwise FALSE
	 */
	public void setMozilla(boolean showMoz) {
		_showMoz = showMoz;
	}
	
	/**
	 * Marks this content as visible to HTML4 users.
	 * @param showHTML4 TRUE if the content should be shown to HTML4 users, otherwise FALSE
	 */
	public void setHtml4(boolean showHTML4) {
		_showHTML4 = showHTML4;
	}
	
	/**
	 * Marks this content as visible to HTML5 users.
	 * @param showHTML5 TRUE if the content should be shown to HTML5 users, otherwise FALSE
	 */
	public void setHtml5(boolean showHTML5) {
		_showHTML5 = showHTML5;
	}
	
	/**
	 * Marks this content as only visible to humans.
	 * @param showHuman TRUE if not visible to bots, otherwise FALSE
	 */
	public void setHuman(boolean showHuman) {
		_showHuman = showHuman;
	}
	
	/**
	 * Determines whether the enclosed content should be rendered to the JSP output stream.
	 * @return TagSupport.EVAL_BODY_INCLUDE or TagSupport.SKIP_BODY
	 */
	@Override
	public int doStartTag() {
		HTTPContextData bctxt = getBrowserContext();
		if (bctxt == null)
			return SKIP_BODY;
		boolean isBrowserHTML5 = _html5Enabled && bctxt.isHTML5();
		if (isBrowserHTML5 && _showHTML5)
			return EVAL_BODY_INCLUDE;
		if (!isBrowserHTML5 && _showHTML4)
			return EVAL_BODY_INCLUDE;
		if ((bctxt.getBrowserType() != BrowserType.SPIDER) && _showHuman)
			return EVAL_BODY_INCLUDE;
		
		BrowserType bt = bctxt.getBrowserType();
		if ((bt == BrowserType.FIREFOX) && _showMoz)
			return EVAL_BODY_INCLUDE;
		else if (((bt == BrowserType.CHROME) || (bt == BrowserType.WEBKIT)) && _showWebKit)
			return EVAL_BODY_INCLUDE;
		else if ((bt == BrowserType.OPERA) && _showOpera)
			return EVAL_BODY_INCLUDE;
		
		return SKIP_BODY;
	}
	
	/**
	 * Closes the tag and releases state.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException never
	 */
	@Override
	public int doEndTag() throws JspException {
		release();
		return EVAL_PAGE;
	}

	@Override
	public void release() {
		super.release();
		_showMoz = false;
		_showWebKit = false;
		_showOpera = false;
		_showHTML4 = false;
		_showHTML5 = false;
		_showHuman = false;
	}
}