// Copyright 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.time.Instant;

import javax.servlet.jsp.JspException;

import org.deltava.util.StringUtils;

/**
 * An HTML 5 JSP tag for time input elements. 
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public class TimeInputTag extends HTML5InputTag {
	
	private static final String HTML5_TIME_FMT = "HH:mm";

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
				_value = StringUtils.format((Instant) _value, _timeFmt);
		} else {
			_data.setAttribute("type", "time");
			if (_value instanceof Instant)
				_value = StringUtils.format((Instant) _value, HTML5_TIME_FMT);
		}
		
		return SKIP_BODY;
	}
	
	/**
	 * Sets the maximum time.
	 * @param dt the time
	 */
	public void setMax(Instant dt) {
		_data.setAttribute("max", StringUtils.format(dt, HTML5_TIME_FMT));
	}
	
	/**
	 * Sets the minimum time.
	 * @param dt the date/time
	 */
	public void setMin(Instant dt) {
		_data.setAttribute("min", StringUtils.format(dt, HTML5_TIME_FMT));
	}
	
	/**
	 * Sets the date stepping interval.
	 * @param s the interval in seconds
	 */
	public void setStep(int s) {
		_data.setAttribute("step", String.valueOf(s));
	}
}