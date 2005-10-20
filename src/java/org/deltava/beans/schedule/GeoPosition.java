package org.deltava.beans.schedule;

import org.deltava.beans.GeoLocation;

/**
 * A class for working with latitude/longitude pairs.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GeoPosition implements GeoLocation, java.io.Serializable {

	private double _lat;
	private double _lon;

	/**
	 * Creates a new GeoPosition object
	 */
	public GeoPosition() {
		super();
	}

	/**
	 * Creates a new GeoPosition object with a particular latitude and longitude
	 * @param lat The latitude in degrees (and some fraction thereof)
	 * @param lon The longitude in degrees (and some fraction thereof)
	 * @throws IllegalArgumentException if latitude or longitude fail validation
	 * @see GeoPosition#setLatitude(double)
	 * @see GeoPosition#setLongitude(double)
	 */
	public GeoPosition(double lat, double lon) {
		super();
		setLatitude(lat);
		setLongitude(lon);
	}
	
	/**
	 * Creates a GeoPosition object from another object that supports lat/lon coordinates. This
	 * is useful when &quot;lightening&quot; an object down to a simple coordinate pair.
	 * @param gl the GeoLocation
	 */
	public GeoPosition(GeoLocation gl) {
		this(gl.getLatitude(), gl.getLongitude());
	}

	/**
	 * Helper method to return back the minutes component of a latitude or longitude.
	 * @param latlon The latitude or longitude
	 * @return The degrees component of the position
	 */
	public static int getDegrees(double latlon) {
		int deg = new Double(Math.floor(latlon)).intValue();
		return (deg < 0) ? ++deg : deg; // Increment by 1 if we're below 0 since Math.floor(-11.0001) ==
													  // -12.00
	}

	/**
	 * Helper method to return back the minutes component of a latitude or longitude.
	 * @param latlon The latitude or longitude
	 * @return The minutes component of the position
	 */
	public static int getMinutes(double latlon) {
		latlon = Math.abs(latlon); // Strip out sign since minutes are always positive
		latlon -= Math.floor(latlon); // Strip out degrees
		return new Double(Math.floor(latlon * 60)).intValue(); // multiply by 60 so we get minutes
	}

	/**
	 * Helper method to return back the seconds component of a latitude or longitude.
	 * @param latlon The latitude or longitude
	 * @return The seconds component of the position
	 */
	public static int getSeconds(double latlon) {
		latlon = Math.abs(latlon); // Strip out sign since seconds are always positive
		latlon -= Math.floor(latlon); // Strip out degrees
		latlon *= 60; // multiply by 60 so we get minutes
		latlon -= Math.floor(latlon); // Strip out minutes
		return new Double(latlon * 60).intValue();
	}

	/**
	 * Get the latitude of this position.
	 * @return The latitude in degrees (and some fraction thereof) (values < 0 are South of the Equator)
	 */
	public double getLatitude() {
		return _lat;
	}

	/**
	 * Get the longitude of this position.
	 * @return The longitude in degrees (and some fraction thereof) (values < 0 are West of the Greenwich Meridian)
	 */
	public double getLongitude() {
		return _lon;
	}

	/**
	 * Sets the latitude and ensures its validity
	 * @param lat The latitude to set in degrees (values < 0 are South of the Equator)
	 * @throws IllegalArgumentException The latitude is < 90 or > -90 degrees
	 */
	public void setLatitude(double lat) {
		if (Math.abs(lat) > 90)
			throw new IllegalArgumentException("Latitude cannot exceed 90 degrees");

		_lat = lat;
	}

	/**
	 * Sets the longitude and ensures its validity. If longitude values of greater than 180 degrees or
	 * less than -180 are specified, they are "wrapped" around the International Date line
	 * @param lng The longitude to set in degrees (values < 0 are West of the Greenwich Meridian)
	 */
	public void setLongitude(double lng) {
		if (lng > 180) {
			_lon = -180 + (lng - 180.0);
		} else if (lng < -180) {
			_lon = 180 + (lng + 180.0);
		} else {
			_lon = lng;
		}
	}

	/**
	 * Set the latitude using a degree/minute/second combo.
	 * @param deg Degrees (values < 0 are South of the Equator)
	 * @param min Minutes (can be > 59)
	 * @param sec Seconds (can be > 59)
	 * @throws IllegalArgumentException The latitude is < 90 or > -90 degrees
	 * @see GeoPosition#setLatitude(double)
	 */
	public void setLatitude(int deg, int min, int sec) {
		double lat = (Math.abs(deg) + (min / 60.0) + (sec / 3600.0)) * ((deg < 0) ? -1 : 1);
		setLatitude(lat);
	}

	/**
	 * Set the longitude using a degree/minute/second combo.
	 * @param deg Degrees (values < 0 are West of the Greenwich Meridian)
	 * @param min Minutes (can be > 59)
	 * @param sec Seconds (can be > 59)
	 * @throws IllegalArgumentException The latitude is < 90 or > -90 degrees
	 * @see GeoPosition#setLongitude(double)
	 */
	public void setLongitude(int deg, int min, int sec) {
		double lng = (Math.abs(deg) + (min / 60.0) + (sec / 3600.0)) * ((deg < 0) ? -1 : 1);
		setLongitude(lng);
	}
	
	/**
	 * Returns the Hemispheres that contain this location.
	 * @return bit-wise hemisphere constants
	 * @see GeoLocation#NORTH
	 * @see GeoLocation#SOUTH
	 * @see GeoLocation#EAST
	 * @see GeoLocation#WEST
	 */
	public int getHemisphere() {
	   int results = (_lat >= 0) ? NORTH : SOUTH;
	   results += (_lon >= 0) ? EAST : WEST;
	   return results;
	}

	/**
	 * Calculates the distance between two GeoPositions.
	 * @param gp2 the other position
	 * @return The distance in natuical miles between the two positions, or -1 if gp2 is null
	 */
	public int distanceTo(GeoLocation gp2) {
	   
	   // Do null check
	   if (gp2 == null)
	      return -1;

		// Convert the latitude to radians
		double lat1 = Math.toRadians(getLatitude());
		double lat2 = Math.toRadians(gp2.getLatitude());

		// Get the longitude difference in radians
		double lngDiff = Math.toRadians(Math.abs(getLongitude() - gp2.getLongitude()));

		// Do the math - this makes my head hurt
		double p1 = Math.sin(lat1) * Math.sin(lat2);
		double p2 = Math.cos(lat1) * Math.cos(lat2) * Math.cos(lngDiff);
		double distD = Math.toDegrees(Math.acos(p1 + p2));

		// Convert to miles and return
		return new Long(Math.round(distD * DEGREE_MILES)).intValue();
	}

	/**
	 * Calculates the midpoint of a Great Circle route between two GeoPositions.
	 * @param gp2 the other Position
	 * @return a GeoPosition storing the midpoint between the two positions
	 * @throws NullPointerException if gp2 is null
	 */
	public GeoPosition midPoint(GeoLocation gp2) {

		// Convert the latitude to radians
		double lat1 = Math.toRadians(getLatitude());
		double lat2 = Math.toRadians(gp2.getLatitude());

		// Convert the longtitude to radians
		double lon1 = Math.toRadians(getLongitude());
		double lon2 = Math.toRadians(gp2.getLongitude());

		// Get the longitude difference in radians
		double lngDiff = lon2 - lon1;

		// Do the math - this makes my head hurt
		double p1 = Math.cos(lat2) * Math.cos(lngDiff);
		double p2 = Math.cos(lat2) * Math.sin(lngDiff);
		double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + p1) * (Math.cos(lat1) + p1) + (p2 * p2)));
		double lon3 = lon1 + Math.atan2(p2, Math.cos(lat1) + p1);

		// Convert the latitude/longitude pair to a geoPosition
		return new GeoPosition(Math.toDegrees(lat3), Math.toDegrees(lon3));
	}

	/**
	 * Calculates equality by determining if the two positions are within 1 mile of each other.
	 * @see GeoPosition#distanceTo(GeoPosition)
	 */
	public boolean equals(Object o2) {
		return (o2 instanceof GeoPosition) ? (distanceTo((GeoPosition) o2) < 1) : false;
	}
}