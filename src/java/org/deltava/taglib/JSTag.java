// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib;

import javax.servlet.jsp.tagext.TagSupport;

/**
 * An abstract class to support JSP tags that render JavaScript data. 
 * @author Luke
 * @version 2.4
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
	public void release() {
		_jsVarName = null;
		super.release();
	}
}