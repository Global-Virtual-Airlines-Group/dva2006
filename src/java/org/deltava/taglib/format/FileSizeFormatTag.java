// Copyright 2022, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import jakarta.servlet.jsp.*;

/**
 * A JSP tag to format file sizes. 
 * @author Luke
 * @version 12.4
 * @since 10.3
 */

public class FileSizeFormatTag extends IntegerFormatTag {

	private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB", "PB"};
	
	private int _unit;
	private boolean _showBytes = true;
	
	public void setShowBytes(boolean showBytes) {
		_showBytes = showBytes;
	}
	
   /**
     * Sets the value to format. <i>This is a nasty hack for Tomcat 6.0.18+ EL casting.</i>
     * @param value the value to format
     */
	@Override
    public void setValue(Long value) {
		final Long v = (value == null) ? Long.valueOf(0) : value;
		double l10 = Math.log10(v.doubleValue());
    	_unit = Math.max(0, Math.min((int)Math.round((l10 / 3) - 1), UNITS.length - 1));
        super.setValue(Long.valueOf(Math.round(v.longValue() / Math.pow(10, (_unit * 3)))));
    }
	
	@Override
	public void release() {
		_showBytes = true;
		super.release();
	}
    
	@Override
    public int doEndTag() throws JspException {
		fmtNoDecimals();
    	try {
    		JspWriter out = pageContext.getOut();
    		openSpan();
    		printValue();
    		if (_showBytes || (_unit > 0)) out.print(UNITS[_unit]);
    		closeSpan();
    	} catch (Exception e) {
    		throw new JspException(e);
    	} finally {
    		release();
    	}
    	
    	return EVAL_PAGE;
    }
}