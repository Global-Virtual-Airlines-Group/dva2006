// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.time.*;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.*;

/**
 * A sunrise/sunset calculator.
 * @author Luke
 * @version 7.3
 * @since 7.3
 */

@Helper(Instant.class)
public class SunriseSunset {
	
	private static final Instant Y2K = ZonedDateTime.of(2000, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC).toInstant();
	
	// static class
	private SunriseSunset() {
		super();
	}

	/*
	 * Helper method to calculate hour angle for a given time/date. 
	 */
	private static double calculate(GeoLocation loc, Instant dt, boolean isSunset) {
		
		// Calculate Julian day since 1/1/2000 12:00
		double jD = Y2K.until(dt, ChronoUnit.DAYS) + (68.184 / 86400);
		double solarNoon = jD - (loc.getLongitude() / 360);
		
		// Solar mean anomaly
		double M = (357.5291 + (0.98560028 * solarNoon)) % 360;
		
		// Equation of the center
		double C = (1.9148 * Math.sin(M)) + (0.02 * Math.sin(2 * M)) + (0.0003 * Math.sin(3 * M));
		
		// Ecliptic longitude
		double lambda = (M + C + 180 + 102.9372) % 360;
		
		// Calculate solar transit
		double jt = 0.5 + solarNoon + (0.0053 * StrictMath.sin(M)) - (0.0069 * StrictMath.sin(2 * lambda));
		
		// sun declination
		double sunDeclination = StrictMath.asin(StrictMath.sin(lambda) * Math.sin(23.44));
		System.out.println("Decl = " + sunDeclination);
		
		// Calculate hour angle
		double cosW = (Math.sin(-0.83) - Math.sin(loc.getLatitude()) * Math.sin(sunDeclination)) / (Math.cos(loc.getLatitude() * Math.cos(sunDeclination)));
		double W = StrictMath.acos(cosW);
		
		double Wd = (W / 360);
		System.out.println(jt + " " + Wd);
		return isSunset ? (jt + Wd) : (jt - Wd);
	}
	
	public static Instant getSunset(GeoLocation loc, Instant dt) {
		double jT =  calculate(loc, dt, true);
		return Y2K.plusSeconds((long) (jT * 86_400));
	}
	
	public static Instant getSunrise(GeoLocation loc, Instant dt) {
		double jT =  calculate(loc, dt, false);
		return Y2K.plusSeconds((long) (jT * 86_400));
	}
}