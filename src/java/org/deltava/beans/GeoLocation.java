// Copyright 2005, 2006, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import org.deltava.beans.schedule.GeoPosition;

/**
 * An interface to mark objects that contain a latitude/longitude pair.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public interface GeoLocation {

	/**
	 * Size of a degree in miles.
	 */
	public static final double DEGREE_MILES = 69.16;
	
	/**
	 * Size of a mile in feet.
	 */
	public static final int FEET_MILES = 5280;
	
	/**
	 * Radius of the Earth in miles.
	 */
	public static final int RADIAN_MILES = 3959;

	/**
	 * Format the Latitude only.
	 * @see org.deltava.util.StringUtils#format(GeoLocation, boolean, int)
	 */
	public static final int LATITUDE = 1;

	/**
	 * Format the Longitude only.
	 * @see org.deltava.util.StringUtils#format(GeoLocation, boolean, int)
	 */
	public static final int LONGITUDE = 2;

	/**
	 * Format the Longitude and Latitude.
	 * @see org.deltava.util.StringUtils#format(GeoLocation, boolean, int)
	 */
	public static final int ALL = 3;

	/**
	 * Latitude directions.
	 */
	public static final String[] LAT_DIRECTIONS = { "North", "South" };

	/**
	 * Longitude directions.
	 */
	public static final String[] LON_DIRECTIONS = { "East", "West" };
	
	/**
	 * Returns the latitude of this location.
	 * @return the latitude in degrees
	 */
	public double getLatitude();
	
	/**
	 * Returns the longitude of this location.
	 * @return the longitude in degrees
	 */
	public double getLongitude();
	
	/**
	 * Calculates the distance between two GeoLocations.
	 * @param gp2 the other GeoLocation
	 * @return The distance in statute miles between the two positions, or -1 if gp2 is null
	 */
	default int distanceTo(GeoLocation gp2) {
	   if (gp2 == null) return -1;

		// Convert the latitude to radians
		double lat1 = Math.toRadians(getLatitude());
		double lat2 = Math.toRadians(gp2.getLatitude());

		// Get the longitude difference in radians
		double lngDiff = Math.toRadians(Math.abs(getLongitude() - gp2.getLongitude()));

		// Do the math - this makes my head hurt
		double p1 = StrictMath.sin(lat1) * StrictMath.sin(lat2);
		double p2 = StrictMath.cos(lat1) * StrictMath.cos(lat2) * StrictMath.cos(lngDiff);
		double distD = Math.toDegrees(Math.acos(p1 + p2));

		// Convert to miles and return
		return (int) StrictMath.round(distD * DEGREE_MILES);
	}
	
	/**
	 * Calculates the distance between two points in feet.
	 * @param l2 the second GeoLocation
	 * @return the distance <i>in feet</i>, or -1 if l1 or l2 are null
	 * @see GeoPosition#distanceTo(GeoLocation)
	 */
	default int distanceFeet(GeoLocation l2) {
		if (l2 == null) return -1;
		
		// Convert the latitude to radians
		double lat1 = Math.toRadians(getLatitude());
		double lat2 = Math.toRadians(l2.getLatitude());
		
		// Get the longitude difference in radians
		double lngDiff = Math.toRadians(Math.abs(getLongitude() - l2.getLongitude()));
		
		// Do the math - this makes my head hurt
		double p1 = StrictMath.sin(lat1) * StrictMath.sin(lat2);
		double p2 = StrictMath.cos(lat1) * StrictMath.cos(lat2) * StrictMath.cos(lngDiff);
		double distD = Math.toDegrees(Math.acos(p1 + p2));
		
		// Convert to miles and return
		return (int) StrictMath.round(distD * DEGREE_MILES * FEET_MILES);
	}
}