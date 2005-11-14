package org.deltava.taglib.html;

import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A class for supporting JSP Tags that render HTML elements.
 * 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class ElementTag extends TagSupport {

    protected static final Object NO_VALUE = new Object();
    private String _name;
    
    protected Map _attrs;
    protected JspWriter _out;
    
    /**
     * Creates a new HTML element tag with a given HTML element type.
     * @param elementName the HTML element type (eg. BODY, FORM, INPUT, etc.)
     */
    protected ElementTag(String elementName) {
        super();
        _name = elementName;
        _attrs = new LinkedHashMap(); // This is a linked hash map to make testing easier; it returns elements in predictable order
    }
    
    /**
     * Resets this tag's data when its lifecycle is complete.
     */
    public void release() {
        _attrs.clear();
    }
    
    /**
     * Returns the type of HTML element this tag generated. (eg. FORM, IMG, INPUT)
     * @return the HTML element type
     */
    protected String getName() {
        return _name;
    }
    
    /**
     * Sets the ID of this HTML element.
     * @param id the element ID
     */
    public void setID(String id) {
        _attrs.put("id", id);
    }
    
    /**
     * Sets the CSS class name of this HTML element.
     * @param cName the class name as refered to in a CSS file.
     */
    public void setClassName(String cName) {
        _attrs.put("class", cName);
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
     * Generates HTML for this element's opening tag
     * @param closeTag TRUE if the opening tag should be closed with an angle bracket (&gt;)
     * @return the element's opening tag 
     */
    protected String openHTML(boolean closeTag) {
        StringBuilder buf = new StringBuilder();
        
        // Start with the element name
        buf.append('<');
        buf.append(_name);
        
        // Append a space if we have attributes
        if (_attrs.size() > 0)
            buf.append(' ');
        
        // Loop through the attributes
        for (Iterator i = _attrs.keySet().iterator(); i.hasNext(); ) {
            String attrName = (String) i.next();
            Object attrValue = _attrs.get(attrName);
            
            // Append the attribute name
            buf.append(attrName);
            
            // If the attribute has a non-null value, append it
            if (attrValue != NO_VALUE) {
                buf.append("=\"");
                buf.append(attrValue);
                buf.append('\"');
            }
            
            // If there's another attribute, add a space
            if (i.hasNext())
                buf.append(' ');
        }
        
        // Close the tag if requested
        if (closeTag)
            buf.append('>');
        
        // Return the HTML
        return buf.toString();
    }
    
    /**
     * Generates HTML for this element's closing tag.
     * @return the element's closing tag
     */
    protected String closeHTML() {
        StringBuilder buf = new StringBuilder();
        buf.append("</");
        buf.append(_name);
        buf.append('>');
        return buf.toString();
    }
    
    /**
     * Sets a numeric attribute. If the raw value is non-numeric or negative, the attribute will
     * <u>NOT</u> be set and this method will fail silently.
     * @param attrName the attribute name
     * @param rawValue the raw value from the JSP
     */
    protected void setNumericAttr(String attrName, String rawValue) {
        try {
            int tmpValue = Integer.parseInt(rawValue);
            if (tmpValue > 0)
                _attrs.put(attrName, String.valueOf(tmpValue));
        } catch (NumberFormatException nfe) { }
    }
}