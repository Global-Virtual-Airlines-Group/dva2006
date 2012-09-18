// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.util.Date;
import java.text.*;

import javax.servlet.jsp.JspException;

import org.deltava.util.StringUtils;

/**
 * An HTML 5 JSP tag for time input elements. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class TimeInputTag extends HTML5InputTag {
	
	private final DateFormat HTML5_TIME_FMT = new SimpleDateFormat("HH:mm");

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
				_value = StringUtils.format((Date) _value, _timeFmt);
		} else {
			_data.setAttribute("type", "time");
			if (_value instanceof Date)
				_value = HTML5_TIME_FMT.format(_value);
		}
		
		return SKIP_BODY;
	}
	
	/**
	 * Sets the maximum time.
	 * @param dt the time
	 */
	public void setMax(Date dt) {
		_data.setAttribute("max", HTML5_TIME_FMT.format(dt));
	}
	
	/**
	 * Sets the minimum time.
	 * @param dt the date/time
	 */
	public void setMin(Date dt) {
		_data.setAttribute("min", HTML5_TIME_FMT.format(dt));
	}
	
	/**
	 * Sets the date stepping interval.
	 * @param s the interval in seconds
	 */
	public void setStep(int s) {
		_data.setAttribute("step", String.valueOf(s));
	}
}