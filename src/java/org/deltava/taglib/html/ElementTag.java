// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.XMLRenderer;

/**
 * A class for supporting JSP Tags that render HTML elements.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class ElementTag extends TagSupport {

	protected XMLRenderer _data;
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
	public void release() {
		_data.clear();
	}

	/**
	 * Returns the type of HTML element this tag generated. (eg. FORM, IMG, INPUT)
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
		_data.setAttribute("class", cName);
	}

	/**
	 * Updates this tag's page context and its JSP output writer.
	 * @param ctxt the new JSP page context
	 */
	public final void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		_out = ctxt.getOut();
	}

	/**
	 * Sets a numeric attribute.
	 * @param attrName the attribute name
	 * @param value the raw value from the JSP
	 * @deprecated
	 */
	protected void setNumericAttr(String attrName, int value) {
		_data.setAttribute(attrName, String.valueOf(value));
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
}