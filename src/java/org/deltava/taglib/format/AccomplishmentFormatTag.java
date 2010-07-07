// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.stats.Accomplishment;

/**
 * A JSP tag to display a Pilot Accomplishment.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class AccomplishmentFormatTag extends TagSupport {

	private Accomplishment _a;
	
	/**
	 * Sets the Accomplishment to display.
	 * @param a the Accomplishment bean
	 */
	public void setAccomplish(Accomplishment a) {
		_a = a;
	}
	
	/**
	 * Renders the Accomplishment to the JSP output stream.
	 * @return EVAL_PAGE always
	 */
	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			out.print("<span style=\"color:#");
			out.print(Integer.toHexString(_a.getColor()).toLowerCase());
			out.print(";\">");
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