// Copyright 2005, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to support writing exception Stack Traces. 
 * @author Luke
 * @version 7.0
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
	@Override
	public int doEndTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();
			StackTraceElement[] traces = _ex.getStackTrace();
			for (int x = 0; x < traces.length; x++)
				out.println(traces[x]);
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		return EVAL_PAGE;
	}
}