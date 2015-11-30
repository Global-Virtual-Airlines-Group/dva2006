// Copyright 2005, 2006, 2008, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;

import javax.servlet.jsp.*;

import org.deltava.beans.*;
import org.deltava.taglib.ContentHelper;

/**
 * A JSP Tag to generate a JavaScript array of Google Maps v2 GMarkers.
 * @author Luke
 * @version 6.3
 * @since 1.0
 */

public class MarkerArrayTag extends GoogleMapEntryTag {

	private Collection<GeoLocation> _entries;
	private String _color;
	private boolean _useMarker;
	private int _palCode = -1;
	private int _iconCode = -1;

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
	 * Sets the Google Earth palette code for this marker, overriding any code provided by the point.
	 * @param code the Google Earth palette code
	 */
	public void setPalette(int code) {
		_palCode = code;
	}

	/**
	 * Sets the Google Earth icon code for this marker, overriding any code provided by the point.
	 * @param code the Google Earth icon code
	 */
	public void setIcon(int code) {
		_iconCode = code;
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
	@Override
	public void release() {
		_color = null;
		_useMarker = false;
		_palCode = -1;
		_iconCode = -1;
		super.release();
	}

	/**
	 * Renders the tag data to the JSP output stream, generating a JavaScript array with a number of Google Maps
	 * markers.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if a network error occurs
	 */
	@Override
	public int doEndTag() throws JspException {

		// Create the JavaScript array definition
		JspWriter out = pageContext.getOut();
		try {
			if (_jsVarName.indexOf('.') == -1)
				out.print("var ");
			out.print(_jsVarName);
			out.println(" = [];");

			// Create the markers
			StringBuilder buf = new StringBuilder();
			for (Iterator<GeoLocation> i = _entries.iterator(); i.hasNext();) {
				GeoLocation entry = i.next();
				if (entry instanceof MapEntry) {
					buf.append(_jsVarName);
					buf.append(".push(");
					
					if ((entry instanceof IconMapEntry) && !_useMarker) {
						IconMapEntry me = (IconMapEntry) entry;
						buf.append(generateIconMarker(entry, (_palCode == -1) ? me.getPaletteCode() : _palCode, (_iconCode == -1) ? me.getIconCode() : _iconCode, me.getInfoBox()));
					} else {
						MarkerMapEntry me = (MarkerMapEntry) entry;
						String entryColor = (_color == null) ? me.getIconColor() : _color; 
						buf.append(generateMarker(entry, entryColor, me.getInfoBox()));
					}
					
					buf.append(");");
					out.println(buf.toString());
					buf.setLength(0);
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