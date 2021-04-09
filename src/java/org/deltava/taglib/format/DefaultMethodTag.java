// Copyright 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.JspException;

import org.deltava.taglib.content.DefaultMethodValueTag;

/**
 * A JSP tag to call default interface methods since EL is stupid.
 * @author Luke
 * @version 10.0
 * @since 9.0
 */

public class DefaultMethodTag extends DefaultMethodValueTag {

	private String _value;
	private String _empty;
	
	/**
	 * Sets the text to render if the object is null.
	 * @param v the value
	 */
	public void setEmpty(String v) {
		_empty = v;
	}

	@Override
	public int doStartTag() throws JspException {
		try {
			_value = hasObject() ? String.valueOf(getValue()) : _empty;
			return SKIP_BODY;
		} catch (Exception e) {
			throw new JspException(e);
		}			
	}
	
	@Override
	public int doEndTag() throws JspException {
		try {
			pageContext.getOut().write(_value);
			return EVAL_PAGE;
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
	}
	
	@Override
	public void release() {
		super.release();
		_empty = null;
	}
}