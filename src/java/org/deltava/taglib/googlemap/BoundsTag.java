// Copyright 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.*;

import javax.servlet.jsp.*;

import org.deltava.beans.GeoLocation;

import org.deltava.util.*;

/**
 * A JSP tag to create a Google Maps LatLngBounds object.
 * @author Luke
 * @version 7.3
 * @since 6.3
 */

public class BoundsTag extends GoogleMapEntryTag {

	private final Collection<GeoLocation> _pts = new ArrayList<GeoLocation>();

	/**
	 * Sets the points to use to calculate the bounding box.
	 * @param pts a Collection of GeoLocation objects
	 */
	public void setItems(Collection<GeoLocation> pts) {
		_pts.addAll(pts);
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_pts.clear();
	}
	
	/**
	 * Renders the tag to the JSP output stream, generating a Google Maps LatLngBounds.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		Tuple<GeoLocation, GeoLocation> bnds = GeoUtils.getBoundingBox(_pts);
		try {
			JspWriter out = pageContext.getOut();
			writeVariableName();
			out.print("new google.maps.LatLngBounds(");
			out.print(JSONUtils.format(bnds.getRight()));
			out.print(',');
			out.print(JSONUtils.format(bnds.getLeft()));
			out.print(");");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}