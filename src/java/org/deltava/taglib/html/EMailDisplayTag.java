// Copyright 2005, 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.Person;

/**
 * A JSP Tag to display a Pilot's e-mail address.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class EMailDisplayTag extends TagSupport {

	private static final String[] ROLES = {"HR", "Moderator", "PIREP", "Examination", "Event", "Instructor", "Signature"};
	
	private Person _usr;
	
	/**
	 * Updates the Person to display.
	 * @param p the Person
	 */
	public void setUser(Person p) {
		_usr = p;
	}
	
	/**
	 * Renders a link to the user's e-mail address to the JSP output stream
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	public int doStartTag() throws JspException {
		
		// Get the HTTP servlet request
		HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
		if (_usr == null)
			return SKIP_BODY;
		
		// Check if we can display the pilot e-mail address
		boolean canDisplay = false;
		switch (_usr.getEmailAccess()) {
			case Person.SHOW_EMAIL :
				canDisplay = true;
				break;
				
			case Person.AUTH_EMAIL :
				canDisplay = (req.getUserPrincipal() != null);
				break;
				
			case Person.HIDE_EMAIL :
			default :
				for (int x = 0; (!canDisplay) && (x < ROLES.length); x++)
					canDisplay = req.isUserInRole(ROLES[x]);
				
				break;
		}
		
		// Return depending on wether we can display
		return canDisplay ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}
}