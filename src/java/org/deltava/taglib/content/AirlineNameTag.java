// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to write the Airline name.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirlineNameTag extends TagSupport {

	/**
	 * Renders the Airline name to the JSP output stream.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if an I/O error occurs
	 */
	public int doEndTag() throws JspException {
		try {
			pageContext.getOut().print(SystemData.get("airline.name"));
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		return EVAL_PAGE;
	}
}