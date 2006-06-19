// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved. 
package org.deltava.taglib.html;

import javax.servlet.jsp.*;

import org.deltava.taglib.ContentHelper;

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
		_data.setAttribute("type", "checkbox");
		_data.setAttribute("class", "check");
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
		_data.setAttribute("value", String.valueOf(objValue));
	}

	/**
	 * Sets if the checkbox is selected.
	 * @param checked
	 */
	public void setChecked(boolean checked) {
		if (checked)
			_data.setAttribute("checked", "checked");
	}
	
	/**
	 * Sets the JavaScript <i>onChange</i> event for these checkboxes/radio buttons.
	 * @param jsEvent the JavaScript event code
	 */
	public void setOnChange(String jsEvent) {
		_data.setAttribute(ContentHelper.isIE6(pageContext) ? "onclick" : "onchange", jsEvent);
	}

	/**
	 * Releases the tag's state data.
	 */
	public void release() {
		super.release();
		_data.setAttribute("type", "checkbox");
		_data.setAttribute("class", "check");
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
			_out.print(_data.open(true, true));
			_out.print(_label);
			
			// Close the span if it is opened
			if (_labelClassName != null)
				_out.print("</span>");
		} catch (Exception e) {
			throw new JspException(e);
		}

		release();
		return EVAL_PAGE;
	}
}