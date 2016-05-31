// Copyright 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.time.*;

import javax.servlet.jsp.JspException;

import org.deltava.util.StringUtils;

/**
 * An HTML 5 JSP tag for date input elements. 
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public class DateInputTag extends HTML5InputTag {
	
	private static final String HTML5_DATE_FMT = "yyyy-MM-dd";

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
			if (_value instanceof Instant)
				_value = StringUtils.format((Instant) _value, _dateFmt);
		} else {
			_data.setAttribute("type", "date");
			if (_value instanceof Instant)
				_value = StringUtils.format((Instant) _value, HTML5_DATE_FMT);
		}
		
		return SKIP_BODY;
	}
	
	/**
	 * Sets the maximum date.
	 * @param dt the date/time
	 */
	public void setMax(ZonedDateTime dt) {
		_data.setAttribute("max", StringUtils.format(dt, HTML5_DATE_FMT));
	}
	
	/**
	 * Sets the minimum date.
	 * @param dt the date/time
	 */
	public void setMin(ZonedDateTime dt) {
		_data.setAttribute("min", StringUtils.format(dt, HTML5_DATE_FMT));
	}
	
	/**
	 * Sets the date stepping interval.
	 * @param s the interval in days
	 */
	public void setStep(int s) {
		_data.setAttribute("step", String.valueOf(s));
	}
}