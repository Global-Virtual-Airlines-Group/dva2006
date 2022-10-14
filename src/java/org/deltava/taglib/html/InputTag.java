// Copyright 2005, 2008, 2009, 2012, 2015, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to generate HTML text field elements.
 * @author Luke
 * @version 10.3
 * @since 1.0
 */

public class InputTag extends FormElementTag {

    /**
     * Create a new Input element tag of type TEXT.
     * @see InputTag#setType(String)
     */
    public InputTag() {
        super("input", true);
        _data.setAttribute("type", "text");
    }

    /**
     * Generates the HTML for this Input element. If a non-String value is set, this method calls
     * the value's toString() method to render it.
     * @throws JspException if an error occurs
     * @see Object#toString()
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            validateState();
            _out.print(_data.open(false));
            
            // Write the value
            if (_value != null) {
                _out.print(" value=\"");
                _out.print(StringUtils.stripInlineHTML(String.valueOf(_value)));
                _out.print('\"');
            }
                
            _out.print(" />");
        } catch (Exception e) {
            throw new JspException(e);
        } finally {
        	release();
        }
        
        return EVAL_PAGE;
    }
    
    @Override
    public void release() {
        super.release();
        _data.setAttribute("type", "text");
    }
    
    /**
     * Sets the HTML field type. This is typically either TEXT or HIDDEN. If no type is explictly set, then
     * the TYPE will be rendered as TEXT.
     * @param type the field type
     */
    public void setType(String type) {
        _data.setAttribute("type", type);
    }
    
    /**
     * Sets the size of this field. This does nothing if a negative, zero or non-numeric value is passed.
     * @param len the size of the field
     * @see ElementTag#setNumericAttr(String, int, int)
     */
    public void setSize(int len) {
        setNumericAttr("size", len, 1);
    }
    
    /**
     * Sets the maximum length of this field. This does nothing if a negative, zero or non-numeric value is passed.
     * @param maxLen the maximum length of the field
     * @see ElementTag#setNumericAttr(String, int, int)
     */
    public void setMax(int maxLen) {
        setNumericAttr("maxlength", maxLen, 1);
    }

    /**
     * Disables or enables autocomplete support for this field.
     * @param isAutoComplete TRUE if AutoComplete enabled, otherwise FASE
     */
    public void setAutoComplete(boolean isAutoComplete) {
    	_data.setAttribute("autocomplete", isAutoComplete ? "on" : "off");
    }
    
    /**
     * Sets the autofill token for this field. This will automatically enable autocomplete.
     * @param token the autofill token
     */
    public void setAutofill(String token) {
    	_data.setAttribute("autocomplete", token);
    }
    
    /**
     * Marks this field as read-only.
     * @param readOnly TRUE if read-only, otherwise FALSE
     */
    public void setReadOnly(boolean readOnly) {
    	_data.setAttribute("readonly", String.valueOf(readOnly));
    }
    
    /**
     * Marks this field as disabled.
     * @param disabled TRUE if disabled, otherwise FALSE
     */
    public void setDisabled(boolean disabled) {
    	_data.setAttribute("disabled", String.valueOf(disabled));
    }
    
    /**
     * Marks this field as spellcheckable.
     * @param sc TRUE if spellcheck enabled, otherwise FALSE
     */
    public void setSpellcheck(boolean sc) {
    	_data.setAttribute("spellcheck", String.valueOf(sc));
    }
    
    /**
     * Sets the placeholder for this field.
     * @param ph the placeholder string
     */
    public void setPlaceholder(String ph) {
    	_data.setAttribute("placeholder", ph);
    }
    
    /**
     * Sets the JavaScript event for this element's onBlur() event.
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
     * Sets the JavaScript event for this element's onKeyUp() event.
     * @param jsCode the JavaScript code
     */
    public void setOnKeyup(String jsCode) {
    	_data.setAttribute("onkeyup", jsCode);
    }
    
    /**
     * Sets the JavaScript event for this element's onKeyPress() event.
     * @param jsCode the JavaScript code
     */
    public void setOnKeypress(String jsCode) {
    	_data.setAttribute("onkeypress", jsCode);
    }
    
	/**
	 * Sets the Javascript to execute when the element is right-clicked.
	 * @param js the JavaScript code to execute
	 */
	public void setOnRightClick(String js) {
		_data.setAttribute("oncontextmenu", js);
	}
}