// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * A JSP tag to create a singleton collection.
 * @author Luke
 * @version 5.1
 * @since 5.1
 */

public class SingletonTag extends SimpleTagSupport {
	
	private String _var;
	private Object _value;
	
	/**
	 * Sets the page attribute to store the result in.
	 * @param varName the page attribute name
	 */
	public void setVar(String varName) {
		_var = varName;
	}
	
	/**
	 * Sets the value.
	 * @param o the value
	 */
	public void setValue(Object o) {
		_value = o;
	}

	/**
	 * Saves the collection in the request.  
	 */
	@Override
	public void doTag() {
		List<Object> v = new ArrayList<Object>(2);
		if (_value != null)
			v.add(_value);
		
		JspContext ctx = getJspContext();
		ctx.setAttribute(_var, v, PageContext.PAGE_SCOPE);
	}
}