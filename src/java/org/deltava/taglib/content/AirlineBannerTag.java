// Copyright 2007, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.Person;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag to set the name of the Airline banner image.
 * @author Luke
 * @version 7.0
 * @since 2.0
 */

public class AirlineBannerTag extends TagSupport {
	
	private String _attrName;
	private String _scheme;
	
	/**
	 * Sets the name of the HTTP request attribute to store the image name in.
	 * @param attr the request attribute name
	 */
	public void setVar(String attr) {
		_attrName = attr;
	}
	
	/**
	 * Loads the UI scheme name from the user object, if present.
	 * @param ctxt the JSP page context
	 */
	@Override
	public final void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		HttpServletRequest req = (HttpServletRequest) ctxt.getRequest();
		Principal user = req.getUserPrincipal();
		if (user instanceof Person) {
			String scheme = ((Person) user).getUIScheme();
			if ((scheme != null) && (!scheme.equals(InsertCSSTag.DEFAULT_SCHEME)))
				_scheme = scheme.toLowerCase().replace(' ', '_');
		}
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_scheme = null;
	}
	
	/**
	 * Renders the Airline name to the JSP output stream.
	 * @return TagSupport.EVAL_PAGE always
	 */
	@Override
	public int doEndTag() {
		try {
			if (_scheme != null) {
				String banner = SystemData.get("airline.banner");
				StringBuilder buf = new StringBuilder(banner.substring(0, banner.lastIndexOf('.')));
				buf.append('_');
				buf.append(_scheme);
				buf.append(banner.substring(banner.lastIndexOf('.')));
				pageContext.setAttribute(_attrName, buf.toString(), PageContext.PAGE_SCOPE);
			} else
				pageContext.setAttribute(_attrName, SystemData.get("airline.banner"), PageContext.PAGE_SCOPE);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}