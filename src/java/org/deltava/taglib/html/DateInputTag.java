// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.util.Date;
import java.text.*;

import javax.servlet.jsp.JspException;

import org.deltava.util.StringUtils;

/**
 * An HTML 5 JSP tag for date input elements. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class DateInputTag extends HTML5InputTag {
	
	private final DateFormat HTML5_DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * Creates an e-mail tag if executing in an HTML5 browser.
	 * @return SKIP_BODY always
	 * @throws JspException if an error occurs 
	 */
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();

		// Check for HTML5 browser
		if (!isHTML5()) {
			removeHTML5Attributes();
			if (_value instanceof Date)
				_value = StringUtils.format((Date) _value, _dateFmt);
		} else {
			_data.setAttribute("type", "date");
			if (_value instanceof Date)
				_value = HTML5_DATE_FMT.format(_value);
		}
		
		return SKIP_BODY;
	}
	
	/**
	 * Sets the maximum date.
	 * @param dt the date/time
	 */
	public void setMax(Date dt) {
		_data.setAttribute("max", HTML5_DATE_FMT.format(dt));
	}
	
	/**
	 * Sets the minimum date.
	 * @param dt the date/time
	 */
	public void setMin(Date dt) {
		_data.setAttribute("min", HTML5_DATE_FMT.format(dt));
	}
	
	/**
	 * Sets the date stepping interval.
	 * @param s the interval in days
	 */
	public void setStep(int s) {
		_data.setAttribute("step", String.valueOf(s));
	}
}