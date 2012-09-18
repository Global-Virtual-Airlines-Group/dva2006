// Copyright 2009, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;
import javax.servlet.http.HttpServletRequest;

import org.deltava.beans.*;

/**
 * A JSP tag to convert and format distances. 
 * @author Luke
 * @version 5.0
 * @since 2.4
 */

public class DistanceFormatTag extends IntegerFormatTag {
	
	private DistanceUnit _unit = DistanceUnit.NM;
	private boolean _longUnits;

	/**
	 * Updates this tag's page context and loads the user object from the request.
     * @param ctxt the new JSP page context
	 */
	@Override
	public final void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		HttpServletRequest req = (HttpServletRequest) ctxt.getRequest();
        java.security.Principal user = req.getUserPrincipal();
        if (user instanceof Person)
        	_unit = ((Person) user).getDistanceType();
	}
	
	/**
	 * Overrides the units to display the distance in.
	 * @param units the unit abbreviation
	 */
	public void setUnits(String units) {
		_unit = DistanceUnit.valueOf(units.toUpperCase());
	}
	
	/**
	 * Sets whether to use long or short unit names.
	 * @param useLong TRUE if long unit names are used, otherwise FALSE
	 */
	public void setLongUnits(boolean useLong) {
		_longUnits = useLong;
	}

	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		_unit = DistanceUnit.NM;
		_longUnits = false;
		super.release();
	}
	
	/**
	 * Formats the number and writes it to the JSP output writer.
     * @return TagSupport.EVAL_PAGE always
     * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
        fmtNoDecimals();
		try {
			JspWriter out = pageContext.getOut();
			if (_className != null) {
                out.print("<span class=\"");
                out.print(_className);
                out.print("\">");
			}
              
			out.print(_nF.format(_value.doubleValue() * _unit.getFactor()));
			out.print(' ');
			out.print(_longUnits ? _unit.getUnitName() : _unit.name().toLowerCase());
			if (_className != null)
                out.print("</span>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}