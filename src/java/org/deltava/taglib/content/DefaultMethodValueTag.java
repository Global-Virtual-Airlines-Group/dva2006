// Copyright 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.lang.reflect.Method;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to call default interface methods since EL is stupid.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class DefaultMethodValueTag extends TagSupport {

	private Object _o;
	private String _methodName;
	
	private String _varName;
	
	/**
	 * Sets the request attribute name.
	 * @param vName the name of the request attribute to store the data in
	 */
	public void setVar(String vName) {
		_varName = vName;
	}

	/**
	 * Sets the object to execute against. 
	 * @param o the Object
	 */
	public void setObject(Object o) {
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
	 * Returns whether the object to executae against is present.
	 * @return TRUE if not null, otherwise FALSE
	 */
	protected boolean hasObject() {
		return (_o != null);
	}
	
	/**
	 * Retrieves the method's return value via reflection.
	 * @return an Object
	 * @throws Exception if an error occurs
	 */
	protected Object getValue() throws Exception {
		
		// Get proper method name
		if (!_methodName.endsWith("()"))
			_methodName = StringUtils.getPropertyMethod(_methodName);
		
		Method m = _o.getClass().getMethod(_methodName);
		if (m == null)
			throw new IllegalArgumentException("Cannot find " + _o.getClass().getName() + "#" + _methodName);
		
		return m.invoke(_o);
	}
	
	@Override
	public int doStartTag() throws JspException {
		if (!hasObject()) return SKIP_BODY;
		
		try {
			pageContext.setAttribute(_varName, getValue(), PageContext.REQUEST_SCOPE);
			return SKIP_BODY;
		} catch (Exception e) {
			throw new JspException(e);
		}			
	}
}