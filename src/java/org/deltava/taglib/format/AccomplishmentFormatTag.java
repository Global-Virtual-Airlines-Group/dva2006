// Copyright 2010, 2016, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.stats.Accomplishment;
import org.deltava.util.StringUtils;

/**
 * A JSP tag to display a Pilot Accomplishment.
 * @author Luke
 * @version 12.3
 * @since 3.2
 */

public class AccomplishmentFormatTag extends TagSupport {

	private Accomplishment _a;
	private String _class;
	
	/**
	 * Sets the Accomplishment to display.
	 * @param a the Accomplishment bean
	 */
	public void setAccomplish(Accomplishment a) {
		_a = a;
	}
	
	/**
	 * Sets the CSS class name to use when displaying this Accomplishment. 
	 * @param className the CSS class name
	 */
	public void setClassName(String className) {
		_class = className;
	}
	
	@Override
	public void release() {
		super.release();
		_class = null;
	}

	@Override
	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			out.print("<span style=\"color:#");
			out.print(_a.getHexColor());
			out.print(";\"");
			if (!StringUtils.isEmpty(_class)) {
				out.print(" class=\"");
				out.print(_class);
				out.print('\"');
			}
			
			out.print('>');
			out.print(_a.getName());
			out.print("</span>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}