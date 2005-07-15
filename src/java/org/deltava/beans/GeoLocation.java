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

	public double getLatitude();
	public double getLongitude();
}