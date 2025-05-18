// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.mapbox;

import javax.servlet.jsp.*;

import org.deltava.beans.*;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.StringUtils;

/**
 * A JSP Tag to generate a MapBox Marker.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class MarkerTag extends MapEntryTag {

	private String _jsPointVarName;

	private String _label;
	private String _color;
	private GeoLocation _entry;
	private boolean _useMarker;
	private int _palCode = -1;
	private int _iconCode = -1;

	/**
	 * Sets the location of the marker.
	 * @param loc the marker's location
	 */
	public void setPoint(GeoLocation loc) {
		_entry = loc;
	}

	/**
	 * Forces the marker to be rendered using a marker image instead of a Google Earth icon.
	 * @param useMarker TRUE if a marker must be used, otherwise FALSE
	 */
	public void setMarker(boolean useMarker) {
		_useMarker = useMarker;
	}

	/**
	 * Sets the label for this marker. This overrides any label provided by the point.
	 * @param label the label HTML text
	 * @see MapEntry#getInfoBox()
	 */
	public void setLabel(String label) {
		_label = label;
	}

	/**
	 * Sets the icon color for this marker. This overrides any color provided by the point.
	 * @param color the icon color
	 * @see MarkerMapEntry#getIconColor()
	 */
	public void setColor(String color) {
		_color = color;
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
	 * Sets the JavaScript point variable name. If this is specified a seperate GPoint variable will be set.
	 * @param varName the variable name
	 */
	public void setPointVar(String varName) {
		_jsPointVarName = varName;
	}

	/**
	 * Resets the tag's state variables.
	 */
	@Override
	public void release() {
		_jsPointVarName = null;
		_label = null;
		_color = null;
		_useMarker = false;
		_palCode = -1;
		_iconCode = -1;
		super.release();
	}

	/**
	 * Renders the JSP tag, creating a Javascript line.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException
	 */
	@Override
	public int doEndTag() throws JspException {

		// Calculate if color or label need to be overridden
		if (_entry instanceof MapEntry mapInfo) {
			if (_label == null)
				_label = mapInfo.getInfoBox();
			if ((_color == null) && (_entry instanceof MarkerMapEntry mme))
				_color = mme.getIconColor();
		}

		try {
			JspWriter out = pageContext.getOut();
			writeVariableName();

			// Call the googleMarker function
			if ((_entry instanceof IconMapEntry ime) && !_useMarker) {
				out.print(generateIconMarker(_entry, (_palCode == -1) ? ime.getPaletteCode() : _palCode, (_iconCode == -1) ? ime.getIconCode() : _iconCode, _label));
			} else
				out.print(generateMarker(_entry, _color, _label));

			out.print(';');

			// Write the point variable
			if (_jsPointVarName != null) {
				out.print("\nlet ");
				out.print(_jsPointVarName);
				out.print(" = [");
				out.print(StringUtils.format(_entry.getLongitude(), "##0.00000"));
				out.print(',');
				out.print(StringUtils.format(_entry.getLatitude(), "##0.00000"));
				out.print("];");
			}

			// Mark the JavaScript point variable as included
			if (_jsPointVarName != null)
				ContentHelper.addContent(pageContext, InsertAPITag.API_JS_NAME, _jsPointVarName);

			// Mark the JavaScript marker variable as included
			if (_jsVarName != null)
				ContentHelper.addContent(pageContext, InsertAPITag.API_JS_NAME, _jsVarName);
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}
}