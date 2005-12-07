// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to generate HTML text field elements.
 * 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InputTag extends FormElementTag {

    /**
     * Create a new Input element tag of type TEXT.
     * @see InputTag#setType(String)
     */
    public InputTag() {
        super("input", true);
        _attrs.put("type", "text");
    }

    /**
     * Generates the HTML for this Input element. If a non-String value is set, this method calls
     * the value's toString() method to render it.
     * @throws JspException if an error occurs
     * @see Object#toString()
     */
    public int doEndTag() throws JspException {
        try {
            validateState();
            _out.print(openHTML(false));
            
            // Write the value
            if (_value != null) {
                _out.print(" value=\"");
                _out.print(StringUtils.stripInlineHTML(String.valueOf(_value)));
                _out.print('\"');
            }
                
            // Close the tag
            _out.print(" />");
        } catch (Exception e) {
            throw new JspException(e);
        }
        
        // Reset state and return
        release();
        return EVAL_PAGE;
    }
    
    /**
     * Resets state variables for this tag.
     */
    public void release() {
        super.release();
        _attrs.put("type", "text");
    }
    
    /**
     * Sets the HTML field type. This is typically either TEXT or HIDDEN. If no type is explictly set, then
     * the TYPE will be rendered as TEXT.
     * @param type the field type
     */
    public void setType(String type) {
        _attrs.put("type", type);
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
     * Sets the maximum length of this field. This does nothing if a negative, zero or non-numeric value is passed.
     * @param maxLen the maximum length of the field
     * @see ElementTag#setNumericAttr(String, int)
     */
    public void setMax(int maxLen) {
        setNumericAttr("maxlength", maxLen);
    }
    
    /**
     * Marks this field as read-only.
     * @param readOnly TRUE if read-only, otherwise FALSE
     */
    public void setReadOnly(boolean readOnly) {
        if (readOnly)
        	_attrs.put("readonly", "readonly");
    }
    
    /**
     * Marks this field as disabled.
     * @param disabled TRUE if disabled, otherwise FALSE
     */
    public void setDisabled(boolean disabled) {
        if (disabled)
            _attrs.put("disabled", "disabled");
    }
    
    /**
     * Sets the JavaScript event for this elements onBlur() event.
     * @param jsCode the JavaScript code
     */
    public void setOnBlur(String jsCode) {
        _attrs.put("onblur", jsCode);
    }
}