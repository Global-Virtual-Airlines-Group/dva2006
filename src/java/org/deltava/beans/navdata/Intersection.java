// Copyright 2005, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * A bean to store Intersection data.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class Intersection extends NavigationDataBean {
	
	/**
	 * Creates a new Intersection object.
	 * @param code the intersection code
	 * @param lat the latitude in degrees
	 * @param lon the longitude in degrees
	 */
	public Intersection(String code, double lat, double lon) {
		super(INT, lat, lon);
		setCode(code);
	}

	/**
	 * Returns the Intersection's name. <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	public final String getName() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Updates the Intersection's name. <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	public final void setName(String name) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Return the default Google Maps icon color.
	 * @return MapEntry#White
	 */
	public String getIconColor() {
		return WHITE;
	}
	
	/**
	 * Returns the Google Earth palette code.
	 * @return 3
	 */
	public int getPaletteCode() {
		return 3;
	}
	
	/**
	 * Returns the Google Earth icon code.
	 * @return 56
	 */
	public int getIconCode() {
		return 61;
	}

	/**
	 * Returns the default Google Maps infobox text.
	 * @return an HTML String
	 */
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<span class=\"mapInfoBox\">");
		buf.append(getHTMLTitle());
		buf.append(getHTMLPosition());
		buf.append("</span>");
		return buf.toString();
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
		if (code == null)
			return null;
		else if (code.contains("/") && (code.length() == 5))
			return parse(code.substring(0, 2) + code.substring(3) + "N");
		
		// Determine what type of coordinate we are
		CodeType ct = NavigationDataBean.isCoordinates(code);
		switch (ct) {
			case FULL:
				// Find where the lat ends
				int pos = 0;
				while ((pos < code.length()) && Character.isDigit(code.charAt(pos)))
					pos++;
				
				String latDir = code.substring(pos, pos + 1).toUpperCase();
				String lngDir = code.substring(code.length() - 1).toUpperCase();
				try {
					Hemisphere hLat = Hemisphere.valueOf(latDir);
					Hemisphere hLng = Hemisphere.valueOf(lngDir);
					double lat = Double.parseDouble(code.substring(0, pos)) * hLat.getLatitudeFactor();
					double lng = Double.parseDouble(code.substring(pos + 1, code.length() - 1)) * hLng.getLongitudeFactor();
					return new Intersection(code, lat, lng);
				} catch (Exception e) {
					throw new IllegalArgumentException("Invalid waypoint code - " + code);
				}
				
			case QUADRANT:
				String dir = code.substring(code.length() - 1);
				try {
					Hemisphere h = Hemisphere.valueOf(dir);
					double lat = Double.parseDouble(code.substring(0, 2)) * h.getLatitudeFactor();
					double lng = Double.parseDouble(code.substring(2, code.length() - 1)) * h.getLongitudeFactor();
					return new Intersection(code, lat, lng);
				} catch (Exception e) {
					throw new IllegalArgumentException("Invalid waypoint code - " + code);
				}				
				
			default:
				return null;
		}
	}
}