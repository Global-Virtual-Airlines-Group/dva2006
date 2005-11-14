// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util;

import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.GeospaceLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A utility class for performing geocoding operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GeoUtils {

	// Singleton constructor
	private GeoUtils() {
	}
	
	private static void recurseMidPoint(GeoPosition start, GeoPosition end, List results, int distance) {
		GeoPosition mPoint = start.midPoint(end);
		results.add(results.indexOf(start) + 1, mPoint);
		if (mPoint.distanceTo(start)  > distance)
			recurseMidPoint(start, mPoint, results, distance);

		if (mPoint.distanceTo(end) > distance)
			recurseMidPoint(mPoint, end, results, distance);
	}
	
	/**
	 * Creates a Great Circle route between two points.
	 * @param start the start position
	 * @param end the end position
	 * @param granularity the maxmimum distance between points
	 * @return a List of GeoPositions describing the Great Circle route
	 */
	public static List greatCircle(GeoPosition start, GeoPosition end, int granularity) {
		
		// Add the start/end points
		List results = new ArrayList();
		results.add(start);
		results.add(end);
		
		// Start looping
		recurseMidPoint(start, end, results, granularity);
		return results;
	}
	
	/**
	 * Calculates the distance between two points.
	 * @param start the start position
	 * @param end the end position
	 * @return the distance between the two points, in miles
	 * @see GeoPosition#distanceTo(GeoLocation)
	 */
	public static int distance(GeoLocation start, GeoLocation end) {
	   GeoPosition gp = new GeoPosition(start);
	   return gp.distanceTo(end);
	}

	/**
	 * Formats a geographic location as &quot;longitude,latitude&quot;.
	 * @param loc the location
	 * @return the formatted location string
	 */
	public static String format2D(GeoLocation loc) {
		StringBuilder buf = new StringBuilder(StringUtils.format(loc.getLongitude(), "##0.00000"));
		buf.append(',');
		buf.append(StringUtils.format(loc.getLatitude(), "#0.00000"));
		return buf.toString();
	}

	/**
	 * Formats a geospatial location as &quot;longitude,latitude,altitude&quot;. <i>The altitude will
	 * be converted from feet to meters for Google Earth</i>. 
	 * @param loc the location
	 * @param altitude the altitude in feet
	 * @return the formatted location string
	 */
	public static String format3D(GeoLocation loc, int altitude) {
		StringBuilder buf = new StringBuilder(format2D(loc));
		buf.append(',');
		buf.append(StringUtils.format(0.3048d * altitude, "#####0"));
		return buf.toString();
	}
	
	/**
	 * Formats a geospatial location as &quot;longitude,latitude,altitude&quot;. <i>The altitude will
	 * be converted from feet to meters for Google Earth</i>. 
	 * @param loc the location
	 * @return the formatted location string
	 */
	public static String format3D(GeospaceLocation loc) {
		StringBuilder buf = new StringBuilder(format2D(loc));
		buf.append(',');
		buf.append(StringUtils.format(loc.getAltitude() * 0.3048, "#####0"));
		return buf.toString();
	}
}