// Copyright 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.Person;

/**
 * An abstract JSP tag class to expose authenticated user data. 
 * @author Luke
 * @version 7.0
 * @since 5.2
 */

abstract class UserSettingsTag extends TagSupport {

	/**
	 * The authenticated user.
	 */
	protected Person _user;
	
	/**
	 * Sets the tag's JSP context and loads the user data.
	 * @param ctxt the JSP context
	 */
	@Override
	public void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		HttpServletRequest req = (HttpServletRequest) ctxt.getRequest();
		Principal user = req.getUserPrincipal();
		_user = (user instanceof Person) ? (Person) user : null;
	}
}