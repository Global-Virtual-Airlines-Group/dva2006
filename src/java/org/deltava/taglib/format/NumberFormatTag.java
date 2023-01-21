// Copyright 2004, 2007, 2009, 2013, 2016, 2019, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.text.*;

import javax.servlet.jsp.*;

/**
 * A JSP tag to support the rendering of formatted numeric values.
 * @author Luke
 * @version 10.4
 * @since 1.0
 */

abstract class NumberFormatTag extends UserSettingsTag {

    protected final DecimalFormat _nF;
    protected Number _value;
    private String _className;
    protected String _zeroValue;
    
    /**
     * Initializes the tag.
     * @param pattern the formatting pattern to use
     */
    public NumberFormatTag(String pattern) {
        super();
        _nF = new DecimalFormat(pattern);
        _nF.setDecimalSeparatorAlwaysShown(false);
    }
    
    /**
     * Updates the CSS class for this formatted number. This will automatically enclose the output in a
     * &lt;SPAN&gt; tag.
     * @param cName the class Name(s)
     */
    public final void setClassName(String cName) {
        _className = cName;
    }
    
    /**
     * Updates the date format pattern.
     * @param pattern the format pattern string
     * @see DecimalFormat#applyPattern(String)
     */
    public final void setFmt(String pattern) {
        _nF.applyPattern(pattern);
    }
    
    /**
     * Updates the content used if the value is zero.
     * @param v the value
     */
    public final void setZero(String v) {
    	_zeroValue = v;
    }
    
    /**
     * Sets the value to format.
     * @param value the value to format
     */
    public final void setValue(Number value) {
        _value = value;
    }
    
    /**
     * Returns a StringBuilder with the class name to allow subclasses to modify the css classes for the SPAN element.
     * @return a StringBuilder
     */
    protected StringBuilder getClassNameBuilder() {
    	StringBuilder buf = new StringBuilder();
    	if (_className != null)
    		buf.append(_className).append(' ');
    	
    	return buf;
    }
    
    /**
     * Releases the tag's state and resets the format pattern string. 
     * @param pattern the new format pattern
     */
    protected void release(String pattern) {
        super.release();
        _className = null;
        _zeroValue = null; 
        _nF.applyPattern(pattern);
    }
    
    /**
     * Updates this tag's page context and loads the user object from the request.
     * @param ctxt the new JSP page context
     */
    @Override
	public void setPageContext(PageContext ctxt) {
        super.setPageContext(ctxt);
        if (_user != null)
            _nF.applyPattern(_user.getNumberFormat());
    }
    
    /**
     * Checks that a non-null value has been provided.
     * @return SKIP_BODY always
     * @throws JspException never
     */
    @Override
	public int doStartTag() throws JspException {
    	if (_value == null)
    		_value = Integer.valueOf(0);
    	
    	return SKIP_BODY;
    }
    
    /**
     * Opens an optional formatting SPAN element.
     * @throws Exception if an I/O error occurs
     */
    protected void openSpan() throws Exception {
    	if (_className == null) return;
    	JspWriter out = pageContext.getOut();
    	out.print("<span class=\"");
        out.print(_className);
        out.print("\">");
    }

    /**
     * Closes an optional formatting SPAN element.
     * @throws Exception if an I/O error occurs
     */
    protected void closeSpan() throws Exception {
    	if (_className != null)
    		pageContext.getOut().print("</span>");
    }
    
    /**
     * Prints the value, or the zero value if set and the value is zero.
     * @throws Exception if an I/O error occurs
     */
    protected void printValue() throws Exception {
    	pageContext.getOut().print(((_value.longValue() == 0) && (_zeroValue != null)) ? _zeroValue : _nF.format(_value.doubleValue()));
    }
    
    /**
     * Formats the number and writes it to the JSP output writer.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
     */
    @Override
	public int doEndTag() throws JspException {
        try {
        	openSpan();
        	printValue();
        	closeSpan();
        } catch (Exception e) {
            throw new JspException(e);
        } finally {
        	release();
        }
        
        return EVAL_PAGE;
    }
}