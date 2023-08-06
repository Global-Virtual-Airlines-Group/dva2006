// Copyright 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.econ.EliteLevel;

/**
 * A JSP tag to format Elite Level names.
 * @author Luke
 * @version 11.1
 * @since 9.2
 */

public class EliteFormatTag extends TagSupport {

	private EliteLevel _lvl;
	private String _className;
	private boolean _nameOnly;
	private boolean _showYear;

	/**
     * Updates the CSS class for this formatted level name.
     * @param cName the class Name(s)
     */
    public final void setClassName(String cName) {
        _className = cName;
    }
    
    /**
     * Updates the Elite status level.
     * @param el the EliteLevel
     */
    public void setLevel(EliteLevel el) {
    	_lvl = el;
    }
    
    /**
     * Sets whether to display &quot;Member&quot; after the level name if needed.
     * @param nameOnly TRUE if suppressed, otherwise FALSE
     */
    public void setNameOnly(boolean nameOnly) {
    	_nameOnly = nameOnly;
    }
    
    /**
     * Sets whether to display the program year in the span title.
     * @param showYear TRUE to display the program year, otherwise FALSE
     */
    public void setShowYear(boolean showYear) {
    	_showYear = showYear;
    }
    
    @Override
    public int doEndTag() throws JspException {
    	if (_lvl == null) return EVAL_PAGE;
    	JspWriter out = pageContext.getOut();
    	try {
    		out.print("<span");
    		if (_className != null) {
    			out.print(" class=\"");
    			out.print(_className);
    			out.print('\"');
    		}
    		
    		if (_showYear) {
    			out.print(" title=\"");
    			out.print(_lvl.getName());
    			out.print(" (");
    			out.print(_lvl.getYear());
    			out.print(")\"");
    		}
    		
    		out.print(" style=\"color:#");
    		out.print(_lvl.getHexColor());
    		out.print("\">");
    		out.print(_lvl.getName());
    		if (!_nameOnly && !_lvl.getName().contains("Member"))
    			out.print(" Member");
    		
    		out.print("</span>");
    		return EVAL_PAGE;
    	} catch (Exception e) {
    		throw new JspException(e);
    	} finally {
    		release();
    	}
    }
  
    @Override
    public void release() {
    	super.release();
    	_className = null;
    	_nameOnly = false;
    	_showYear = false;
    }
}