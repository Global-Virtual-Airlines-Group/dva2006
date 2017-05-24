// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.GeoLocation;

/**
 * A utility class to calculate sunrise and sunset times.
 * @author Luke
 * @version 7.4
 * @since 7.4
 */

public class SunriseSunset {

	// Static class
	private SunriseSunset() {
		super();
	}
	
	private static ZonedDateTime getTime(GeoLocation loc, ZonedDateTime zdt, boolean isSunrise, Twilight z) {
		
		int day = zdt.get(ChronoField.DAY_OF_YEAR);
		
		// Convert longitude to hour value and calculate approximate time
		double lnHour = loc.getLongitude() / 15.0;
		double t = isSunrise ? (day + ((6 - lnHour) / 24)) : (day + ((18 - lnHour) / 24));
	
		 // Calculate the Sun's mean anomaly
		double M = (0.9856 * t) - 3.289;
		
		// Calculate the Sun's true longitude
	    double L = M + (1.916 * Math.sin(Math.toRadians(M))) + (0.020 * Math.sin(Math.toRadians(2 * M))) + 282.634;
	    if (L > 360)
	        L -= 360;
	    else if (L < 0)
	        L += 360;
	    
	    // Calculate the Sun's right ascension
	    double RA = Math.toDegrees(Math.atan(0.91764 * Math.tan(Math.toRadians(L))));
	    if (RA > 360)
	        RA -= 360;
	    else if (RA < 0)
	        RA += 360;
	    
	    // Right ascension value needs to be in the same quadrant and converted to hours
	    double Lquadrant = (Math.floor(L / (90))) * 90;
	    double RAquadrant = (Math.floor(RA / 90)) * 90;
	    RA = (RA + (Lquadrant - RAquadrant)) / 15;
	    
	    // Calculate the Sun's declination
	    double sinDec = 0.39782 * Math.sin(Math.toRadians(L));
	    double cosDec = Math.cos(Math.asin(sinDec));
	    
	    //calculate the Sun's local hour angle
	    double cosH = (Math.cos(Math.toRadians(z.getDegrees())) - (sinDec * Math.sin(Math.toRadians(loc.getLatitude())))) / (cosDec * Math.cos(Math.toRadians(loc.getLatitude())));
	    double H = (isSunrise ? (360 - Math.toDegrees(Math.acos(cosH))) : Math.toDegrees(Math.acos(cosH))) / 15;
	    
	    // Calculate local mean time of rising/setting
	    double T = H + RA - (0.06571 * t) - 6.622;
	    
	    // Adjust back to UTC and local time
	    double UT = T - lnHour + (zdt.getOffset().getTotalSeconds() / 3600.0);
	    if (UT > 24)
	    		UT -= 24;
	    else if (UT < 0)
	    		UT += 24;
	    
	    // Convert to milliseconds and adjust to the date
	    long ms = (long)(UT * 3600.0d);
	    return (ms == 0) ? null : zdt.truncatedTo(ChronoUnit.DAYS).plusSeconds(ms); 
	}
	
	/**
	 * Calculates sunrise time for a given location and date.
	 * @param loc the GeoLocation
	 * @param dt a ZonedDateTime
	 * @return the zoned time of sunrise, or null if none
	 */
	public static ZonedDateTime getSunrise(GeoLocation loc, ZonedDateTime dt) {
		return getTime(loc, dt, true, Twilight.EXACT);
	}

	/**
	 * Calculates sunset time for a given location and date.
	 * @param loc the GeoLocation
	 * @param dt a ZonedDateTime
	 * @return the zoned time of sunset, or null if none
	 */
	public static ZonedDateTime getSunset(GeoLocation loc, ZonedDateTime dt) {
		return getTime(loc, dt, false, Twilight.EXACT);
	}
}