// Copyright 2005, 2006, 2009 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to generate HTML textbox elements.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class TextboxTag extends FormElementTag {

    /**
     * Creates a new textbox element tag.
     */
    public TextboxTag() {
        super("textarea", true);
    }

    /**
     * Opens this TEXTAREA element by writing a &gt;TEXTAREA&lt; tag.
     * @throws JspException if an error occurs
     */
    public int doStartTag() throws JspException {
        try {
            validateState();
            _out.print(_data.open(true));
        } catch (Exception e) {
            throw new JspException(e);
        }
        
        return EVAL_BODY_INCLUDE;
    }
    
    /**
     * Closes this TEXTAREA element by writing a &gt;/TEXTAREA&lt; tag.
     * @throws JspException if an I/O error occurs
     */
    public int doEndTag() throws JspException {
        try {
            _out.print(_data.close());
        } catch (Exception e) {
            throw new JspException(e);
        } finally {
        	release();
        }
        
        return EVAL_PAGE;
    }
    
    /**
     * Sets the width of the textbox. This does nothing if a negative, zero or non-numeric value is passed.
     * @param width the width of the textbox in columns, or as a percentage
     */
    public void setWidth(String width) {
    	if ((width != null) && (width.endsWith("%")))
    		_data.setAttribute("style", "width:" + width + ";");
    	else
    		setNumericAttr("cols", StringUtils.parse(width, 0), 0);	
    }
    
    /**
     * Sets the wrapping property for this textbox.
     * @param wrapType the wrap type, PHYSICAL or VIRTUAL
     */
    public void setWrap(String wrapType) {
        _data.setAttribute("wrap", wrapType);
    }
    
    /**
     * Sets the height of the textbox. This does nothing if a negative, zero or non-numeric value is passed.
     * @param height the height of the textbox in rows
     */
    public void setHeight(int height) {
        setNumericAttr("rows", height, 0);
    }
    
    /**
     * Marks this textbox as read-only.
     * @param readOnly TRUE if read-only, otherwise FALSE
     */
    public void setReadOnly(boolean readOnly) {
        if (readOnly)
            _data.setAttribute("readonly", "readonly");
    }
    
    /**
     * Marks this textbox as disabled.
     * @param disabled TRUE if disabled, otherwise FALSE
     */
    public void setDisabled(boolean disabled) {
        if (disabled)
            _data.setAttribute("disabled", "disabled");
    }
    
    /**
     * Sets code to execute on this textbox's JavaScript onBlur() event.
     * @param jsCode the JavaScript code
     */
    public void setOnBlur(String jsCode) {
       _data.setAttribute("onblur", jsCode);
    }
    
    /**
     * Sets the JavaScript event for this element's onChange() event.
     * @param jsCode the JavaScript code
     */
    public void setOnChange(String jsCode) {
    	_data.setAttribute("onchange", jsCode);
    }
    
    /**
     * Sets the JavaScript event for this element's onFocus() event.
     * @param jsCode the JavaScript code
     */
    public void setOnFocus(String jsCode) {
    	_data.setAttribute("onfocus", jsCode);
    }
    
    /**
     * Updates the value for this textarea. This is unsupported since the value of textareas is contained within
     * their body.
     * @throws UnsupportedOperationException always
     */
    public final void setValue(Object obj) {
        throw new UnsupportedOperationException("Value not Supported in TextBoxTag");
    }
}