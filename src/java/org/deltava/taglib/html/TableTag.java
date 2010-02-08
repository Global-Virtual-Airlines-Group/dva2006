// Copyright 2005, 2010 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to support HTML tables and options not rendered properly in CSS.
 * @author Luke
 * @version 3.0
 * @since 1.0
 */

public class TableTag extends ElementTag {

    private static final String DEFAULT = "default";

    /**
     * Creates a new Table tag.
     */
    public TableTag() {
        super("table");
    }

    /**
     * Sets the CELLSPACING value for this table.
     * @param cSpacing the cellspacing attribute value.
     * @see ElementTag#setNumericAttr(String, int, int)
     */
    public void setSpace(String cSpacing) {
        if (DEFAULT.equalsIgnoreCase(cSpacing))
            setNumericAttr("cellspacing", SystemData.getInt("html.table.spacing", 0), 0);
        else
            setNumericAttr("cellspacing", Integer.parseInt(cSpacing), 0);
    }

    /**
     * Sets the CELLPADDING value for this table.
     * @param cPadding the cellpadding attribute value.
     * @see ElementTag#setNumericAttr(String, int, int)
     */
    public void setPad(String cPadding) {
        if (DEFAULT.equalsIgnoreCase(cPadding))
            setNumericAttr("cellpadding", SystemData.getInt("html.table.padding", 0), 0);
        else
            setNumericAttr("cellpadding", Integer.parseInt(cPadding), 0);
    }
    
    /**
     * Sets the WIDTH value for this table.
     * @param width the width attribute value
     */
    public void setWidth(String width) {
        _data.setAttribute("width", width);
    }
    
    /**
     * Associates a CSS style with this table.
     * @param style the CSS
     */
    public void setStyle(String style) {
    	_data.setAttribute("style", style);
    }

    /**
     * Opens this TABLE element by writing a &gt;TABLE&lt; tag.
     * @throws JspException if an error occurs
     */
    public int doStartTag() throws JspException {
        try {
            _out.print(_data.open(true));
        } catch (Exception e) {
            throw new JspException(e);
        }
        
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Closes this TABLE element by writing a &gt;/TABLE&lt; tag.
     * @throws JspException if an I/O error occurs
     */
    public int doEndTag() throws JspException {
        try {
            _out.print(_data.close());
        } catch (Exception e) {
            throw new JspException(e);
        } finally {
        	release();	
        }
        
        return EVAL_PAGE;
    }
}