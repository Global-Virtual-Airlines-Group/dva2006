// Copyright 2005, 2006, 2009, 2010, 2012, 2015, 2021 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to generate HTML textbox elements.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public class TextboxTag extends FormElementTag {
	
	private String _width;
	private boolean _resize;

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
    @Override
    public int doStartTag() throws JspException {
    	super.doStartTag();
    	
    	// If resizing, enable the Javascript
    	if (_resize && ContentHelper.containsContent(pageContext, "JS", "common")) {
    		_data.setAttribute("onkeyup", "void golgotha.form.resize(this)");
    		StringBuilder buf = new StringBuilder();
    		if (_data.has("class"))
    			buf.append(_data.get("class")).append(' ');
    		
    		buf.append("resizable");
    		_data.setAttribute("class", buf.toString());
    	}
    	
    	// If width set, adjust style as required
    	if ((_width != null) && (_width.endsWith("%"))) {
    		if (_data.has("style")) {
    			StringBuilder buf = new StringBuilder(_data.get("style"));
    			buf.append("; width:");
    			buf.append(_width);
    			buf.append(';');
    			_data.setAttribute("style", buf.toString());
    		} else
    			_data.setAttribute("style", "width:" + _width + ";");
    	} else
    		setNumericAttr("cols", StringUtils.parse(_width, 0), 0);	
    	
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
    @Override
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
    
    @Override
    public void release() {
    	super.release();
    	_resize = false;
    	_width = null;
    }
    
    /**
     * Sets the width of the textbox. This does nothing if a negative, zero or non-numeric value is passed.
     * @param width the width of the textbox in columns, or as a percentage
     */
    public void setWidth(String width) {
    	_width = width;
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
    	_data.setAttribute("readonly", String.valueOf(readOnly));
    }
    
    /**
     * Marks this textbox as disabled.
     * @param disabled TRUE if disabled, otherwise FALSE
     */
    public void setDisabled(boolean disabled) {
    	_data.setAttribute("disabled", String.valueOf(disabled));
    }
    
    /**
     * Marks this textbox as spellcheckable.
     * @param sc TRUE if spellcheck enabled, otherwise FALSE
     */
    public void setSpellcheck(boolean sc) {
    	_data.setAttribute("spellcheck", String.valueOf(sc));
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
	 * Sets the Javascript to execute when the element is right-clicked.
	 * @param js the JavaScript code to execute
	 */
	public void setOnRightClick(String js) {
		_data.setAttribute("oncontextmenu", js);
	}
    
    /**
     * Sets whether the textbox should have auto-resize code enabled.
     * @param doResize TRUE if auto-resize enabled, otherwise FALSE
     */
    public void setResize(boolean doResize) {
    	_resize = doResize;
    }
    
    /**
     * Updates the value for this textarea. This is unsupported since the value of textareas is contained within
     * their body.
     * @throws UnsupportedOperationException always
     */
    @Override
    public final void setValue(Object obj) {
        throw new UnsupportedOperationException("Value not Supported in TextBoxTag");
    }
}