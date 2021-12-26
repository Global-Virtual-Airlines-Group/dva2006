// Copyright 2005, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

/**
 * A JSP tag to generate a FILE tag.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public class FileUploadTag extends FormElementTag {
	
	private int _maxSizeKB;

	/**
	 * Creates a new file upload element tag.
	 */
	public FileUploadTag() {
		super("input", true);
		_data.setAttribute("type", "file");
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
	 * Sets the JavaScript onChange event for this field.
	 * @param jscript the JavaScript to execute when the field value changes
	 */
	public void setOnChange(String jscript) {
		_data.setAttribute("onchange", jscript);
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
	 * Sets the maximum size for the file. This will <b>NOT</b> provide validation code, instead places a small label
	 * to the right of the input box with the size limit displayed.
	 * @param kb the size limit in kilobytes
	 */
	public void setMaxSize(int kb) {
		_maxSizeKB = kb;
	}

	@Override
	public void release() {
		super.release();
		_data.setAttribute("type", "file");
		_maxSizeKB = 0;
	}

	/**
	 * Generates the HTML for this Input element.
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			validateState();
			_out.print(_data.open(true, true));
			if (_maxSizeKB > 0) {
				_out.print("&nbsp;<span class=\"small ita\">(Maximum Size: ");
				_out.print(_maxSizeKB);
				_out.print("KB)</span>");
			}
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}
}