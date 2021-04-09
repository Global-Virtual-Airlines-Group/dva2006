// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.econ.EliteLevel;

/**
 * A JSP tag to format Elite Level names.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class EliteFormatTag extends TagSupport {

	private EliteLevel _lvl;
	private String _className;
	private boolean _nameOnly;

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
    }
}