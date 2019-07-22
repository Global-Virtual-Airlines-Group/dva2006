// Copyright 2005, 2006, 2007, 2008, 2010, 2012, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.*;

/**
 * A class for working with latitude/longitude pairs.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public class GeoPosition implements GeospaceLocation, java.io.Serializable {

	private double _lat;
	private double _lon;
	private int _alt;

	/**
	 * Creates a new GeoPosition object
	 */
	public GeoPosition() {
		super();
	}

	/**
	 * Creates a new GeoPosition object with a particular latitude and longitude.
	 * @param lat the latitude in degrees (and some fraction thereof)
	 * @param lon the longitude in degrees (and some fraction thereof)
	 * @param altitude the altitude in feet above mean sea level
	 * @throws IllegalArgumentException if latitude or longitude fail validation
	 * @see GeoPosition#setLatitude(double)
	 * @see GeoPosition#setLongitude(double)
	 * @see GeoPosition#setAltitude(int)
	 */
	public GeoPosition(double lat, double lon, int altitude) {
		super();
		setLatitude(lat);
		setLongitude(lon);
		setAltitude(altitude);
	}
	
	/**
	 * Creates a new GeoPosition object with a particular latitude and longitude.
	 * @param lat The latitude in degrees (and some fraction thereof)
	 * @param lon The longitude in degrees (and some fraction thereof)
	 * @throws IllegalArgumentException if latitude or longitude fail validation
	 * @see GeoPosition#setLatitude(double)
	 * @see GeoPosition#setLongitude(double)
	 */
	public GeoPosition(double lat, double lon) {
		this(lat, lon, 0);
	}
	
	/**
	 * Creates a GeoPosition object from another object that supports lat/lon coordinates. This
	 * is useful when &quot;lightening&quot; an object down to a simple coordinate pair.
	 * @param gl the GeoLocation
	 */
	public GeoPosition(GeoLocation gl) {
		this(gl.getLatitude(), gl.getLongitude());
		if (gl instanceof GeospaceLocation)
			_alt = ((GeospaceLocation) gl).getAltitude();
	}

	/**
	 * Helper method to return back the degrees component of a latitude or longitude.
	 * @param latlon The latitude or longitude
	 * @return The degrees component of the position
	 */
	public static int getDegrees(double latlon) {
		return (int) latlon;
	}

	/**
	 * Helper method to return back the minutes component of a latitude or longitude.
	 * @param latlon The latitude or longitude
	 * @return The minutes component of the position
	 */
	public static int getMinutes(double latlon) {
		double ll = Math.abs(latlon); // Strip out sign since minutes are always positive
		ll -= StrictMath.floor(ll); // Strip out degrees
		return (int) StrictMath.floor(ll * 60); // multiply by 60 so we get minutes
	}

	/**
	 * Helper method to return back the seconds component of a latitude or longitude.
	 * @param latlon The latitude or longitude
	 * @return The seconds component of the position
	 */
	public static double getSeconds(double latlon) {
		double ll = Math.abs(latlon); // Strip out sign since seconds are always positive
		ll -= StrictMath.floor(ll); // Strip out degrees
		ll *= 60; // multiply by 60 so we get minutes
		ll -= StrictMath.floor(ll); // Strip out minutes
		return (int) (ll * 60);
	}
	
	/**
	 * Get the latitude of this position.
	 * @return the latitude in degrees (and some fraction thereof) (values < 0 are South of the Equator)
	 */
	@Override
	public double getLatitude() {
		return _lat;
	}

	/**
	 * Get the longitude of this position.
	 * @return the longitude in degrees (and some fraction thereof) (values < 0 are West of the Greenwich Meridian)
	 */
	@Override
	public double getLongitude() {
		return _lon;
	}
	
	/**
	 * Returns the altitude of this position.
	 * @return the altitude in feet above mean sea level, or 0 if undefined
	 */
	@Override
	public int getAltitude() {
		return _alt;
	}
	
	/**
	 * Updates the altitude of this position.
	 * @param alt the altitude in feet above mean sea level, or 0 if undefined
	 */
	public void setAltitude(int alt) {
		_alt = alt;
	}

	/**
	 * Sets the latitude and ensures its validity.
	 * @param lat The latitude to set in degrees (values < 0 are South of the Equator)
	 */
	public void setLatitude(double lat) {
		_lat = Math.max(-89.99999, Math.min(89.99999, lat));
	}

	/**
	 * Sets the longitude and ensures its validity. If longitude values of greater than 180 degrees or
	 * less than -180 are specified, they are "wrapped" around the International Date line
	 * @param lng The longitude to set in degrees (values < 0 are West of the Greenwich Meridian)
	 */
	public void setLongitude(double lng) {
		double lon = lng;
		if (lng > 180)
			lon = -180 + (lng - 180.0);
		else if (lng < -180)
			lon = 180 + (lng + 180.0);
		
		_lon = Math.max(-179.99999, Math.min(179.99999, lon));
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
		double p1 = StrictMath.cos(lat2) * StrictMath.cos(lngDiff);
		double p2 = StrictMath.cos(lat2) * StrictMath.sin(lngDiff);
		double lat3 = StrictMath.atan2(StrictMath.sin(lat1) + StrictMath.sin(lat2), 
				StrictMath.sqrt((StrictMath.cos(lat1) + p1) * (StrictMath.cos(lat1) + p1) + (p2 * p2)));
		double lon3 = lon1 + StrictMath.atan2(p2, StrictMath.cos(lat1) + p1);

		// Convert the latitude/longitude pair to a geoPosition
		return new GeoPosition(Math.toDegrees(lat3), Math.toDegrees(lon3));
	}

	/**
	 * Calculates equality by determining if the two positions are within 1 mile of each other.
	 * @see GeoPosition#distanceTo(GeoLocation)
	 */
	@Override
	public boolean equals(Object o2) {
		if (!(o2 instanceof GeoLocation)) return false;
		if (o2 == this) return true;
		GeoLocation l2 = (GeoLocation) o2;
		return (Math.abs(l2.getLatitude() - _lat) < 0.0001) && (Math.abs(l2.getLongitude() - _lon) < 0.0001);  
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(String.valueOf(_lat));
		buf.append(',');
		buf.append(String.valueOf(_lon));
		return buf.toString();
	}
}