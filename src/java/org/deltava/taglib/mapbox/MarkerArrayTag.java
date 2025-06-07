// Copyright 2005, 2006, 2008, 2015, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.mapbox;

import java.util.*;

import javax.servlet.jsp.*;

import org.deltava.beans.*;
import org.deltava.taglib.*;

/**
 * A JSP Tag to generate a JavaScript array of MapBox markers.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class MarkerArrayTag extends MapEntryTag {

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
	
	@Override
	public void release() {
		_color = null;
		_useMarker = false;
		_palCode = -1;
		_iconCode = -1;
		super.release();
	}

	@Override
	public int doEndTag() throws JspException {

		// Create the JavaScript array definition
		JspWriter out = pageContext.getOut();
		try {
			if (_jsVarName.indexOf('.') == -1)
				out.print("let ");
			out.print(_jsVarName);
			out.println(" = [];");

			// Create the markers
			StringBuilder buf = new StringBuilder();
			for (Iterator<GeoLocation> i = _entries.iterator(); i.hasNext();) {
				GeoLocation entry = i.next();
				if (entry instanceof MapEntry me) {
					buf.append(_jsVarName);
					buf.append(".push(");
					if ((me instanceof IconMapEntry ime) && !_useMarker) {
						int pal = (_palCode == -1) ? ime.getPaletteCode() : _palCode;
						int icon = (_iconCode == -1) ? ime.getIconCode() : _iconCode;
						String label = (ime instanceof LabelMapEntry lme) ? lme.getLabel() : null;
						buf.append(generateIconMarker(entry, pal, icon, ime.getInfoBox(), label));
					} else {
						if ((_color == null) && (entry instanceof MarkerMapEntry mme)) _color = mme.getIconColor();
						if (_color == null) _color = "white";
						String label = (entry instanceof LabelMapEntry lme) ? lme.getLabel() : null;
						buf.append(generateMarker(entry, _color, me.getInfoBox(), label));
					}
					
					buf.append(");");
					out.println(buf.toString());
					buf.setLength(0);
				}
			}
			
			// Mark the JavaScript variable as included
			ContentHelper.addContent(pageContext, InsertAPITag.API_JS_NAME, _jsVarName);
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
		   release();
		}

		return EVAL_PAGE;
	}
}