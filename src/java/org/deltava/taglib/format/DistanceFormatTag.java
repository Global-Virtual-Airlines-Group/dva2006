// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;
import javax.servlet.http.HttpServletRequest;

import org.deltava.beans.Person;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to convert and format distances. 
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

public class DistanceFormatTag extends IntegerFormatTag {
	
	private static final String[] ABBRS = {"mi", "nm", "km"};
	
	private int _fmtType;
	private boolean _longUnits;

	/**
	 * Updates this tag's page context and loads the user object from the request.
     * @param ctxt the new JSP page context
	 */
	public final void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		HttpServletRequest req = (HttpServletRequest) ctxt.getRequest();
        java.security.Principal user = req.getUserPrincipal();
        if (user instanceof Person)
        	_fmtType = ((Person) user).getDistanceType();
	}
	
	/**
	 * Overrides the units to display the distance in.
	 * @param units the unit abbreviation
	 */
	public void setUnits(String units) {
		_fmtType = StringUtils.arrayIndexOf(ABBRS, units, 0);
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
		_fmtType = 0;
		_longUnits = false;
		super.release();
	}
	
	/**
	 * Formats the number and writes it to the JSP output writer.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		
		// Convert the units
		double distance = _value.doubleValue();
		if (_fmtType == 1)
			distance *= 0.868976242;
		else if (_fmtType == 2)
			distance *= 1.609344;
		
        fmtNoDecimals();
		JspWriter out = pageContext.getOut();
		try {
			if (_className != null) {
                out.print("<span class=\"");
                out.print(_className);
                out.print("\">");
			}
              
			out.print(_nF.format(distance));
			out.print(' ');
			out.print(_longUnits ? Person.DISTANCE_NAMES[_fmtType] : ABBRS[_fmtType]);
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