// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.GeoLocation;

import org.deltava.util.StringUtils;

/**
 * A JSP Tag to format geographic coordinates.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GeoFormatTag extends TagSupport {

	private GeoLocation _gp;
	private String _className;
	
    /**
     * Updates the CSS class for this formatted number. This will automatically enclose the output in a
     * &lt;SPAN&gt; tag.
     * @param cName the class Name(s)
     */
    public final void setClassName(String cName) {
        _className = cName;
    }
    
	/**
	 * Sets the latitude/longitude to display.
	 * @param gp the GeoPosition to display
	 */
	public void setPos(GeoLocation gp) {
		_gp = gp;
	}
	
	/**
	 * Releases the tag's state data.
	 */
	public void release() {
		_className = null;
		super.release();
	}
	
    /**
     * Formats the position and writes it to the JSP output writer.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
     */
	public int doEndTag() throws JspException {
		
		JspWriter out = pageContext.getOut();
		try {
            if (_className != null) {
                out.print("<span class=\"");
                out.print(_className);
                out.print("\">");
            }
            
            // Format the latitude/longitude
            out.print((_gp == null) ? "UNKNOWN" : StringUtils.format(_gp, true, GeoLocation.ALL));
			
            if (_className != null)
                out.print("</span>");
        } catch (Exception e) {
            throw new JspException(e);
		}
        
        // Release state and return
        release();
        return EVAL_PAGE;
	}
}