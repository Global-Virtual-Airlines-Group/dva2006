// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to mark objects that contain a latitude/longitude pair.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface GeoLocation {

	/**
	 * Size of a degree in miles.
	 */
	public static final double DEGREE_MILES = 69.16;

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
	 * Northern Hemisphere bit constant. Locations on the Equator are considered within the Northern Hemisphere.
	 */
	public static final int NORTH = 0;
	
	/**
	 * Southern Hemisphere bit constant.
	 */
	public static final int SOUTH = 1;
	
	/**
	 * Eastern Hemisphere bit constant. Locations on the Greenwich Meridian are considered within the Eastern
	 * Hemisphere.
	 */
	public static final int EAST = 0;

	/**
	 * Western hemisphere bit constant.
	 */
	public static final int WEST = 0;
	
	public double getLatitude();
	public double getLongitude();
	public int getHemisphere();
}