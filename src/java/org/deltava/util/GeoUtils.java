// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.navdata.Hemisphere;
import org.deltava.beans.schedule.GeoPosition;

import com.vividsolutions.jts.geom.*;

/**
 * A utility class for performing geocoding operations.
 * @author Luke
 * @version 7.3
 * @since 1.0
 */

public class GeoUtils {

	private static final char DEGREE = (char) 176;

	// Singleton constructor
	private GeoUtils() {
		super();
	}

	private static void recurseMidPoint(GeoPosition start, GeoLocation end, List<GeoLocation> results, int distance) {
		GeoPosition mPoint = start.midPoint(end);
		results.add(results.indexOf(start) + 1, mPoint);
		if (mPoint.distanceTo(start) > distance)
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
	 * Formats a geospatial location as &quot;longitude,latitude,altitude&quot;. <i>The altitude will be converted from
	 * feet to meters for Google Earth</i>.
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
	 * Formats a geospatial location as &quot;longitude,latitude,altitude&quot;. <i>The altitude will be converted from
	 * feet to meters for Google Earth</i>.
	 * @param loc the location
	 * @return the formatted location string
	 */
	public static String format3D(GeospaceLocation loc) {
		StringBuilder buf = new StringBuilder(format2D(loc));
		buf.append(',');
		buf.append(StringUtils.format(loc.getAltitude() * 0.3048d, "#####0"));
		return buf.toString();
	}

	/**
	 * Formats a location into a format suitable for inclusion in a Microsoft Flight Simulator 2004 flight plan.
	 * @param loc the location
	 * @return a formatted location
	 */
	public static String formatFS9(GeoLocation loc) {
		StringBuilder buf = new StringBuilder(48);
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
	 * Formats a location into a format suitable for inclusion in a Microsoft Flight Simulator X or Lockheed-Martin
	 * Prepar3D flight plan.
	 * @param loc the location
	 * @return a formatted location
	 */
	public static String formatFSX(GeoLocation loc) {
		StringBuilder buf = new StringBuilder(48);
		buf.append((loc.getLatitude() >= 0) ? 'N' : 'S');
		buf.append(StringUtils.format(Math.abs(GeoPosition.getDegrees(loc.getLatitude())), "#0"));
		buf.append(DEGREE); // Win32 degree character
		buf.append(' ');
		buf.append(GeoPosition.getMinutes(loc.getLatitude()));
		buf.append("' ");
		buf.append(StringUtils.format(GeoPosition.getSeconds(loc.getLatitude()), "#0.00"));
		buf.append("\",");
		buf.append((loc.getLongitude() >= 0) ? 'E' : 'W');
		buf.append(StringUtils.format(Math.abs(GeoPosition.getDegrees(loc.getLongitude())), "##0"));
		buf.append(DEGREE); // Win32 degree character
		buf.append(' ');
		buf.append(GeoPosition.getMinutes(loc.getLongitude()));
		buf.append("' ");
		buf.append(StringUtils.format(GeoPosition.getSeconds(loc.getLongitude()), "#0.00"));
		buf.append('\"');
		return buf.toString();
	}
	
	/**
	 * Formats an elevation for Flight Simulator flight plans
	 * @param elevation the elevation in feet
	 * @return the formatted elevation
	 */
	public static String formatFSElevation(int elevation) {
		StringBuilder buf = new StringBuilder();
		buf.append((elevation < 0) ? '-' : '+');
		buf.append(StringUtils.format(Math.abs(elevation), "000000.00"));
		return buf.toString();
	}

	/**
	 * &quot;Normalizes&quot; an angle by ensuring it is between 0 and 360.
	 * @param degrees the angle in degrees
	 * @return the normalized angle
	 */
	public static double normalize(double degrees) {
		int amt = (degrees > 0) ? 360 : -360; 
		double d = degrees % amt;
		while (d < 0)
			d -= amt;

		return d;
	}
	
	/**
	 * Calculates the delta between two headings.
	 * @param hdg1 the first heading in degrees
	 * @param hdg2 the second heading in degrees
	 * @return the delta in degrees
	 */
	public static double delta(double hdg1, double hdg2) {
		double delta = Math.abs(normalize(hdg1) - normalize(hdg2));
		return (delta > 180) ? (360 - delta) : delta;
	}
	
	/**
	 * Returns a bounding box that contains all of the specified locations.
	 * @param locs a Collection of GeoLocations
	 * @return a Tuple with the northeast and southwest limits of the box
	 */
	public static Tuple<GeoLocation, GeoLocation> getBoundingBox(Collection<? extends GeoLocation> locs) {
		double maxLat = -90; double minLat = 90; double maxLng = -180; double minLng = 180;
		for (GeoLocation loc : locs) {
			if (!isValid(loc)) continue;
			maxLat = Math.max(maxLat, loc.getLatitude());
			minLat = Math.min(minLat, loc.getLatitude());
			maxLng = Math.max(maxLng, loc.getLongitude());
			minLng = Math.min(minLng, loc.getLongitude());
		}
		
		return Tuple.create(new GeoPosition(maxLat, maxLng), new GeoPosition(minLat, minLng));
	}

	/**
	 * &quot;Normalizes&quot; a geographic location by ensuring that the latitude is between -90 and 90 degrees, and the
	 * longitude is between -180 and 180 degrees.
	 * @param lat the latitude
	 * @param lng the longitude
	 * @return the normalized location
	 */
	public static GeoLocation normalize(double lat, double lng) {
	
		// Sanity check
		double lt = lat; double ln = lng;
		if (Math.abs(lt) > 90)
			lt %= (lt > 0) ? 90 : -90;
		if (Math.abs(ln) > 180)
			ln %= (ln > 0) ? 180 : -180;

		// Normalize latitude
		int amt = (lat < -90) ? 90 : -90; 
		while ((lt < -90) || (lt > 90))
			lt += amt;

		// Normalize longitude
		amt = (lng < -180) ? 180 : -180; 
		while ((ln < -180) || (ln > 180))
			ln += amt;

		return new GeoPosition(lt, ln);
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
		double dst = distance / GeoLocation.DEGREE_MILES;
		double angrad = StrictMath.toRadians(angle);

		// These are coordinates RELATIVE to the origin
		double lat2 = dst * StrictMath.cos(angrad);
		double lng2 = dst * StrictMath.sin(angrad);
		return normalize(p1.getLatitude() + lat2, p1.getLongitude() + lng2);
	}

	/**
	 * Returns all the neighbors of a location within a certain distance.
	 * @param gl the point
	 * @param points the locations to check
	 * @param distance the distance in miles
	 * @return a Collection of GeoLocations
	 */
	public static List<GeoLocation> neighbors(GeoLocation gl, Collection<? extends GeoLocation> points, int distance) {
		GeoPosition gp = new GeoPosition(gl);
		return points.stream().filter(pt -> (gp.distanceTo(pt) < distance)).collect(Collectors.toList());
	}
	
	/**
	 * Calculates the distance between two points.
	 * @param start the start position
	 * @param end the end position
	 * @return the distance between the two points, in miles
	 * @see GeoPosition#distanceTo(GeoLocation)
	 */
	public static int distance(GeoLocation start, GeoLocation end) {
		return new GeoPosition(start).distanceTo(end);
	}

	/**
	 * Calculates the distance between two points in feet.
	 * @param l1 the first GeoLocation
	 * @param l2 the second GeoLocation
	 * @return the distance <i>in feet</i>, or -1 if l1 or l2 are null
	 * @see GeoPosition#distanceTo(GeoLocation)
	 */
	public static int distanceFeet(GeoLocation l1, GeoLocation l2) {

		// Do null check
		if ((l1 == null) || (l2 == null))
			return -1;
		
		// Convert the latitude to radians
		double lat1 = Math.toRadians(l1.getLatitude());
		double lat2 = Math.toRadians(l2.getLatitude());
		
		// Get the longitude difference in radians
		double lngDiff = Math.toRadians(Math.abs(l1.getLongitude() - l2.getLongitude()));
		
		// Do the math - this makes my head hurt
		double p1 = StrictMath.sin(lat1) * StrictMath.sin(lat2);
		double p2 = StrictMath.cos(lat1) * StrictMath.cos(lat2) * StrictMath.cos(lngDiff);
		double distD = Math.toDegrees(Math.acos(p1 + p2));
		
		// Convert to miles and return
		return (int) StrictMath.round(distD * GeoLocation.DEGREE_MILES * 5280);
	}
	
	/**
	 * Calculates the course between two points.
	 * @param l1 the first GeoLocation
	 * @param l2 the second GeoLocation
	 * @return the initial course in degrees
	 */
	public static double course(GeoLocation l1, GeoLocation l2) {
		
		// Convert the latitude to radians
		double lat1 = Math.toRadians(l1.getLatitude());
		double lat2 = Math.toRadians(l2.getLatitude());
		
		// Convert the longitude to radians
		double lng1 = Math.toRadians(l1.getLongitude());
		double lng2 = Math.toRadians(l2.getLongitude());
		
		// Do the math - this makes my head hurt
		double y = StrictMath.sin(lng2-lng1) * StrictMath.cos(lat2);
		double x = StrictMath.cos(lat1) * StrictMath.sin(lat2) - StrictMath.sin(lat1) * 
				StrictMath.cos(lat2) * StrictMath.cos(lng2-lng1);
		
		// Calcualte the degrees
		double crs = Math.toDegrees(StrictMath.atan2(y, x) % (Math.PI * 2));
		while (crs < 0)
			crs += 360;
		
		return crs;
	}
	
	/**
	 * Returns the latitude at which point a Greate Cirlce route intersects a meridian.
	 * @param l1 the first GeoLocation
	 * @param l2 the second GeoLocation
	 * @param lng the longitude in degrees
	 * @return the latitude in degrees
	 */
	public static double meridianLatitude(GeoLocation l1, GeoLocation l2, double lng) {
		double lon = Math.toRadians(lng);
		
		// Convert the latitude to radians
		double lat1 = Math.toRadians(l1.getLatitude());
		double lat2 = Math.toRadians(l2.getLatitude());
		
		// Convert the longitude to radians
		double lng1 = Math.toRadians(l1.getLongitude());
		double lng2 = Math.toRadians(l2.getLongitude());
		
		double x = StrictMath.sin(lat1) * StrictMath.cos(lat2) * StrictMath.sin(lon - lng2) - 
		StrictMath.sin(lat2) * StrictMath.cos(lat1) * StrictMath.sin(lon - lng1);
		double y = StrictMath.cos(lat1) * StrictMath.cos(lat2) * StrictMath.sin(lng1-lng2);
		
		double lat = StrictMath.atan(x / y);
		return Math.toDegrees(lat);
	}
	
	/**
	 * Returns whether a direct Great Circle route crosses a meridian.
	 * @param l1 the first GeoLocation
	 * @param l2 the second GeoLocation
	 * @param lng the longitude in degrees
	 * @return TRUE if the meridian is crossed on the most direct route, otherwise FALSE
	 */
	public static boolean crossesMeridian(GeoLocation l1, GeoLocation l2, double lng) {
		double lng2 = normalize(lng);
		double ln1 = normalize(l1.getLongitude());
		double ln2 = normalize(l2.getLongitude());
		double ld = Math.abs(ln1 - ln2);
		double ld2 = Math.min(ld, 360 - ld);
		
		double d1 = Math.abs(ln1 - lng2);
		double d2 = Math.abs(ln2 - lng2);
		double dn1 = Math.min(d1, 360 - d1);
		double dn2 = Math.min(d2, 360 - d2);
		
		return ((dn1 + dn2) <= ld2);
	}
	
	/**
	 * Strips out spurious waypoints from a route, that are at least a certain number of miles off
	 * the most direct route. 
	 * @param entries a Collection of GeoLocation objects
	 * @param minDetour the minimum
	 * @return the stripped list of GeoLocations
	 */
	public static <T extends GeoLocation> List<T> stripDetours(Collection<T> entries, int minDetour) {
		LinkedList<T> locs = new LinkedList<T>(entries);
		if (entries.size() > 3) {
			GeoLocation lastP = locs.getFirst(); GeoLocation dest = locs.getLast();
			int distance = GeoUtils.distance(lastP, dest);
			int distThreshold = Math.min(250, Math.max(minDetour, distance / 10));

			// Strip out any points that are too far out of the way
			final Collection<GeoLocation> deletedItems = new LinkedHashSet<GeoLocation>();
			for (int x = 1; x < locs.size() - 2; x++) {
				GeoLocation gl = locs.get(x);
				GeoLocation next = locs.get(x+1);

 				int d0 = distance(lastP, gl);
				int d1 = distance(lastP, next);
				int d2 = d0 + distance(gl, next);
				int d3 = distance(lastP, dest);
				
				if (d2 > (d1 + distThreshold))
					deletedItems.add(gl);
				else if (d0 > (distance + distThreshold))
					deletedItems.add(gl);
				else if (d0 > (d3 + distThreshold))
					deletedItems.add(gl);
				else
					lastP = gl;
			}
			
			// Remove the stripped-out points
			locs.removeAll(deletedItems);
		}
		
		return locs;
	}
	
	/**
	 * Parses XACARS-format geolocations. 
	 * @param pos an XACARS format geolocation
	 * @return a GeoLocation
	 */
	public static GeoLocation parseXACARS(String pos) {
		List<String> parts = StringUtils.split(pos, " ");
		if ((parts == null) || (parts.size() < 4))
			return null;
		
		// Parse latitude
		String ltd = parts.get(0);
		Hemisphere lth = Hemisphere.valueOf(ltd.substring(0, 1).toUpperCase());
		int latD = StringUtils.parse(ltd.substring(1), 0);
		double lat = (StringUtils.parse(parts.get(1), 0.0d) / 60.0d + latD) * lth.getLatitudeFactor();
		
		// Parse longitude
		String lnd = parts.get(2);
		Hemisphere lnh = Hemisphere.valueOf(lnd.substring(0, 1).toUpperCase());
		int lngD = StringUtils.parse(lnd.substring(1), 0);
		double lng = (StringUtils.parse(parts.get(3), 0.0d) / 60.0d + lngD) * lnh.getLongitudeFactor();
		
		return new GeoPosition(lat, lng);
	}

	/**
	 * Returns whether a location is valid (ie. not 0/0/0)
	 * @param loc a GeoLocation
	 * @return TRUE if valid, otherwise FALSE
	 */
	public static boolean isValid(GeoLocation loc) {
		if (loc == null) return false;
		boolean isOK = ((loc.getLatitude() != 0.00) || (loc.getLongitude() != 0.00));
		if (loc instanceof GeospaceLocation) {
			GeospaceLocation gsl = (GeospaceLocation) loc;
			isOK |= (gsl.getAltitude() != 0);
		}
		
		return isOK;
	}
	
	/**
	 * Converts a Collection of GeoLocations into a LinearRing object.
	 * @param pts a Collection of GeoLocations
	 * @return a LinearRing object
	 */
	public static LinearRing toRing(Collection<GeoLocation> pts) {
		List<GeoLocation> brd = new ArrayList<GeoLocation>(pts);
		brd.add(brd.get(0));
		List<Coordinate> cts = brd.stream().map(pt -> toCoordinate(pt)).collect(Collectors.toList());
		
		GeometryFactory gf = new GeometryFactory();
		return gf.createLinearRing(cts.toArray(new Coordinate[0]));
	}
	
	/**
	 * Converts a Collection of GeoLocations into a Geometry object.
	 * @param pts a Collection of GeoLocations 
	 * @return a Geometry object
	 */
	public static Geometry toGeometry(Collection<GeoLocation> pts) {
		GeometryFactory gf = new GeometryFactory();
		return gf.createPolygon(toRing(pts), null);
	}

	/**
	 * Parses a Geometry object and converts it into a Collection of GeoLocations.
	 * @param geo a Geometry
	 * @return a Collection of GeoLocations
	 */
	public static Collection<GeoLocation> fromGeometry(Geometry geo) {
		List<Coordinate> coords = Arrays.asList(geo.getCoordinates());
		return coords.stream().map(pt -> fromCoordinate(pt)).collect(Collectors.toList());
	}
	
	/**
	 * Converts a GeoLocation to a JTS Coordinate.
	 * @param loc a GeoLocation
	 * @return a JTS Coordinate
	 */
	public static Coordinate toCoordinate(GeoLocation loc) {
		return new Coordinate(loc.getLatitude(), loc.getLongitude());
	}
	
	/**
	 * Converts a JTS Coordinate to a GeoLocation.
	 * @param c a Coordinate
	 * @return a GeoLocation
	 */
	public static GeoLocation fromCoordinate(Coordinate c) {
		return  new GeoPosition(c.x, c.y);
	}
}