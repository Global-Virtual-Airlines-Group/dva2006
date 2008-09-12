// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;

import javax.servlet.jsp.*;

import org.deltava.beans.*;
import org.deltava.taglib.ContentHelper;

/**
 * A JSP Tag to generate a JavaScript array of Google Maps v2 GMarkers.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class MarkerArrayTag extends GoogleMapEntryTag {

	private Collection<GeoLocation> _entries;
	private String _color;
	private boolean _useMarker;

	/**
	 * Sets the icon color for these markers. This overrides any color provided by the points.
	 * @param color the icon color
	 * @see MarkerMapEntry#getIconColor()
	 */
	public void setColor(String color) {
		_color = color;
	}
	
	/**
	 * Forces the marker to be rendered using a marker image instead of a Google Earth icon.
	 * @param useMarker TRUE if a marker must be used, otherwise FALSE
	 */
	public void setMarker(boolean useMarker) {
		_useMarker = useMarker;
	}

	/**
	 * Sets the points used to generate the array.
	 * @param points a Collection of GeoLocations
	 */
	public void setItems(Collection<GeoLocation> points) {
		_entries = points;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		_color = null;
		_useMarker = false;
		super.release();
	}

	/**
	 * Renders the tag data to the JSP output stream, generating a JavaScript array with a number of Google Maps
	 * markers.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if a network error occurs
	 */
	public int doEndTag() throws JspException {

		// Create the JavaScript array definition
		JspWriter out = pageContext.getOut();
		try {
			out.print("var ");
			out.print(_jsVarName);
			out.println(" = new Array();");

			// Create the markers
			for (Iterator<GeoLocation> i = _entries.iterator(); i.hasNext();) {
				GeoLocation entry = i.next();
				if (entry instanceof MapEntry) {
					StringBuilder buf = new StringBuilder(_jsVarName);
					buf.append(".push(");
					
					if ((entry instanceof IconMapEntry) && !_useMarker) {
						IconMapEntry me = (IconMapEntry) entry;
						buf.append(generateIconMarker(entry, me.getPaletteCode(), me.getIconCode(), me.getInfoBox()));
					} else {
						MarkerMapEntry me = (MarkerMapEntry) entry;
						String entryColor = (_color == null) ? me.getIconColor() : _color; 
						buf.append(generateMarker(entry, entryColor, me.getInfoBox()));
					}
					
					buf.append(");");
					out.println(buf.toString());
				}
			}
			
			// Mark the JavaScript variable as included
			ContentHelper.addContent(pageContext, API_JS_NAME, _jsVarName);
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
		   release();
		}

		return EVAL_PAGE;
	}
}