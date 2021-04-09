// Copyright 2005, 2007, 2010, 2012, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to generate an XHTML button. Buttons will have a CSS class name of BUTTON unless it is explicitly overridden.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class ButtonTag extends ElementTag {

	/**
	 * Create a new Button element tag.
	 */
	public ButtonTag() {
		super("input");
		_data.setAttribute("type", "button");
	}

	@Override
	public int doStartTag() throws JspException {
		_classes.add("button");
		return super.doStartTag();
	}
	
	/**
	 * Generates the HTML for this button.
	 * @throws JspException if an I/O error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			_out.print(_data.open(true, true));
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
		_data.setAttribute("type", "button");
	}

	/**
	 * Sets the label for this button.
	 * @param label the button label
	 */
	public void setLabel(String label) {
		_data.setAttribute("value", label);
	}

	/**
	 * Sets the button type. This is either SUBMIT or RESET.
	 * @param type the button type
	 */
	public void setType(String type) {
		_data.setAttribute("type", type.toLowerCase());
	}

	/**
	 * Sets the JavaScript code to execute when the button is clicked.
	 * @param js the JavaScript code
	 */
	public void setOnClick(String js) {
		_data.setAttribute("onclick", js);
	}

	/**
	 * Sets the keyboard shortcut for this button.
	 * @param accessKey the Unicode value for this key combination
	 */
	public void setKey(String accessKey) {
		_data.setAttribute("accesskey", accessKey);
	}
	
    /**
     * Sets the tab index of this field. This does nothing if a negative, zero or non-numeric value is passed.
     * @param index the tab index, or * if it should be retrieved from the parent form.
     * @see ElementTag#setNumericAttr(String, int, int)
     */
    public void setIdx(String index) {
        setNumericAttr("tabindex", ("*".equals(index)) ? getFormIndexCount() : StringUtils.parse(index, 0), 1);
    }
}