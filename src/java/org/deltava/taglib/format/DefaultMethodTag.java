// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.lang.reflect.Method;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to call default interface methods since EL is stupid.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class DefaultMethodTag extends TagSupport {

	private Object _o;
	private String _methodName;

	/**
	 * Sets the object to execute against. 
	 * @param o the Object
	 */
	public void setVar(Object o) {
		_o = o;
	}

	/**
	 * Sets the method name to find and call.
	 * @param methodName the method name, minus get or set
	 */
	public void setMethod(String methodName) {
		_methodName = methodName;
	}
	
	/**
	 * Prints the results of the specified method to the JSP output stream.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs 
	 */
	@Override
	public int doEndTag() throws JspException {
		
		// Get proper method name
		if (!_methodName.endsWith("()"))
			_methodName = StringUtils.getPropertyMethod(_methodName);
		
		try {
			Method m = _o.getClass().getMethod(_methodName);
			if (m == null)
				throw new IllegalArgumentException("Cannot find " + _o.getClass().getName() + "#" + _methodName);
			
			// Execute and output
			Object r = m.invoke(_o);
			pageContext.getOut().write(String.valueOf(r));
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}