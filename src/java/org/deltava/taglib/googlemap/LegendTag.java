// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to display a legend entry for a Google Maps marker.
 * @author Luke
 * @version 1.0
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
	public void release() {
		super.release();
		_class = null;
	}

	/**
	 * Renders the marker image and legend text to the JSP output stream.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {

		JspWriter jw = pageContext.getOut();
		try {
			// Write image tag
			jw.print("<img border=\"0\" alt=\"");
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

			// Write legend
			jw.print(_legend);

			// Close className span if necessary
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