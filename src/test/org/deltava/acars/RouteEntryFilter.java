package org.deltava.acars;

import java.awt.Point;

import org.deltava.beans.acars.RouteEntry;

public interface RouteEntryFilter {

	public Point filter(RouteEntry re);
}