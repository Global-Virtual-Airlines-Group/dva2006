// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP tag to render page layouts in a browser-specific way. On Mozilla, absolutely positioned DIV elements will be
 * used, while tables will be used for Internet Explorer 6.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class PageTag extends TagSupport {
	
	private boolean _rowOpen;

	/**
	 * Checks if the table row (for Internet Explorer) is currently open. This method is package-private to allow the
	 * {@link org.deltava.taglib.layout.RegionTag} to access it.
	 * @return TRUE if the row is open, otherwise FALSE
	 */
	boolean isRowOpen() {
		return _rowOpen;
	}
	
	/**
	 * Marks the table row (for Internet Explorer) as open or closed. This method is package-private to allow the
	 * {@link org.deltava.taglib.layout.RegionTag} to access it.
	 * @param isOpen TRUE if the row is open, otherwise FALSE
	 */
	void setRowOpen(boolean isOpen) {
		_rowOpen = isOpen;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		_rowOpen = false;
		super.release();
	}
	
	/**
	 * Writes the layout element's opening tag to the JSP output stream.
	 * @return TagSuppport.EVAL_BODY_INCLUDE always
	 * @throws JspException if an error occurs
	 */
	public int doStartTag() throws JspException {
		// Do nothing for non-IE6
		if (!ContentHelper.isIE6(pageContext))
			return EVAL_BODY_INCLUDE;

		// Render a table for IE
		try {
			pageContext.getOut().print("<table id=\"ieLayout\" cellspacing=\"0\" cellpadding=\"0\"><tbody>");
		} catch (Exception e) {
			throw new JspException(e);
		}

		// Include the body
		_rowOpen = false;
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Writes the layout element's closing tag to the JSP output stream.
	 * @return TagSuppport.EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		if (ContentHelper.isIE6(pageContext)) {
			JspWriter out = pageContext.getOut();
			try {
				if (_rowOpen)
					out.print("</tr>");
				
				out.print("</tbody></table>");
			} catch (Exception e) {
				throw new JspException(e);
			}
		}

		// Release and return
		release();
		return EVAL_PAGE;
	}
}