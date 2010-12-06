// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.util.Arrays;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to insert an Enumeration's values into a page attribute.
 * @author Luke
 * @version 3.4
 * @since 3.2
 */

public class EnumTag extends TagSupport {

	private String _varName;
	private String _className;
	private String _itemName;
	
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
	 * Sets the enumeration item name.
	 * @param iName the item name or null if all values requested
	 */
	public void setItem(String iName) {
		_itemName = iName;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_itemName = null;
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

			// If a specific item requested, fetch it
			Object[] values = c.getEnumConstants();
			if (_itemName != null) {
				for (int x = 0; x < values.length; x++) {
					Object value = values[x];
					if (_itemName.equals(value.toString())) {
						pageContext.setAttribute(_varName, value, PageContext.PAGE_SCOPE);
						break;
					}
				}
			} else
				pageContext.setAttribute(_varName, Arrays.asList(values), PageContext.PAGE_SCOPE);
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}