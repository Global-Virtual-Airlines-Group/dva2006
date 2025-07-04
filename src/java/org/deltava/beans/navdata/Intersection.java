// Copyright 2005, 2007, 2008, 2009, 2012, 2014, 2015, 2020, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import org.apache.logging.log4j.*;

/**
 * A bean to store Intersection data.
 * @author Luke
 * @version 11.4
 * @since 1.0
 */

public class Intersection extends NavigationDataBean {
	
	private static final Logger log = LogManager.getLogger(Intersection.class);
	
	/**
	 * Creates a new Intersection object.
	 * @param code the intersection code
	 * @param lat the latitude in degrees
	 * @param lon the longitude in degrees
	 */
	public Intersection(String code, double lat, double lon) {
		super(Navaid.INT, lat, lon);
		setCode(code);
	}

	/**
	 * Returns the Intersection's name. <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public final String getName() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Updates the Intersection's name. <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public final void setName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getIconColor() {
		return WHITE;
	}
	
	@Override
	public int getPaletteCode() {
		return 3;
	}
	
	@Override
	public int getIconCode() {
		return 61;
	}

	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox navdata\">");
		buf.append(getHTMLTitle());
		buf.append(getHTMLPosition());
		buf.append("</div>");
		return buf.toString();
	}
	
	private static double parseDMSLatitude(double dms) {
		
		double lat = dms;
		if (lat >= 100000) {
			double latS = lat % 100;
			double latM = Math.floor(((lat % 10000) - latS) / 100);
			double latD = Math.floor(lat / 10000);
			lat = latD + (latM / 60) + (latS / 3600); 
		} else if (lat >= 1000) {
			double latM = lat % 100;
			double latD = Math.floor(lat / 100);
			lat = latD + (latM / 60);
		}
		
		return lat;
	}
	
	private static double parseDMSLongitude(double dms) {
		
		double lng = dms; 
		if (lng >= 180000) {
			double lngS = lng % 100;
			double lngM = Math.floor(((lng % 10000) - lngS) / 100);
			double lngD = Math.floor(lng / 10000);
			lng = lngD + (lngM / 60) + (lngS / 3600); 
		} else if (lng >= 1800) {
			double lngM = lng % 100;
			double lngD = Math.floor(lng / 100);
			lng = lngD + (lngM / 60);
		}
		
		return lng;
	}
	
	/**
	 * Parses a North Atlantic Track latitude/longitude waypoint code.
	 * @param code the waypoint code
	 * @return an Intersection
	 * @throws IllegalArgumentException if the code is not in the format NN/WW or NNWWN
	 * @throws NullPointerException if code is null
	 * @throws NumberFormatException if the latitude/longitude cannot be parsed
	 */
	public static Intersection parse(String code) {
		if (code == null) return null; 
		int spos = code.indexOf('/');
		if ((spos > 0) && (code.length() == 5))
			return parse(code.substring(0, 2) + code.substring(3) + "N");
		else if ((spos > 0) && (code.length() > 6)) {
			if (!Character.isLetter(code.charAt(spos - 1)) && !Character.isLetter(code.charAt(code.length() - 1))) {
				try {
					double lat = Double.parseDouble(code.substring(0, spos));
					double lng = Double.parseDouble(code.substring(spos + 1));
					return new Intersection(code, parseDMSLatitude(lat), -parseDMSLongitude(lng));
				} catch (NumberFormatException nfe) {
					log.warn("Unparseable waypoint - {} / {}", code, nfe.getMessage());
				}
			}
		}
	
		// Determine what type of coordinate we are
		CodeType ct = NavigationDataBean.isCoordinates(code);
		switch (ct) {
			case FULL:
				boolean isPrefixDirection = Hemisphere.isDirection(code.charAt(0));
				
				// Find where the lat ends
				int pos = isPrefixDirection ? 1 : 0;
				while ((pos < code.length()) && Character.isDigit(code.charAt(pos)))
					pos++;
				
				String latDir = isPrefixDirection ? code.substring(0, 1) : code.substring(pos, pos + 1);
				String lngDir = isPrefixDirection ? code.substring(pos, pos+1) : code.substring(code.length() - 1);
				try {
					Hemisphere hLat = Hemisphere.valueOf(latDir.toUpperCase());
					Hemisphere hLng = Hemisphere.valueOf(lngDir.toUpperCase());
					double lat = Double.parseDouble(isPrefixDirection ? code.substring(1, pos) : code.substring(0, pos));
					double lng = Double.parseDouble(isPrefixDirection ? code.substring(pos+1) : code.substring(pos + 1, code.length() - 1));
					return new Intersection(code, parseDMSLatitude(lat) * hLat.getLatitudeFactor(), parseDMSLongitude(lng) * hLng.getLongitudeFactor());
				} catch (Exception e) {
					log.warn("Invalid full waypoint code - {}", code);
					return null;
				}
				
			case QUADRANT:
				String dir = code.substring(code.length() - 1);
				try {
					Hemisphere h = Hemisphere.valueOf(dir);
					double lat = Double.parseDouble(code.substring(0, 2)) * h.getLatitudeFactor();
					double lng = Double.parseDouble(code.substring(2, code.length() - 1)) * h.getLongitudeFactor();
					return new Intersection(code, lat, lng);
				} catch (Exception e) {
					log.warn("Invalid quadrant waypoint code - {}", code);
					return null;
				}
				
			case SLASH:
				try {
					Hemisphere hLat = Hemisphere.valueOf(String.valueOf(code.charAt(spos - 1)).toUpperCase());
					Hemisphere hLng = Hemisphere.valueOf(String.valueOf(code.charAt(code.length() - 1)).toUpperCase());
					double lat = Double.parseDouble(code.substring(0, spos - 1));
					double lng = Double.parseDouble(code.substring(spos + 1, code.length() -1 ));
					return new Intersection(code, parseDMSLatitude(lat) * hLat.getLatitudeFactor(), parseDMSLongitude(lng) * hLng.getLongitudeFactor());
				} catch (Exception e) {
					log.warn("Invalid slash waypoint code - {}", code);
					return null;
				}
				
			default:
				return null;
		}
	}
}