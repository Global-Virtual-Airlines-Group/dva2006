// Copyright 2010, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to insert an Enumeration's values into a page attribute.
 * @author Luke
 * @version 6.3
 * @since 3.2
 */

public class EnumTag extends TagSupport {

	private String _varName;
	private String _className;
	private String _itemName;
	
	private final Collection<String> _filterNames = new HashSet<String>();
	
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
	 * Sets the name of the enumeration entries to exclude.
	 * @param names a comma-seprated list of enumeration names
	 */
	public void setExclude(String names) {
		_filterNames.addAll(StringUtils.split(names, ","));
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_itemName = null;
		_filterNames.clear();
	}
	
	/**
	 * Loads the Enumeration and stores its values in a page attribute.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs or the className cannot be loaded
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			Class<?> c = Class.forName(_className);
			if (!c.isEnum())
				throw new IllegalArgumentException(c.getName() + " is not an Enumeration");

			// If a specific item requested, fetch it
			Object[] values = c.getEnumConstants();
			if (_itemName != null) {
				for (int x = 0; x < values.length; x++) {
					Object e = values[x];
					if (_itemName.equals(e.toString())) {
						pageContext.setAttribute(_varName, e, PageContext.PAGE_SCOPE);
						break;
					}
				}
			} else if (_filterNames.size() > 0) {
				Collection<Object> objs = Arrays.asList(values).stream().filter(e -> !_filterNames.contains(e.toString())).collect(Collectors.toList());
				pageContext.setAttribute(_varName, objs, PageContext.PAGE_SCOPE);
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