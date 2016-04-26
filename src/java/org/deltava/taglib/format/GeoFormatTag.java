// Copyright 2005, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.GeoLocation;

import org.deltava.util.StringUtils;

/**
 * A JSP Tag to format geographic coordinates.
 * @author Luke
 * @version 7.0
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
	@Override
	public void release() {
		super.release();
		_className = null;
	}
	
    /**
     * Formats the position and writes it to the JSP output writer.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
     */
	@Override
	public int doEndTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();
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
		} finally {
			release();
		}
        
        return EVAL_PAGE;
	}
}