// Copyright 2009, 2010, 2012, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.util.*;

import javax.servlet.jsp.JspException;

import org.deltava.taglib.JSTag;

/**
 * A JSP tag to add objects into a JavaScript array.
 * @author Luke
 * @version 6.3
 * @since 2.4
 */

public class JSArrayTag extends JSTag {

	private Collection<Object> _data;

	/**
	 * Sets the items to put into the JavaScript array.
	 * @param items a Collection of objects.
	 */
	public void setItems(Collection<Object> items) {
		_data = items;
	}

	/**
	 * Renders the JavaScript array to the JSP output stream.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {

		StringBuilder buf = new StringBuilder("[");
		if (_data != null) {
			for (Iterator<Object> i = _data.iterator(); i.hasNext();) {
				Object obj = i.next();
				if (obj instanceof Number)
					buf.append(obj.toString());
				else if (obj == null)
					buf.append("null");
				else {
					buf.append('\'');
					buf.append(String.valueOf(obj).replace("\'", "\\\'"));
					buf.append('\'');
				}

				if (i.hasNext())
					buf.append(',');
			}
		}

		buf.append("];");
		try {
			writeVariableName();
			pageContext.getOut().write(buf.toString());
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}
}