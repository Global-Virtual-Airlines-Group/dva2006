package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

/**
 * A JSP tag to generate HTML textbox elements.
 * @author Luke
 * @version 1.0
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
            _out.print(openHTML(true));
        } catch (Exception e) {
            throw wrap(e);
        }
        
        return EVAL_BODY_INCLUDE;
    }
    
    /**
     * Closes this TEXTAREA element by writing a &gt;/TEXTAREA&lt; tag.
     * @throws JspException if an I/O error occurs
     */
    public int doEndTag() throws JspException {
        try {
            _out.print(closeHTML());
        } catch (Exception e) {
            throw wrap(e);
        }
        
        // Clear state and return
        release();
        return EVAL_PAGE;
    }
    
    /**
     * Sets the width of the textbox. This does nothing if a negative, zero or non-numeric value is passed.
     * @param width the width of the textbox in columns
     */
    public void setWidth(String width) {
        setNumericAttr("cols", width);
    }
    
    /**
     * Sets the wrapping property for this textbox.
     * @param wrapType the wrap type, PHYSICAL or VIRTUAL
     */
    public void setWrap(String wrapType) {
        _attrs.put("wrap", wrapType);
    }
    
    /**
     * Sets the height of the textbox. This does nothing if a negative, zero or non-numeric value is passed.
     * @param height the height of the textbox in rows
     */
    public void setHeight(String height) {
        setNumericAttr("rows", height);
    }
    
    /**
     * Marks this textbox as read-only.
     * @param readOnly TRUE if read-only, otherwise FALSE
     */
    public void setReadOnly(boolean readOnly) {
        if (readOnly)
            _attrs.put("readonly", "readonly");
    }
    
    /**
     * Marks this textbox as disabled.
     * @param disabled TRUE if disabled, otherwise FALSE
     */
    public void setDisabled(boolean disabled) {
        if (disabled)
            _attrs.put("disabled", "disabled");
    }
    
    /**
     * Sets code to execute on this textbox's JavaScript onBlur() event.
     * @param jsCode the JavaScript code
     */
    public void setOnBlur(String jsCode) {
       _attrs.put("onblur", jsCode);
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