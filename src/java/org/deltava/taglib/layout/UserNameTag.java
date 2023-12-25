// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import java.security.Principal;

import javax.servlet.jsp.*;
import javax.servlet.http.HttpServletRequest;

import org.deltava.beans.Pilot;
import org.deltava.beans.econ.EliteStatus;

/**
 * A JSP tag to render the logged in User's name in the menu.
 * @author Luke
 * @version 11.1
 * @since 11.1
 */

public class UserNameTag extends MenuItemTag {

	private Principal _user;
	
	@Override
	public int doStartTag() throws JspException {
		
		// Get the user object
		HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
		_user = req.getUserPrincipal();
		if (_user == null)
			return SKIP_BODY;
		
		// Set color if EliteStatus set
		if (_user instanceof Pilot p) {
			EliteStatus es = p.getEliteStatus();
			if (es != null) {
				setColor(es.getColor());
				setLabel(es.getLevel().getName());
			}
		}
		
		setWidth(_user.getName().length() * 10);
		return super.doStartTag();
	}
	
	@Override
	public int doEndTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();
			out.print(_user.getName());
			return super.doEndTag();
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
	}
	
	@Override
	public void release() {
		super.release();
		_user = null;
	}
}