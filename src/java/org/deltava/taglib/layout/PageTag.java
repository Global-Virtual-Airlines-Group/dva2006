// Copyright 2005, 2006, 2008, 2009, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.Pilot;

import org.deltava.commands.CommandContext;

/**
 * A JSP tag to render page layouts in a user-specific way.
 * @author Luke
 * @version 3.6
 * @since 1.0
 */

public class PageTag extends TagSupport {
	
	private boolean _sideMenu;

	/**
	 * Tells a child tag wheter we are rendering side menus or navigation bars.
	 * @return TRUE if rendering a side menu, otherwise FALSE
	 */
	boolean sideMenu() {
		return _sideMenu;
	}
	
	/**
	 * Writes the layout element's opening tag to the JSP output stream.
	 * @return TagSuppport.EVAL_BODY_INCLUDE always
	 * @throws JspException if an error occurs
	 */
	public int doStartTag() throws JspException {
		
		// Check if our screen size is big enough
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
		
		// Render a table for IE6
		try {
			JspWriter out = pageContext.getOut();
			if (_sideMenu)
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
			pageContext.getOut().print("</div>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}
}