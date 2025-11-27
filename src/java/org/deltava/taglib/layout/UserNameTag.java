// Copyright 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import java.security.Principal;

import jakarta.servlet.jsp.*;
import jakarta.servlet.http.HttpServletRequest;

import org.deltava.beans.Pilot;
import org.deltava.beans.econ.*;

/**
 * A JSP tag to render the logged in User's name in the menu.
 * @author Luke
 * @version 12.3
 * @since 11.1
 */

public class UserNameTag extends MenuItemTag {
	
	private boolean _isAnonymous;

	@Override
	public int doStartTag() throws JspException {
		
		// Get the user object
		HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
		Principal user = req.getUserPrincipal();
		_isAnonymous = (user == null);
		if (user == null)
			return SKIP_BODY;
		
		// Set color if EliteStatus set
		setWidth(user.getName().length() * 10);
		if (user instanceof Pilot p) {
			EliteStatus es = p.getEliteStatus();
			if (es != null) {
				setColor(es.getColor());
				if (es instanceof LifetimeStatus ls)
					setLabel(String.format("%s (%s)", ls.getLifetimeName(), ls.getLevel().getName()));
				else
					setLabel(es.getLevel().getName());
			}
		}
		
		try {
			JspWriter out = pageContext.getOut();
			super.doStartTag();
			out.print(user.getName());
			return SKIP_BODY;
		} catch (Exception e) {
			throw new JspException(e);
		}
	}
	
	@Override
	public int doEndTag() throws JspException {
		if (_isAnonymous) {
			release();
			return EVAL_PAGE;
		}
		
		return super.doEndTag();
	}
}