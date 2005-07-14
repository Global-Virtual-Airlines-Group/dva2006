package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to support HTML tables and options not rendered properly in CSS.
 * @author Luke
 * @version 1.0
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
     */
    public void setSpace(String cSpacing) {
        if (DEFAULT.equalsIgnoreCase(cSpacing)) {
            setNumericAttr("cellspacing", SystemData.get("html.table.spacing"));
        } else {
            setNumericAttr("cellspacing", cSpacing);
        }
    }

    /**
     * Sets the CELLPADDING value for this table.
     * @param cPadding the cellpadding attribute value.
     */
    public void setPad(String cPadding) {
        if (DEFAULT.equalsIgnoreCase(cPadding)) {
            setNumericAttr("cellpadding", SystemData.get("html.table.padding"));
        } else {
            setNumericAttr("cellpadding", cPadding);
        }
    }
    
    /**
     * Sets the WIDTH value for this table.
     * @param width the width attribute value
     */
    public void setWidth(String width) {
        _attrs.put("width", width);
    }

    /**
     * Opens this TABLE element by writing a &gt;TABLE&lt; tag.
     * @throws JspException if an error occurs
     */
    public int doStartTag() throws JspException {
        try {
            _out.print(openHTML(true));
        } catch (Exception e) {
            throw wrap(e);
        }
        
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Closes this TABLE element by writing a &gt;/TABLE&lt; tag.
     * @throws JspException if an I/O error occurs
     */
    public int doEndTag() throws JspException {
        try {
            _out.print(closeHTML());
        } catch (Exception e) {
            throw wrap(e);
        }
        
        // Clear state and return
        release();
        return EVAL_PAGE;
    }
}