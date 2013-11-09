// Copyright 2009, 2012, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;

import org.deltava.beans.*;

/**
 * A JSP tag to convert and format unit values. 
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public abstract class UnitFormatTag extends IntegerFormatTag {
	
	protected Unit _unit;
	private boolean _longUnits;

	/**
	 * Overrides the units to display the value in.
	 * @param units the unit abbreviation
	 */
	public abstract void setUnits(String units);
	
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
	@Override
	public void release() {
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
			if (_longUnits)
				out.print('s');
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