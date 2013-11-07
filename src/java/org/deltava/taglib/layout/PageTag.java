// Copyright 2005, 2006, 2008, 2009, 2011, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import javax.servlet.http.*;
import javax.servlet.jsp.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.*;

import org.deltava.commands.CommandContext;

import org.deltava.taglib.BrowserInfoTag;

/**
 * A JSP tag to render page layouts in a user-specific way.
 * @author Luke
 * @version 5.2
 * @since 1.0
 */

public class PageTag extends BrowserInfoTag {

	private boolean _sideMenu;

	/**
	 * Tells a child tag whether we are rendering side menus or navigation bars.
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
	@Override
	public int doStartTag() throws JspException {

		// Check for IE6 and phones/tablets
		HTTPContextData bctxt = getBrowserContext();
		_sideMenu = (bctxt == null) || (bctxt.getDeviceType() != DeviceType.DESKTOP);
		_sideMenu |= ((bctxt.getBrowserType() == BrowserType.IE) && (bctxt.getMajor() < 8));

		// Check if our screen size is big enough
		HttpServletRequest hreq = (HttpServletRequest) pageContext.getRequest();
		HttpSession s = hreq.getSession(false);
		if (!_sideMenu && (s != null)) {
			Pilot usr = (Pilot) hreq.getUserPrincipal();
			if (usr == null) {
				Number sX = (Number) s.getAttribute(CommandContext.SCREENX_ATTR_NAME);
				_sideMenu = (sX == null) || (sX.intValue() < 1155);
			} else
				_sideMenu = !usr.getShowNavBar();
		}

		// Render the div
		try {
			JspWriter out = pageContext.getOut();
			out.print("<div class=\"");
			out.print(_sideMenu ? "navside" : "navbar");
			out.print("\">");
		} catch (Exception e) {
			throw new JspException(e);
		}

		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Writes the layout element's closing tag to the JSP output stream.
	 * @return TagSuppport.EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			pageContext.getOut().print("</div>");
		} catch (Exception e) {
			throw new JspException(e);
		}

		return EVAL_PAGE;
	}
}