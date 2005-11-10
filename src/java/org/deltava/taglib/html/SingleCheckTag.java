// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.html;

import java.io.IOException;
import javax.servlet.jsp.*;

/**
 * A JSP tag to support the generation of HTML single-option checkboxes.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SingleCheckTag extends FormElementTag {

	private String _label;
	private String _labelClassName;

	/**
	 * Creates a new checkbox tag.
	 */
	public SingleCheckTag() {
		super("input", false);
		_attrs.put("type", "checkbox");
		_attrs.put("class", "check");
	}

	/**
	 * Sets the CSS class name <i>for the checkbox label text </i>.
	 * @param cName the CSS class name(s)
	 * @see ElementTag#setClassName(String)
	 */
	public final void setClassName(String cName) {
		_labelClassName = cName;
	}

	/**
	 * Sets the checkbox label text.
	 * @param label the label text
	 */
	public void setLabel(String label) {
		_label = label;
	}
	
	/**
	 * Sets the checkbox value/alias.
	 * @param objValue the alias
	 */
	public final void setValue(Object objValue) {
		_attrs.put("value", String.valueOf(objValue));
	}

	/**
	 * Sets if the checkbox is selected.
	 * @param checked
	 */
	public void setChecked(boolean checked) {
		if (checked)
			_attrs.put("checked", "checked");
	}
	
	/**
	 * Sets the JavaScript <i>onChange</i> event for these checkboxes/radio buttons.
	 * @param jsEvent the JavaScript event code
	 */
	public void setOnChange(String jsEvent) {
		_attrs.put("onchange", jsEvent);
	}

	/**
	 * Releases the tag's state data.
	 */
	public void release() {
		super.release();
		_attrs.put("type", "checkbox");
		_attrs.put("class", "check");
	}

	/**
	 * Renders the checkbox to the JSP output stream.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an I/O error occurs
	 */
	public int doEndTag() throws JspException {
		try {
			validateState();
			if (_labelClassName != null) {
				_out.print("<span class=\"");
				_out.print(_labelClassName);
				_out.print("\">");
			}
			
			// Open the tag and print the label
			_out.print(openHTML(false));
			_out.print(" />");
			_out.print(_label);
			
			// Close the span if it is opened
			if (_labelClassName != null)
				_out.print("</span>");
		} catch (IOException ie) {
			throw new JspException(ie);
		}

		release();
		return EVAL_PAGE;
	}
}