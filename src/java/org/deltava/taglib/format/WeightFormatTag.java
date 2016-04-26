// Copyright 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.PageContext;

import org.deltava.beans.*;

/**
 * A JSP tag to convert and format weights. 
 * @author Luke
 * @version 7.0
 * @since 5.2
 */

public class WeightFormatTag extends UnitFormatTag {

	/**
	 * Updates this tag's page context and loads the user object from the request.
     * @param ctxt the new JSP page context
	 */
	@Override
	public final void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		_unit = (_user != null) ? _user.getWeightType() : WeightUnit.LB;
	}
	
	/**
	 * Overrides the units to display the weight in.
	 * @param units the unit abbreviation
	 */
	@Override
	public void setUnits(String units) {
		_unit = WeightUnit.valueOf(units.toUpperCase());
	}
}