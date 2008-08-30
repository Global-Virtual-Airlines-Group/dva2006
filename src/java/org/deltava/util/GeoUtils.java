// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A utility class for performing geocoding operations.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class GeoUtils {
	
	private static final char DEGREE = (char) 176;
	
	// Singleton constructor
	private GeoUtils() {
	}
	
	private static void recurseMidPoint(GeoPosition start, GeoLocation end, List<GeoLocation> results, int distance) {
		GeoPosition mPoint = start.midPoint(end);
		results.add(results.indexOf(start) + 1, mPoint);
		if (mPoint.distanceTo(start)  > distance)
			recurseMidPoint(start, mPoint, results, distance);

		if (mPoint.distanceTo(end) > distance)
			recurseMidPoint(mPoint, end, results, distance);
	}
	
	/**
	 * Creates a Great Circle route between two points.
	 * @param start the start location
	 * @param end the end location
	 * @param granularity the maxmimum distance between points
	 * @return a List of GeoPositions describing the Great Circle route
	 */
	public static List<GeoLocation> greatCircle(GeoLocation start, GeoLocation end, int granularity) {
		
		// Add the start point
		GeoPosition gpStart = new GeoPosition(start);
		List<GeoLocation> results = new ArrayList<GeoLocation>();
		results.add(gpStart);
		recurseMidPoint(gpStart, end, results, granularity);
		results.add(end);
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
	
	/**
	 * Formats a location into a format suitable for inclusion in a Microsoft Flight Simulator 2004
	 * flight plan. 
	 * @param loc the location
	 * @return a formatted location
	 */
	public static String formatFS9(GeoLocation loc) {
		StringBuilder buf = new StringBuilder();
		buf.append((loc.getLatitude() >= 0) ? 'N' : 'S');
		buf.append(StringUtils.format(Math.abs(GeoPosition.getDegrees(loc.getLatitude())), "#0"));
		buf.append("* ");
		buf.append(StringUtils.format(Math.abs((loc.getLatitude() - GeoPosition.getDegrees(loc.getLatitude())) * 60), "#0.00"));
        buf.append("', ");
        buf.append((loc.getLongitude() >= 0) ? 'E' : 'W');
        buf.append(StringUtils.format(Math.abs(GeoPosition.getDegrees(loc.getLongitude())), "##0"));
        buf.append("* ");
        buf.append(StringUtils.format(Math.abs((loc.getLongitude() - GeoPosition.getDegrees(loc.getLongitude())) * 60), "#0.00"));
        buf.append('\'');
        return buf.toString();
	}
	
	/**
	 * Formats a location into a format suitable for inclusion in a Microsoft Flight Simulator X
	 * flight plan. Since this has a Windows-specific character in it, the resulting string should
	 * be formatted as CP1252.
	 * @param loc the location
	 * @return a formatted location
	 */
	public static String formatFSX(GeoLocation loc) {
		StringBuilder buf = new StringBuilder();
		buf.append((loc.getLatitude() >= 0) ? 'N' : 'S');
		buf.append(StringUtils.format(Math.abs(GeoPosition.getDegrees(loc.getLatitude())), "#0"));
		buf.append(DEGREE); // Win32 degree character
		buf.append(' ');
        buf.append(GeoPosition.getMinutes(loc.getLatitude()));
        buf.append("' ");
        buf.append(StringUtils.format(GeoPosition.getSeconds(loc.getLatitude()), "#.00"));
        buf.append("\",");
        buf.append((loc.getLongitude() >= 0) ? 'E' : 'W');
        buf.append(StringUtils.format(Math.abs(GeoPosition.getDegrees(loc.getLongitude())), "##0"));
        buf.append(DEGREE); // Win32 degree character
		buf.append(' ');
        buf.append(GeoPosition.getMinutes(loc.getLongitude()));
        buf.append("' ");
        buf.append(StringUtils.format(GeoPosition.getSeconds(loc.getLongitude()), "#.00"));
        buf.append("\"");
        return buf.toString();
	}
	
	/**
	 * &quot;Normalizes&quot; an angle by ensuring it is between 0 and 360.
	 * @param degrees the angle in degrees
	 * @return the normalized angle
	 */
	public static double normalize(double degrees) {
		int amt = (degrees < 0) ? 360 : -360;
		while ((degrees < 0) || (degrees > 360))
			degrees += amt;
		
		return degrees;
	}
	
	/**
	 * &quot;Normalizes&quot; a geographic location by ensuring that the latitude is between -90 and 90 degrees,
	 * and the longitude is between -180 and 180 degrees.
	 * @param lat the latitude
	 * @param lng the longitude
	 * @return the normalized location
	 */
	public static GeoLocation normalize(double lat, double lng) {
		 
		// Normalize latitude
		int amt = (lat < -90) ? 90 : -90;
		while ((lat < -90) || (lat > 90))
			lat += amt;
		
		// Normalize longitude
		amt = (lng < -180) ? 180 : -180;
		while ((lng < -180) || (lng > 180))
			lng += amt;
		
		return new GeoPosition(lat, lng);
	}
	
	/**
	 * Determines the coordinates of a second point on a particular heading from the first. This converts the polar
	 * coordinates provided into cartesian coordinates, and then adds them to the original point.
	 * @param p1 the original point
	 * @param distance the distance in miles
	 * @param angle the heading in degrees
	 * @return a normalized GeoPosition
	 */
	public static GeoLocation bearingPoint(GeoLocation p1, double distance, double angle) {
		
		// Convert the miles to degrees of latitude, and the angle to radians
		distance /= GeoLocation.DEGREE_MILES;
		angle = StrictMath.toRadians(angle);
	
		// These are coordinates RELATIVE to the origin
		double lat2 = distance * StrictMath.cos(angle);
		double lng2 = distance * StrictMath.sin(angle);
		return normalize(p1.getLatitude() + lat2, p1.getLongitude() + lng2);
	}
	
	/**
	 * Returns all the neighbors of a location within a certain distance.
	 * @param gl the point
	 * @param points the locations to check
	 * @param distance the distance in miles
	 * @return a Collection of GeoLocations
	 */
	public static Collection<GeoLocation> neighbors(GeoLocation gl, Collection<? extends GeoLocation> points, int distance) {
		GeoPosition gp = new GeoPosition(gl); 
		Collection<GeoLocation> results = new ArrayList<GeoLocation>();
		for (Iterator<? extends GeoLocation> i = points.iterator(); i.hasNext(); ) {
			GeoLocation loc = i.next();
			if (gp.distanceTo(loc) < distance)
				results.add(loc);
		}
		
		return results;
	}
}