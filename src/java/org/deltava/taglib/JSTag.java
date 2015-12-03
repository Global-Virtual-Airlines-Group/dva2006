// Copyright 2009, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * An abstract class to support JSP tags that render JavaScript data. 
 * @author Luke
 * @version 6.3
 * @since 2.4
 */

public abstract class JSTag extends TagSupport {

	/**
	 * The name of the Javascript variable to create.
	 */
	protected String _jsVarName;
	
	/**
	 * Sets the JavaScript variable to create.
	 * @param varName the variable name
	 */
	public final void setVar(String varName) {
		_jsVarName = varName;
	}
	
	/**
	 * Resets the tag's state variables.
	 */
	@Override
	public void release() {
		_jsVarName = null;
		super.release();
	}
	
	/**
	 * Writes the variable name to the JSP output stream
	 * @throws IOException 
	 */
	protected void writeVariableName() throws IOException {
		if (_jsVarName == null) return;
		JspWriter out = pageContext.getOut();
		if (_jsVarName.indexOf('.') == -1)
			out.print("var ");
		out.print(_jsVarName);
		out.print(" = ");
	}
}