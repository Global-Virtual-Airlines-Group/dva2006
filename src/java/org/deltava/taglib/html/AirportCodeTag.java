// Copyright 2015, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.security.Principal;

import javax.servlet.jsp.*;
import javax.servlet.http.HttpServletRequest;

import org.deltava.beans.Person;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.schedule.Airport.Code;

/**
 * A JSP tag to render airport code text boxes.
 * @author Luke
 * @version 8.1
 * @since 6.0
 */

public class AirportCodeTag extends InputTag {
	
	private Airport _a;
	private String _comboName;
	private Code _codeType;
	
	/**
	 * Sets the airport selection combobox name.
	 * @param fName the combobox name
	 */
	public void setCombo(String fName) {
		_comboName = fName;
	}
	
	/**
	 * Sets the airport choice.
	 * @param a an Airport, or null
	 */
	public void setAirport(Airport a) {
		_a = a;
	}
	
	/**
	 * Fetches the airport code type from the authenticated user, if present.
	 * @param ctxt the PageContext
	 */
	@Override
	public void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		Principal user = ((HttpServletRequest) ctxt.getRequest()).getUserPrincipal();
		Person p = (user instanceof Person) ? (Person)user : null;
		_codeType = (p == null) ? Code.IATA : p.getAirportCodeType();
	}

	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		_a = null;
		_value = null; // Parent assumes this is mandatory, but for this tag it is not
		super.release();
	}
	
	/**
	 * Sets default tag options and opens the tag.
	 * @return EVAL_BODY_INCLUDE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doStartTag() throws JspException {
		_classes.add("caps");
		setName(_comboName + "Code");
		setMax((_codeType == Code.IATA) ? 3 : 4);
		setSize(3);
		setAutoComplete(false);
		if (_a != null)
			_value = (_codeType == Code.IATA) ? _a.getIATA() : _a.getICAO();
		
		// Set default events and render the tag
		setOnChange("void this.form." + _comboName + ".setAirport(this.value, true, this)");
		setOnKeypress("void golgotha.airportLoad.codeMassage()");
		return super.doStartTag();
	}
}