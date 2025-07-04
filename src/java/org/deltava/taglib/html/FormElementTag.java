// Copyright 2004, 2005, 2006, 2010, 2011, 2012, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.PageContext;

import org.deltava.util.StringUtils;

/**
 * A class for supporting JSP tags that generate HTML input elements.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public abstract class FormElementTag extends ElementTag {

    /**
     * The value of this input element.
     */ 
    protected Object _value;
    private final boolean _validateParent;
    
    /**
     * Generates an input element tag with a given name.
     * @param elementName the HTML element name
     * @param validateParent TRUE if parent validation is enabled, otherwise FALSE
     * @see FormElementTag#validateState()
     */
    protected FormElementTag(String elementName, boolean validateParent) {
        super(elementName);
        _validateParent = validateParent;
    }
    
    /**
     * Sets the name of this field.
     * @param name the field name
     */
    public void setName(String name) {
        _data.setAttribute("name", name);
    }
    
    /**
     * Sets the tab index of this field. This does nothing if a negative, zero or non-numeric value is passed.
     * @param index the tab index, or * if it should be retrieved from the parent form.
     * @see ElementTag#setNumericAttr(String, int, int)
     */
    public void setIdx(String index) {
        setNumericAttr("tabindex", ("*".equals(index)) ? getFormIndexCount() : StringUtils.parse(index, 0), 1);
    }
    
    /**
     * Sets the value of this input element. Each implementer needs to render multiple values appropriately. 
     * @param value the value(s) for this field
     */
    public void setValue(Object value) {
        _value = value;
    }
    
    /**
     * Sets this field as required for HTML5 browsers.
     * @param isRequired TRUE if required, otherwise FALSE
     */
    public void setRequired(boolean isRequired) {
    	_data.setAttribute("required", null);
    	_classes.add("req");
    }
    
    /**
     * Sets multiple values for this input element.
     * @param values a comma-delimited list of values
     */
    public void setDelimValues(String values) {
    	_value = StringUtils.split(values, ",");
    }
    
    /**
     * Validates tag state.
     * @throws IllegalStateException if not in a FORM and is required
     */
    protected void validateState() {
       if (_validateParent) {
    	   boolean isConditional = (pageContext.getAttribute("isForm", PageContext.REQUEST_SCOPE) != null);
           if ((getParentFormTag() == null) && (!isConditional))
               throw new IllegalStateException(getName() + " must be contained within a FORM tag");
       }
    }
}