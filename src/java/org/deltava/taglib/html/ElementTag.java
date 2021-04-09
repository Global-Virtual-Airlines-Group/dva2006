// Copyright 2005, 2010, 2011, 2012, 2015, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.*;
import org.deltava.util.StringUtils;

/**
 * A class for supporting JSP Tags that render HTML elements.
 * @author Luke
 * @version 10.0
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
     * Returns the parent form tag.
     * @return the parent FormTag, or null
     */
    protected FormTag getParentFormTag() {
    	return (FormTag) TagSupport.findAncestorWithClass(this, FormTag.class);
    }
    
    /**
     * Gets and increments the current tab index count for the parent form tag.
     * @return the tabIndex
     */
    protected int getFormIndexCount() {
    	FormTag parent = getParentFormTag();
    	return (parent == null) ? 0 : parent.incTabIndex();
    }

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
		if (cName != null)
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

	@Override
	public void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		_out = ctxt.getOut();
	}

	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();
		if (!_classes.isEmpty())
			_data.setAttribute("class", StringUtils.listConcat(_classes, " "));
		
		return EVAL_BODY_INCLUDE;
	}
}