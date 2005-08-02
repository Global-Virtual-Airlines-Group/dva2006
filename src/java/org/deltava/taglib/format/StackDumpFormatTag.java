// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to support writing exception Stack Traces. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class StackDumpFormatTag extends TagSupport {

	private Throwable _ex;
	
	/**
	 * Sets the exception to display.
	 * @param t the exception
	 */
	public void setException(Throwable t) {
		_ex = t;
	}

	/**
	 * Renders the exception stack trace to the JSP output stream.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an I/O error occurs
	 */
	public int doEndTag() throws JspException {
		
		JspWriter out = pageContext.getOut();
		try {
			StackTraceElement[] traces = _ex.getStackTrace();
			for (int x = 0; x < traces.length; x++) {
				out.print(traces[x]);
				out.println("<br />");
			}
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		return EVAL_PAGE;
	}
}