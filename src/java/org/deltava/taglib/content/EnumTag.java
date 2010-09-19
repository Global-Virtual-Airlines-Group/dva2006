// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.util.Arrays;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to insert an Enumeration's values into a page attribute.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class EnumTag extends TagSupport {

	private String _varName;
	private String _className;
	
	/**
	 * Sets the page attribute name.
	 * @param vName the name of the attribute to store the data in
	 */
	public void setVar(String vName) {
		_varName = vName;
	}
	
	/**
	 * Sets the Enumeration class name.
	 * @param cName the class name
	 */
	public void setClassName(String cName) {
		_className = cName;
	}
	
	/**
	 * Loads the Enumeration and stores its values in a page attribute.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs or the className cannot be loaded
	 */
	public int doEndTag() throws JspException {
		try {
			Class<?> c = Class.forName(_className);
			if (!c.isEnum())
				throw new IllegalArgumentException(c.getName() + " is not an Enumeration");
			
			// Convert to a list and save in the page
			pageContext.setAttribute(_varName, Arrays.asList(c.getEnumConstants()), PageContext.PAGE_SCOPE);
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		return EVAL_PAGE;
	}
}