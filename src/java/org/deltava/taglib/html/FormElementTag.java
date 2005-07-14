package org.deltava.taglib.html;

import java.util.*;

import javax.servlet.jsp.tagext.TagSupport;

/**
 * A class for supporting JSP tags that generate HTML input elements.
 * 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class FormElementTag extends ElementTag {

    /**
     * The value of this input element.
     */ 
    protected Object _value;
    
    private boolean _validateParent;
    
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
     * Private helper method to return the parent form tag.
     */
    private FormTag getParentFormTag() {
    	return (FormTag) TagSupport.findAncestorWithClass(this, FormTag.class);
    }
    
    /**
     * Private helper method to get and increment the current tab index count for the parent form tag.
     */
    private String getFormIndexCount() {
    	FormTag parent = getParentFormTag();
    	return (parent == null) ? "" : String.valueOf(parent.incTabIndex());
    }
    
    /**
     * Sets the name of this field.
     * @param name the field name
     */
    public void setName(String name) {
        _attrs.put("name", name);
    }
    
    /**
     * Sets the tab index of this field. This does nothing if a negative, zero or non-numeric value is passed.
     * @param index the tab index, or * if it should be retrieved from the parent form
     * @see ElementTag#setNumericAttr(String, String)
     */
    public void setIdx(String index) {
        setNumericAttr("tabindex", ("*".equals(index)) ? getFormIndexCount() : index);
    }
    
    /**
     * Sets the value of this input element. Each implementer needs to render multiple values appropriately. 
     * @param value the value(s) for this field
     */
    public void setValue(Object value) {
        _value = value;
    }
    
    /**
     * Sets multiple values for this input element.
     * @param values a comma-delimited list of values
     */
    public void setDelimValues(String values) {
        StringTokenizer tkns = new StringTokenizer(values, ",");
        List l = new ArrayList();
        while (tkns.hasMoreTokens())
            l.add(tkns.nextToken());
        
        _value = l;
    }
    
    /**
     * Validates that this tag has a NAME attribute. All Form elements must have a name.
     * @throws IllegalStateException if the NAME attribute is not set
     */
    protected void validateState() throws IllegalStateException {
       if (!(_attrs.containsKey("name")))
           throw new IllegalStateException("Form Element must contain NAME");
       
       if (_validateParent) {
           if (getParentFormTag() == null)
               throw new IllegalStateException(getName() + " must be contained within a FORM tag");
       }
    }
}