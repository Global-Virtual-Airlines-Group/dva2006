// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.acars;

import java.awt.Point;

import org.deltava.beans.acars.RouteEntry;

import org.deltava.util.tile.*;

public class AllFilter implements RouteEntryFilter {
	
	private final Projection _p;

	public AllFilter(int zoom) {
		super();
		_p = new MercatorProjection(zoom);
	}

	@Override
	public Point filter(RouteEntry re) {
		return _p.getPixelAddress(re);
	}
}