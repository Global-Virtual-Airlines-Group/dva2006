// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.econ.EliteLifetime;

/**
 * A JSP tag to format EliteLifetime beans.
 * @author Luke
 * @version 11.5
 * @since 11.5
 */

public class EliteLifetimeFormatTag extends TagSupport {
	
	private EliteLifetime _el;
	private String _className;

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
    public void setLevel(EliteLifetime el) {
    	_el = el;
    }
    
    @Override
    public int doEndTag() throws JspException {
    	if (_el == null) return EVAL_PAGE;
    	JspWriter out = pageContext.getOut();
    	try {
    		out.print("<span");
    		if (_className != null) {
    			out.print(" class=\"");
    			out.print(_className);
    			out.print('\"');
    		}
    		
    		out.print(" style=\"color:#");
    		out.print(_el.getHexColor());
    		out.print("\">");
    		out.print(_el.getName());
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
    }
}