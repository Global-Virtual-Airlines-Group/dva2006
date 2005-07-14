// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.format;

import java.io.IOException;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.schedule.GeoPosition;

/**
 * A JSP Tag to format geographic coordinates.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GeoFormatTag extends TagSupport {

	private GeoPosition _gp;
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
	public void setPos(GeoPosition gp) {
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
            
            // Format the latitude
            out.print(Math.abs(GeoPosition.getDegrees(_gp.getLatitude())));
            out.print("<sup>o</sup> ");
            out.print(GeoPosition.getMinutes(_gp.getLatitude()));
            out.print("&#39; ");
            out.print(GeoPosition.getSeconds(_gp.getLatitude()));
            out.print("&quot; ");
            out.print((GeoPosition.getDegrees(_gp.getLatitude()) < 0) ? "S " : "N ");
            
            // Format the longitude
            out.print(Math.abs(GeoPosition.getDegrees(_gp.getLongitude())));
            out.print("<sup>o</sup> ");
            out.print(GeoPosition.getMinutes(_gp.getLongitude()));
            out.print("&#39; ");
            out.print(GeoPosition.getSeconds(_gp.getLongitude()));
            out.print("&quot; ");
            out.print((GeoPosition.getDegrees(_gp.getLongitude()) < 0) ? 'W' : 'E');
			
            if (_className != null)
                out.print("</span>");
        } catch (IOException ie) {
            JspException je = new JspException(ie.getMessage());
            je.initCause(ie);
            throw je;
		}
        
        // Release state and return
        release();
        return EVAL_PAGE;
	}
}