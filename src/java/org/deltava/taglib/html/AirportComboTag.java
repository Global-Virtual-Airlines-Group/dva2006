// Copyright 2005 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.util.Iterator;

import org.deltava.beans.Person;
import org.deltava.beans.ComboAlias;
import org.deltava.beans.schedule.Airport;

import javax.servlet.jsp.JspException;
import javax.servlet.http.HttpServletRequest;

import org.deltava.util.ComboUtils;

/**
 * A JSP tag to support generating HTML Airport combo/list boxes.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirportComboTag extends ComboTag {

	private int _codeType = Airport.IATA;

	/**
	 * Helper method to convert an airport into a ComboAlias object with the proper code.
	 */
	private ComboAlias convertAirport(Airport a) {
		String code = (_codeType == Airport.IATA) ? a.getIATA() : a.getICAO();
		return ComboUtils.fromString(a.getName() + " (" + code + ")", code);
	}
	
	/**
	 * Reads the user's Airport code type setting.
	 * @return TagSupport.SKIP_BODY always
	 * @throws JspException if an error occurs
	 */
	public int doStartTag() throws JspException {
		HttpServletRequest hreq = (HttpServletRequest) pageContext.getRequest();
		Person usr = (Person) hreq.getUserPrincipal();
		if (usr != null)
			_codeType = usr.getAirportCodeType();
		
		return SKIP_BODY;
	}
	
	/**
	 * Writes the Airport Combobox to the JSP output stream.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		try {
			validateState();
			_out.println(openHTML(true));
			
			// Render the first entry if present
			if (_firstEntry != null)
				renderOption(convertAirport((Airport) _firstEntry));

			// Render the options
			if (_options != null) {
				for (Iterator i = _options.iterator(); i.hasNext();) {
					Airport a = (Airport) i.next();
					renderOption(convertAirport(a));
				}
			}

			_out.println(closeHTML());
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		// Release state and return
		release();
		return EVAL_PAGE;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		_codeType = Airport.IATA;
		super.release();
	}
}