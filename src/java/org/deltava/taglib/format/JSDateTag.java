// Copyright 2009, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import javax.servlet.jsp.JspException;

import org.deltava.taglib.JSTag;

/**
 * A JSP Tag to format a Date/Time object into a JavaScript date. 
 * @author Luke
 * @version 7.0
 * @since 2.4
 */

public class JSDateTag extends JSTag {
	
	private LocalDateTime _dt = LocalDateTime.now();
	private boolean _doTime;

	/**
	 * Sets the date/time to format.
	 * @param dt the date/time
	 */
	public void setDate(LocalDateTime dt) {
		_dt = dt;
	}
	
	/**
	 * Sets whether the time component of the date should be used.
	 * @param doTime TRUE if the time should be included, otherwise FALSE
	 */
	public void setTime(boolean doTime) {
		_doTime = doTime;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
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
		
        StringBuilder buf = new StringBuilder("new Date(");
        buf.append(_dt.get(ChronoField.YEAR));
        buf.append(',');
        buf.append(_dt.get(ChronoField.MONTH_OF_YEAR));
        buf.append(',');
        buf.append(_dt.get(ChronoField.DAY_OF_MONTH));
        if (_doTime) {
        	buf.append(',');
        	buf.append(_dt.get(ChronoField.HOUR_OF_DAY));
        	buf.append(',');
        	buf.append(_dt.get(ChronoField.MINUTE_OF_HOUR));
        	buf.append(',');
        	buf.append(_dt.get(ChronoField.SECOND_OF_MINUTE));
        }
        
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