// Copyright 2005, 2006, 2008, 2009, 2011, 2013, 2014, 2015, 2016, 2018, 2020, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import javax.servlet.http.*;
import javax.servlet.jsp.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.*;

import org.deltava.taglib.*;

import com.newrelic.api.agent.NewRelic;

/**
 * A JSP tag to render page layouts in a user-specific way.
 * @author Luke
 * @version 12.0
 * @since 1.0
 */

public class PageTag extends BrowserInfoTag {

	private boolean _sideMenu;
	private boolean _isLogged;

	/**
	 * Tells a child tag whether we are rendering side menus or navigation bars.
	 * @return TRUE if rendering a side menu, otherwise FALSE
	 */
	boolean sideMenu() {
		return _sideMenu;
	}
	
	private void logReason(String msg) {
		if (!_sideMenu || _isLogged) return;
		HttpServletResponse rsp = (HttpServletResponse) pageContext.getResponse();
		rsp.setHeader("X-Side-Menu", msg);
		_isLogged = true;
	}

	@Override
	public int doStartTag() throws JspException {

		// Check for IE6 and phones/tablets
		HTTPContextData bctxt = getBrowserContext();
		_sideMenu = (bctxt == null) || (bctxt.getDeviceType() == DeviceType.PHONE) || (bctxt.getDeviceType() == DeviceType.UNKNOWN);
		logReason("Non-desktop/tablet device");
		_sideMenu |= (bctxt != null) && ((bctxt.getBrowserType() == BrowserType.IE) && (bctxt.getMajor() < 9));
		logReason("Internet Explorer < 9");
		
		// Check if our screen size is big enough
		HttpServletRequest hreq = (HttpServletRequest) pageContext.getRequest();
		HttpSession s = hreq.getSession(false);
		if (!_sideMenu && (s != null) && !s.isNew()) {
			Pilot usr = (Pilot) hreq.getUserPrincipal();
			if (usr != null) {
				_sideMenu = !usr.getShowNavBar();
				logReason(usr.getName() + " ShowNavBar = " + String.valueOf(usr.getShowNavBar()));
			}
		}

		try {
			JspWriter out = pageContext.getOut();
			out.println(NewRelic.getBrowserTimingHeader());
			out.print("<div id=\"nav\" class=\"");
			out.print(_sideMenu ? "navside" : "navbar");
			if (bctxt != null) {
				out.print(bctxt.isIPv6() ? " ipv6 " : " ipv4 ");
				out.print(bctxt.getDeviceType().name().toLowerCase());
			}
			
			out.println("\">");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_BODY_INCLUDE;
	}
	
	@Override
	public void release() {
		super.release();
		_isLogged = false;
	}

	@Override
	public int doEndTag() throws JspException {

		ContentHelper.flushCSP(pageContext);
		try {
			pageContext.getOut().println("</div>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}
}