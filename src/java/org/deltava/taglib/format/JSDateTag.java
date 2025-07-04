// Copyright 2009, 2015, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.time.*;
import java.time.temporal.ChronoUnit;

import javax.servlet.jsp.JspException;

import org.deltava.taglib.JSTag;

/**
 * A JSP Tag to format a Date/Time object into a JavaScript date. 
 * @author Luke
 * @version 9.1
 * @since 2.4
 */

public class JSDateTag extends JSTag {
	
	private ZonedDateTime _dt = ZonedDateTime.now(ZoneOffset.UTC);
	private boolean _doTime;

	/**
	 * Sets the date/time to format.
	 * @param dt the date/time
	 */
	public void setDate(ZonedDateTime dt) {
		_dt = dt;
	}
	
	/**
	 * Sets whether the time component of the date should be used.
	 * @param doTime TRUE if the time should be included, otherwise FALSE
	 */
	public void setTime(boolean doTime) {
		_doTime = doTime;
	}
	
	@Override
	public void release() {
		_doTime = false;
		super.release();
	}
	
	/**
	 * Renders the JavaScript date to the JSP output stream.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		
		if (!_doTime)
			_dt = _dt.truncatedTo(ChronoUnit.DAYS);
		
        StringBuilder buf = new StringBuilder("new Date(");
        buf.append(_dt.toEpochSecond() * 1000);
        buf.append(");");
        try {
        	writeVariableName();
        	pageContext.getOut().write(buf.toString());
        } catch (Exception e) {
        	throw new JspException(e);
        } finally {
        	release();
        }
        
        return EVAL_PAGE;
	}
}