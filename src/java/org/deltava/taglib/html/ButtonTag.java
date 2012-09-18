// Copyright 2005, 2007, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

/**
 * A JSP tag to generate an XHTML button. Buttons will have a CSS class name of
 * BUTTON unless it is explicitly overridden.
 * @author Luke
 * @version 5.0
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

	/**
	 * Ensures that the button contains the CSS <b>button</b> class.
	 * @return SKIP_BODY always
	 * @throws JspException if an error occurs
	 */
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

	/**
	 * Releases state and readies the tag for another invocation.
	 */
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
}