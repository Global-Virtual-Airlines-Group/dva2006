// Copyright 2004, 2007, 2009, 2010, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.JspException;

/**
 * A JSP tag to support the rendering of formatted integer values.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public class IntegerFormatTag extends NumberFormatTag {

    private static final String DEFAULT_PATTERN = "#,###,###,##0";
    
    /**
     * Initializes the tag and the number formatter.
     */
    public IntegerFormatTag() {
        super(DEFAULT_PATTERN);
        _nF.setParseIntegerOnly(true);
    }
    
    /**
     * Resets this tag's data when its lifecycle is complete.
     */
    @Override
	public void release() {
        super.release(DEFAULT_PATTERN);
    }
    
    /**
     * Sets the value to format. <i>This is a nasty hack for Tomcat 6.0.18+ EL casting.</i>
     * @param value the value to format
     */
    public void setValue(Long value) {
        super.setValue(value);
    }
    
    /**
     * Helper method to ensure that the formatter does not include decimals.
     */
    protected void fmtNoDecimals() {
    	String pattern = _nF.toPattern();
        if (pattern.indexOf('.') != -1)
            _nF.applyPattern(pattern.substring(0, pattern.indexOf('.')));
    }
    
    /**
     * Formats the number and writes it to the JSP output writer.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
     */
    @Override
	public int doEndTag() throws JspException {
        fmtNoDecimals();
        return super.doEndTag();
    }
}