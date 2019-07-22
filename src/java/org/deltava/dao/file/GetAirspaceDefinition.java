// Copyright 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.DAOException;

import org.deltava.util.*;

/**
 * A Data Access Object to load OpenAir airspace definition files.
 * @author Luke
 * @version 8.6
 * @since 7.3
 */

public class GetAirspaceDefinition extends DAO {
	
	private static final Logger log = Logger.getLogger(GetAirspaceDefinition.class);

	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to read
	 */
	public GetAirspaceDefinition(InputStream is) {
		super(is);
	}

	/**
	 * Load the definition file.
	 * @return a Collection of Airspace beans
	 * @throws DAOException if an error occurs
	 */
	@SuppressWarnings("null")
	public Collection<Airspace> load() throws DAOException {
		
		Collection<Airspace> results = new ArrayList<Airspace>();
		try (LineNumberReader lr = new LineNumberReader(new InputStreamReader(getStream()))) {
			String data = lr.readLine(); Airspace a = null; AirspaceType type = null; boolean isClockwise = true; GeoLocation ctr = null; String name = null;
			while (data != null) {
				data = lr.readLine(); if (StringUtils.isEmpty(data)) continue;
				boolean isComment = (data.charAt(0) == '*') || (data.charAt(0) == '#');
				if (isComment && (a != null) && (data.length() == 1)) {
					results.add(a);
					a = null; ctr = null; name = null;
					isClockwise = true;
					continue;
				} else if (isComment && data.startsWith("*##") && data.endsWith("##")) {
					name = data.substring(4, data.lastIndexOf(' '));
					if (name.startsWith("*"))
						name = name.substring(1);
					
					continue;
				} else if (isComment)
					continue;
				
				String codeType = data.substring(0, data.indexOf(' ')).toUpperCase(); data = data.substring(codeType.length() + 1);
				switch (codeType) {
				case "AC":
					type = AirspaceType.fromName(data);
					break;
					
				case "AN":
					int p2 = data.indexOf(' '); String id = (type.ordinal() < AirspaceType.CTR.ordinal()) ? data.substring(p2) : data; 
					if (((type == AirspaceType.P) || (type == AirspaceType.R)) && id.contains(" "))
						id = name.substring(0, name.indexOf(' '));
					
					if (id.length() > 48)
						id = id.substring(0, 48);
					if (name.length() > 63)
						name = name.substring(0,  64);
					
					a = new Airspace(id, type);
					a.setName(name);
					log.info("Loaded " + a.getID() + " - " + a.getName());
					break;
					
				case "AH":
					String maxAlt = data.replace("MSL", "").trim();
					a.setMaxAltitude("UNLTD".equals(maxAlt) ? 125000 : StringUtils.parse(maxAlt, 18000));
					break;
					
				case "AL":
					String minAlt = data.replace("MSL", "").trim();
					a.setMinAltitude("SFC".equals(minAlt) ? 0 : StringUtils.parse(minAlt, 0));
					break;
					
				case "DP":
					a.addBorderPoint(parseLL(data));
					break;
					
				case "DB": // draw arc between points; convert to angles and radius
					List<String> params = StringUtils.split(data, ",");
					GeoLocation gl1 = parseLL(params.get(0)); GeoLocation gl2 = parseLL(params.get(1));
					double a1 = GeoUtils.course(ctr, gl1); double a2 = GeoUtils.course(ctr, gl2); double dst = ctr.distanceTo(gl1);
					a.setBorder(drawArc(ctr, dst, a1, a2, isClockwise));
					break;
					
				case "DC": // draw circle
					double radius = StringUtils.parse(data, 1.0d);
					a.setBorder(drawArc(ctr, radius, 0, 360, true));
					break;
					
				case "V": // set pt
					int pos2 = data.indexOf('=');
					if (pos2 == -1) continue;
					String subCode = data.substring(0, pos2).toUpperCase();
					if ("D".equals(subCode))
						isClockwise = (data.charAt(pos2 + 1) != '-');
					else if ("X".equals(subCode))
						ctr = parseLL(data.substring(pos2 + 1));
					break;
					
				case "SP":
				case "SB":
				case "AT":
					break;
				
				default:
					log.warn("Unknown entry " + codeType + " at Line " + lr.getLineNumber());
				}
			}
		} catch (Exception ie) {
			throw new DAOException(ie);
		}
		
		return results;
	}
	
	/*
	 * Helper method to parse lat/long coordinates.
	 */
	private static GeoLocation parseLL(String data) {

		List<String> ll = StringUtils.split(data.trim(), " ");
		List<String> lat = StringUtils.split(ll.get(0), ":");
		List<String> lng = StringUtils.split(ll.get(2), ":");
		Hemisphere lt = Hemisphere.valueOf(ll.get(1)); Hemisphere ln = Hemisphere.valueOf(ll.get(3));
		GeoPosition loc = new GeoPosition();
		loc.setLatitude(StringUtils.parse(lat.get(0), 0) * lt.getLatitudeFactor(), StringUtils.parse(lat.get(1), 0), StringUtils.parse(lat.get(2), 0));
		loc.setLongitude(StringUtils.parse(lng.get(0), 0) * ln.getLongitudeFactor(), StringUtils.parse(lng.get(1), 0), StringUtils.parse(lng.get(2), 0));
		return loc;
	}
	
	/*
	 * Helper method to draw an arc around a point.
	 */
	private static Collection<GeoLocation> drawArc(GeoLocation c, double r, double startAngle, double endAngle, boolean isClock) {
		
		// Convert miles to degrees
		double rr = r / GeoLocation.DEGREE_MILES;
		double deltaA = Math.abs(startAngle - endAngle); int pointCt = Math.max(4, (int)(deltaA / 10)); double intervalA = (deltaA / pointCt);
		if (!isClock) intervalA *= -1;
		
		// Plot the circle
		Collection<GeoLocation> results = new ArrayList<GeoLocation>(); double a = startAngle;
		for (int x = 0; x <= pointCt; x++) {
			double ar = Math.toRadians(a);
			double lat = c.getLatitude() + (rr * StrictMath.cos(ar));
			double lng = c.getLongitude() + (rr * StrictMath.sin(ar));
			results.add(new GeoPosition(lat, lng));
			a += intervalA;
		}			
			
		return results;
	}
}