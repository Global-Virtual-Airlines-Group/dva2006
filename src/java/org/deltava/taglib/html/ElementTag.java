// Copyright 2005, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.util.*;

import javax.servlet.jsp.*;

import org.deltava.taglib.*;
import org.deltava.util.StringUtils;

/**
 * A class for supporting JSP Tags that render HTML elements.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public abstract class ElementTag extends BrowserInfoTag {

	protected final XMLRenderer _data;
	protected final Collection<String> _classes = new LinkedHashSet<String>();
	protected JspWriter _out;

	/**
	 * Creates a new HTML element tag with a given HTML element type.
	 * @param elementName the HTML element type (eg. BODY, FORM, INPUT, etc.)
	 */
	protected ElementTag(String elementName) {
		super();
		_data = new XMLRenderer(elementName);
	}

	/**
	 * Resets this tag's data when its lifecycle is complete.
	 */
	@Override
	public void release() {
		super.release();
		_classes.clear();
		_data.clear();
	}

	/**
	 * Returns the type of HTML element this tag generated.
	 * @return the HTML element type
	 */
	protected String getName() {
		return _data.getName();
	}

	/**
	 * Sets the ID of this HTML element.
	 * @param id the element ID
	 */
	public void setID(String id) {
		_data.setAttribute("id", id);
	}

	/**
	 * Sets the CSS class name of this HTML element.
	 * @param cName the class name as refered to in a CSS file.
	 */
	public void setClassName(String cName) {
		_classes.addAll(StringUtils.split(cName, " "));
	}
	
    /**
     * Associates a CSS style with this HTML element.
     * @param style the CSS
     */
    public void setStyle(String style) {
    	_data.setAttribute("style", style);
    }
    
	/**
	 * Sets a numeric attribute.
	 * @param attrName the attribute name
	 * @param value the value
	 * @param minValue the minimum value for the attribute
	 */
	protected void setNumericAttr(String attrName, int value, int minValue) {
		if (value >= minValue)
			_data.setAttribute(attrName, String.valueOf(value));
	}

	/**
	 * Updates this tag's page context and its JSP output writer.
	 * @param ctxt the new JSP page context
	 */
	@Override
	public void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		_out = ctxt.getOut();
	}

	/**
	 * Executed post tag setup. Concatenates CSS classes into a single string.
	 * @returns EVAL_BODY_INCLUDE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();
		if (!_classes.isEmpty())
			_data.setAttribute("class", StringUtils.listConcat(_classes, " "));
		
		return EVAL_BODY_INCLUDE;
	}
}