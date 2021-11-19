// Copyright 2009, 2015, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * An abstract class to support JSP tags that render JavaScript data. 
 * @author Luke
 * @version 10.2
 * @since 2.4
 */

public abstract class JSTag extends TagSupport {

	/**
	 * The name of the Javascript variable to create.
	 */
	protected String _jsVarName;
	
	private boolean _isConst;
	
	/**
	 * Sets the JavaScript variable to create.
	 * @param varName the variable name
	 */
	public final void setVar(String varName) {
		_jsVarName = varName;
	}

	/**
	 * Marks the JavaScript variable as a constnt.
	 * @param isConst TRUE if a constant, otherwise FALSE
	 */
	public final void setConst(boolean isConst) {
		_isConst = isConst;
	}
	
	@Override
	public void release() {
		_jsVarName = null;
		_isConst = false;
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
			out.print(_isConst ? "const " : "let ");
		out.print(_jsVarName);
		out.print(" = ");
	}
}