// Copyright 2005, 2006, 2008, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.JspException;

import org.deltava.beans.system.BrowserType;
import org.deltava.beans.system.HTTPContextData;

import org.deltava.taglib.*;

/**
 * A JSP tag to filter content based on the browser type.
 * @author Luke
 * @version 3.7
 * @since 1.0
 * @see org.deltava.servlet.filter.BrowserTypeFilter
 */

public class BrowserFilterTag extends BrowserInfoTag {

	private boolean _showIE6;
	private boolean _showIE7;
	private boolean _showIE8;
	private boolean _showIE9;
	private boolean _showMoz;
	private boolean _showWebKit;
	private boolean _showOpera;
	private boolean _showHTML5;
	
	/**
	 * Marks this content as visible to all Internet Explorer users.
	 * @param showIE TRUE if the content should be shown to IE users, otherwise FALSE
	 * @see BrowserFilterTag#setIe6(boolean)
	 * @see BrowserFilterTag#setIe7(boolean)
	 */
	public void setIe(boolean showIE) {
		_showIE6 = showIE;
		_showIE7 = showIE;
		_showIE8 = showIE;
		_showIE9 = showIE;
	}
	
	/**
	 * Marks this content as visible to Internet Explorer 5 and 6 users.
	 * @param showIE TRUE if the content should be shown to IE5/IE6 users, otherwise FALSE
	 */
	public void setIe6(boolean showIE) {
		_showIE6 = showIE;
	}

	/**
	 * Marks this content as visible to Internet Explorer 7 users.
	 * @param showIE TRUE if the content should be shown to IE7 users, otherwise FALSE
	 */
	public void setIe7(boolean showIE) {
		_showIE7 = showIE;
	}
	
	/**
	 * Marks this content as visible to Internet Explorer 8 users.
	 * @param showIE TRUE if the content should be shown to IE8 users, otherwise FALSE
	 */
	public void setIe8(boolean showIE) {
		_showIE8 = showIE;
	}
	
	/**
	 * Marks this content as visible to Internet Explorer 9 users.
	 * @param showIE TRUE if the content should be shown to IE9 users, otherwise FALSE
	 */
	public void setIe9(boolean showIE) {
		_showIE9 = showIE;
	}
	
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
	 * Marks this content as visible to HTML5 users.
	 * @param showHTML5 TRUE if the content should be shown to HTML5 users, otherwise FALSE
	 */
	public void setHtml5(boolean showHTML5) {
		_showHTML5 = showHTML5;
	}
	
	/**
	 * Determines whether the enclosed content should be rendered to the JSP output stream.
	 * @return TagSupport.EVAL_BODY_INCLUDE or TagSupport.SKIP_BODY
	 */
	public int doStartTag() {
		HTTPContextData bctxt = getBrowserContext();
		if (bctxt == null)
			return SKIP_BODY;
		else if (bctxt.getHTML5() && _showHTML5)
			return EVAL_BODY_INCLUDE;
		
		BrowserType bt = bctxt.getBrowserType();
		if (bt == BrowserType.IE) {
			if (_showIE6 && (bctxt.getMajor() == 6))
				return EVAL_BODY_INCLUDE;
			if (_showIE7 && (bctxt.getMajor() == 7))
				return EVAL_BODY_INCLUDE;
			if (_showIE8 && (bctxt.getMajor() == 8))
				return EVAL_BODY_INCLUDE;
			if (_showIE9 && (bctxt.getMajor() == 9))
				return EVAL_BODY_INCLUDE;
		} else if ((bt == BrowserType.FIREFOX) && _showMoz)
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
	public int doEndTag() throws JspException {
		release();
		return EVAL_PAGE;
	}

	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_showIE6 = false;
		_showIE7 = false;
		_showIE8 = false;
		_showIE9 = false;
		_showMoz = false;
		_showWebKit = false;
		_showOpera = false;
		_showHTML5 = false;
	}
}