// Copyright 2005, 2006, 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP tag to filter content based on the browser type.
 * @author Luke
 * @version 2.8
 * @since 1.0
 * @see org.deltava.servlet.filter.BrowserTypeFilter
 */

public class BrowserFilterTag extends TagSupport {

	private boolean _showIE6;
	private boolean _showIE7;
	private boolean _showIE8;
	private boolean _showMoz;
	private boolean _showWebKit;
	private boolean _showOpera;
	private boolean _showFF36;
	
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
	}
	
	/**
	 * Marks this content as visible to Internet Explorer 5 and 6 users.
	 * @param showIE TRUE if the content should be shown to IE5/IE6 users, otherwise FALSE
	 * @see BrowserFilterTag#setIe7(boolean)
	 * @see BrowserFilterTag#setIe8(boolean)
	 */
	public void setIe6(boolean showIE) {
		_showIE6 = showIE;
	}

	/**
	 * Marks this content as visible to Internet Explorer 7 users.
	 * @param showIE TRUE if the content should be shown to IE7 users, otherwise FALSE
	 * @see BrowserFilterTag#setIe6(boolean)
	 * @see BrowserFilterTag#setIe8(boolean)
	 */
	public void setIe7(boolean showIE) {
		_showIE7 = showIE;
	}
	
	/**
	 * Marks this content as visible to Internet Explorer 8 users.
	 * @param showIE TRUE if the content should be shown to IE8 users, otherwise FALSE
	 * @see BrowserFilterTag#setIe6(boolean)
	 * @see BrowserFilterTag#setIe7(boolean)
	 */
	public void setIe8(boolean showIE) {
		_showIE8 = showIE;
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
		_showFF36 = showMoz;
		_showMoz = showMoz;
	}
	
	/**
	 * Marks this content as visible to Firefox 3.6 users.
	 * @param showFF36 TRUE if the content should be shown to Firefox 3.6 users, otherwise FALSE
	 */
	public void setFf36(boolean showFF36) {
		_showFF36 = showFF36;
	}
	
	/**
	 * Determines wether the enclosed content should be rendered to the JSP output stream.
	 * @return TagSupport.EVAL_BODY_INCLUDE or TagSupport.SKIP_BODY
	 */
	public int doStartTag() {
		if (ContentHelper.isIE6(pageContext) && _showIE6)
			return EVAL_BODY_INCLUDE;
		else if (ContentHelper.isIE7(pageContext) && _showIE7)
			return EVAL_BODY_INCLUDE;
		else if (ContentHelper.isIE8(pageContext) && _showIE8)
			return EVAL_BODY_INCLUDE;
		else if (ContentHelper.isFirefox36(pageContext) && _showFF36)
			return EVAL_BODY_INCLUDE;
		else if (ContentHelper.isFirefox(pageContext) && _showMoz)
			return EVAL_BODY_INCLUDE;
		else if (ContentHelper.isWebKit(pageContext) && _showWebKit)
			return EVAL_BODY_INCLUDE;
		else if (ContentHelper.isOpera(pageContext) && _showOpera)
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
		super.release();
		_showIE6 = false;
		_showIE7 = false;
		_showIE8 = false;
		_showMoz = false;
		_showFF36 = false;
		_showWebKit = false;
		_showOpera = false;
	}
}