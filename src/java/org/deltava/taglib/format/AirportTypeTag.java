// Copyright 2009, 2010, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;

import org.deltava.beans.Person;
import org.deltava.beans.schedule.Airport;

/**
 * A JSP tag to determine whether the user wants to display ICAO or IATA airport codes, and saves the result in a page
 * attribute.
 * @author Luke
 * @version 7.0
 * @since 2.7
 */

public class AirportTypeTag extends UserSettingsTag {

	private String _varName;
	private boolean _useICAO;

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
	@Override
	public final void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		_useICAO = (_user != null) ? (_user.getAirportCodeType() == Airport.Code.ICAO) : false;
	}

	/**
	 * Writes whether the user wishes to view ICAO codes to a page attribute.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		pageContext.setAttribute(_varName, Boolean.valueOf(_useICAO), PageContext.PAGE_SCOPE);
		release();
		return EVAL_PAGE;
	}
}