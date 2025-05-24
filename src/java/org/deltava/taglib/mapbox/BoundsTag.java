// Copyright 2015, 2017, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.mapbox;

import java.util.*;

import javax.servlet.jsp.*;

import org.deltava.beans.GeoLocation;

import org.deltava.util.*;

/**
 * A JSP tag to create a MapBox LatLngBounds object.
 * @author Luke
 * @version 12.0
 * @since 6.3
 */

public class BoundsTag extends MapEntryTag {

	private final Collection<GeoLocation> _pts = new ArrayList<GeoLocation>();

	/**
	 * Sets the points to use to calculate the bounding box.
	 * @param pts a Collection of GeoLocation objects
	 */
	public void setItems(Collection<GeoLocation> pts) {
		_pts.addAll(pts);
	}
	
	@Override
	public void release() {
		super.release();
		_pts.clear();
	}
	
	@Override
	public int doEndTag() throws JspException {
		Tuple<GeoLocation, GeoLocation> bnds = GeoUtils.getBoundingBox(_pts);
		try {
			JspWriter out = pageContext.getOut();
			writeVariableName();
			out.print('[');
			out.print(JSONUtils.toLL(bnds.getRight()));
			out.print(',');
			out.print(JSONUtils.toLL(bnds.getLeft()));
			out.print("];");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}