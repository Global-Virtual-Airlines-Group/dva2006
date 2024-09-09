// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.JspException;

import org.deltava.beans.EnumDescription;
import org.deltava.taglib.content.DefaultMethodValueTag;

/**
 * A JSP tag to display enumeration descriptions. 
 * @author Luke
 * @version 11.2
 * @since 11.2
 */

public class EnumDescriptionTag extends DefaultMethodValueTag {

	/**
	 * Sets the object to execute against. 
	 * @param e the EnumDescription
	 */
	public void setObject(EnumDescription e) {
		super.setObject(e);
	}
	
	@Override
	public int doStartTag() throws JspException {
		setMethod("description");
		return SKIP_BODY;
	}
	
	@Override
	public int doEndTag() throws JspException {
		try {
			Object v = getValue();
			if (v != null)
				pageContext.getOut().write(String.valueOf(getValue()));
			return EVAL_PAGE;
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
	}
}