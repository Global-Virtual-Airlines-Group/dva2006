// Copyright 2005, 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.Pilot;

import org.deltava.commands.CommandContext;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP tag to render page layouts in a browser-specific way. On Mozilla, absolutely positioned DIV elements will be
 * used, while tables will be used for Internet Explorer 6.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class PageTag extends TagSupport {
	
	private boolean _renderTable;
	private boolean _sideMenu;
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
	 * Tells a child tag whether we are rendering TABLE or DIV elements.
	 * @return TRUE if rendering TABLEs, otherwise FALSE
	 */
	boolean renderTable() {
		return _renderTable;
	}
	
	/**
	 * Tells a child tag wheter we are rendering side menus or navigation bars.
	 * @return TRUE if rendering a side menu, otherwise FALSE
	 */
	boolean sideMenu() {
		return _sideMenu;
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
		_renderTable = ContentHelper.isIE6(pageContext);
		
		// Check if our screen size is big enough
		if (!_renderTable) {
			HttpServletRequest hreq = (HttpServletRequest) pageContext.getRequest(); 
			HttpSession s = hreq.getSession(false);
			if (s != null) {
				Pilot usr = (Pilot) hreq.getUserPrincipal();
				_sideMenu = ((usr == null) || !usr.getShowNavBar());
				if (!_sideMenu) {
					Number sX = (Number) s.getAttribute(CommandContext.SCREENX_ATTR_NAME);
					_sideMenu = (sX == null) || (sX.intValue() < 1280);
				}
			} else
				_sideMenu = true;
		} else
			_sideMenu = true;
		
		// Render a table for IE6
		try {
			JspWriter out = pageContext.getOut();
			if (_renderTable)
				out.print("<table id=\"ieLayout\" class=\"navside\"><tbody>");
			else if (_sideMenu)
				out.print("<div class=\"navside\">");
			else
				out.print("<div class=\"navbar\">");
		} catch (Exception e) {
			throw new JspException(e);
		}

		// Include the body
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Writes the layout element's closing tag to the JSP output stream.
	 * @return TagSuppport.EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();
			if (_renderTable) {
				if (_rowOpen)
					out.print("</tr>");
				
				out.print("</tbody></table>");
			} else
				out.print("</div>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}
}