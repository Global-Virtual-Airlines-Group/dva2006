// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP Tag to set an attribute based on role membership.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class RoleAttributeTag extends TagSupport {

	private final Collection<String> _roles = new HashSet<String>();
	private String _attrName;
	private Object _value;

	/**
	 * Sets the request attribute name.
	 * @param attrName the attribute name
	 */
	public void setAttr(String attrName) {
		_attrName = attrName;
	}

	/**
	 * Sets the request attribute value.
	 * @param attrValue the attribute value
	 */
	public void setValue(Object attrValue) {
		_value = attrValue;
	}

	/**
	 * Sets the role(s) a user must belong to in order to view the body of this tag. Use * (asterisk) for all roles, and
	 * a role name prefaced by an ! (exclamation mark) for the lack of role memebrship.
	 * @param roles a comma-delimited list of authorized role names
	 */
	public void setRoles(String roles) {
		StringTokenizer tkns = new StringTokenizer(roles, ",");
		while (tkns.hasMoreTokens())
			_roles.add(tkns.nextToken());
	}

	/**
	 * Clears state by reseting the role list.
	 */
	public void release() {
		_roles.clear();
		super.release();
	}

	/**
	 * Checks for the roles listed and adds the attribute to the request if found.
	 * @return SKIP_BODY always
	 */
	public int doStartTag() {

		// Get the request
		HttpServletRequest hreq = (HttpServletRequest) pageContext.getRequest();

		// Check if the user has any of the roles listed in our role section
		boolean hasRole = false;
		for (Iterator<String> i = _roles.iterator(); i.hasNext() && !hasRole;) {
			String role = i.next();
			if (role.charAt(0) == '!') {
				if (!hreq.isUserInRole(role.substring(1)))
					hasRole = true;
			} else {
				if (hreq.isUserInRole(role))
					hasRole = true;
			}
		}

		// If we have the role, add the attribute
		if (hasRole)
			hreq.setAttribute(_attrName, _value);

		return SKIP_BODY;
	}

	/**
	 * Closes the JSP and releases state.
	 * @return EVAL_PAGE always
	 */
	public int doEndTag() {
		release();
		return EVAL_PAGE;
	}
}