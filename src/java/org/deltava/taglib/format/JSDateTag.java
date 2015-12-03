// Copyright 2009, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.util.*;

import javax.servlet.jsp.JspException;

import org.deltava.taglib.JSTag;

/**
 * A JSP Tag to format a Date/Time object into a JavaScript date. 
 * @author Luke
 * @version 6.3
 * @since 2.4
 */

public class JSDateTag extends JSTag {
	
	private final Calendar _cld = Calendar.getInstance();
	private boolean _doTime;

	/**
	 * Sets the date/time to format.
	 * @param dt the date/time
	 */
	public void setDate(Date dt) {
		_cld.setTime(dt);
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
        buf.append(_cld.get(Calendar.YEAR));
        buf.append(',');
        buf.append(_cld.get(Calendar.MONTH));
        buf.append(',');
        buf.append(_cld.get(Calendar.DAY_OF_MONTH));
        if (_doTime) {
        	buf.append(',');
        	buf.append(_cld.get(Calendar.HOUR_OF_DAY));
        	buf.append(',');
        	buf.append(_cld.get(Calendar.MINUTE));
        	buf.append(',');
        	buf.append(_cld.get(Calendar.SECOND));
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