// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.mapbox;

import java.util.*;

import javax.servlet.jsp.*;

import org.json.*;

import org.deltava.beans.GeoLocation;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP Tag to generate a GeoJSON line.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class LineTag extends MapEntryTag {
	
	private final Collection<GeoLocation> _pts = new ArrayList<GeoLocation>();

	private String _srcJsVarName;
	private String _color = "#000000";

	private int _width = 1;
	private double _transparency = 1.0;
	
	/**
	 * Sets the JavaScript variable name for the line GeoJSON source.
	 * @param varName the variable name
	 */
	public void setSrc(String varName) {
		_srcJsVarName = varName;
	}

	/**
	 * Sets the line's color.
	 * @param color the color as an HTML #RRGGBB value
	 */
	public void setColor(String color) {
		_color = color;
	}

	/**
	 * Sets the line's width.
	 * @param width the width in pixels
	 */
	public void setWidth(int width) {
		_width = width;
	}

	/**
	 * Sets the line's transparency.
	 * @param trans the transparancy, as a percentage
	 */
	public void setTransparency(double trans) {
		_transparency = Math.max(0, Math.min(1, trans));
	}

	@Override
	public void release() {
		_pts.clear();
		_width = 1;
		_transparency = 1.0;
		_color = "#000000";
		super.release();
	}
	
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();

		// Check that the source variable has been defined
		if (!ContentHelper.containsContent(pageContext, InsertAPITag.API_JS_NAME, _srcJsVarName))
			throw new IllegalStateException(_srcJsVarName + " not defined in JavaScript");
		
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws JspException {
		
		// Calculate the name
		String name = (_srcJsVarName.indexOf('.') > -1) ? _srcJsVarName.substring(_srcJsVarName.lastIndexOf('.') + 1) : _srcJsVarName;
		
		// Generate the line
		JSONObject lo = new JSONObject();
		lo.put("id", name);
		lo.put("type", "line");
		lo.put("source", _srcJsVarName);
		JSONObject po = new JSONObject();
		po.put("line-color", _color);
		po.put("line-width", _width);
		po.put("line-opacity", _transparency);
		lo.put("paint", po);
		JSONObject llo = new JSONObject();
		llo.put("line-join", "round");
		llo.put("line-cap", "round");
		lo.put("layout", llo);
		
		try {
			JspWriter out = pageContext.getOut();
			writeVariableName();
			out.print(lo.toString());
			out.print(';');
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}
}