// Copyright 2004, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.text.DecimalFormat;

import javax.servlet.jsp.JspException;

/**
 * A JSP tag to support the rendering of formatted integer values.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IntegerFormatTag extends NumberFormatTag {

    private static final String DEFAULT_PATTERN = "#,###,###,##0";
    
    /**
     * Initializes the tag and the number formatter.
     */
    public IntegerFormatTag() {
        super();
        _nF = new DecimalFormat(DEFAULT_PATTERN);
        _nF.setDecimalSeparatorAlwaysShown(false);
        _nF.setParseIntegerOnly(true);
    }
    
    /**
     * Resets this tag's data when its lifecycle is complete.
     */
    public void release() {
        super.release(DEFAULT_PATTERN);
    }
    
    /**
     * Formats the number and writes it to the JSP output writer.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
     */
    public int doEndTag() throws JspException {
        
        // Check to ensure that the number format string has no decimals
        String pattern = _nF.toPattern();
        if (pattern.indexOf('.') != -1)
            _nF.applyPattern(pattern.substring(0, pattern.indexOf('.')));
        
        return super.doEndTag();
    }
}