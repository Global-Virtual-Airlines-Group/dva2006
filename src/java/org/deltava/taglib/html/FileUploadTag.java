// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

/**
 * A JSP tag to generate a FILE tag.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FileUploadTag extends FormElementTag {

    /**
     * Creates a new file upload element tag.
     */
    public FileUploadTag() {
        super("input", true);
        _attrs.put("type", "file");
    }

    /**
     * Sets the size of this field. This does nothing if a negative, zero or non-numeric value is passed.
     * @param len the size of the field
     * @see ElementTag#setNumericAttr(String, int)
     */
    public void setSize(int len) {
        setNumericAttr("size", len);
    }
    
    /**
     * Sets the JavaScript onChange event for this field.
     * @param jscript the JavaScript to execute when the field value changes
     */
    public void setOnChange(String jscript) {
    	_attrs.put("onchange", jscript);
    }
    
    /**
     * Sets the maximum length of this field. This does nothing if a negative, zero or non-numeric value is passed.
     * @param maxLen the maximum length of the field
     * @see ElementTag#setNumericAttr(String, int)
     */
    public void setMax(int maxLen) {
        setNumericAttr("maxlength", maxLen);
    }
    
    /**
     * Resets state variables for this tag.
     */
    public void release() {
        super.release();
        _attrs.put("type", "file");
    }
    
    /**
     * Generates the HTML for this Input element.
     * @throws JspException if an error occurs
     */
    public int doEndTag() throws JspException {
        try {
            validateState();
            _out.print(openHTML(false));
            _out.print(" />");
        } catch (Exception e) {
            throw new JspException(e);
        }
        
        // Reset state and return
        release();
        return EVAL_PAGE;
    }
}