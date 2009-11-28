// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.Person;
import org.deltava.beans.schedule.Airport;

/**
 * A JSP tag to determine whether the user wants to display ICAO or IATA airport codes, and saves the result in a page
 * attribute.
 * @author Luke
 * @version 2.7
 * @since 2.7
 */

public class AirportTypeTag extends TagSupport {

	private String _varName;
	private boolean _useICAO = false;

	/**
	 * Sets the page attribute name.
	 * @param attrName the attribute name
	 */
	public void setVar(String attrName) {
		_varName = attrName;
	}

	/**
	 * Sets the tag's JSP context and loads the code type to display from the user's preferences.
	 * @param ctxt the JSP context
	 * @see Person#getAirportCodeType()
	 */
	public final void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		HttpServletRequest req = (HttpServletRequest) ctxt.getRequest();
		Principal user = req.getUserPrincipal();
		if (user instanceof Person) {
			Person p = (Person) user;
			_useICAO = (p.getAirportCodeType() == Airport.ICAO);
		}
	}

	/**
	 * Release's the tag's state variables.
	 */
	public void release() {
		super.release();
		_useICAO = false;
	}

	/**
	 * Writes whether the user wishes to view ICAO codes to a page attribute.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		pageContext.setAttribute(_varName, Boolean.valueOf(_useICAO), PageContext.PAGE_SCOPE);
		release();
		return EVAL_PAGE;
	}
}