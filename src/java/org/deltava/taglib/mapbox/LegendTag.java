// Copyright 2005 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.mapbox;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to display a legend entry for a MapBox marker.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public class LegendTag extends TagSupport {

	private String _color;
	private String _legend;
	private String _class;

	/**
	 * Sets the marker color.
	 * @param color the marker color
	 */
	public void setColor(String color) {
		_color = color;
	}

	/**
	 * Sets the marker legend.
	 * @param legend the legend text
	 */
	public void setLegend(String legend) {
		_legend = legend;
	}

	/**
	 * Sets the CSS class name for the marker legend.
	 * @param cName the CSS class name
	 */
	public void setClassName(String cName) {
		_class = cName;
	}

	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_class = null;
	}

	/**
	 * Renders the marker image and legend text to the JSP output stream.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			JspWriter jw = pageContext.getOut();
			jw.print("<img class=\"noborder\" alt=\"");
			jw.print(_color);
			jw.print("\" src=\"/");
			jw.print(SystemData.get("path.img"));
			jw.print("/maps/point_");
			jw.print(_color);
			jw.print(".png\" /> ");

			// Write span for legend
			if (_class != null) {
				jw.print("<span class=\"");
				jw.print(_class);
				jw.print("\">");
			}

			jw.print(_legend);
			if (_class != null)
				jw.print("</span>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
		   release();
		}

		return EVAL_PAGE;
	}
}