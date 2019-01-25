// Copyright 2008, 2009, 2010, 2012, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;

import org.jdom2.*;
import org.jdom2.input.*;
import org.jdom2.filter.ElementFilter;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.stats.GeocodeResult;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to do reverse geocoding using the Google HTTP API. The GeoLocation
 * URL is https://maps.google.com/maps/geo?q=(lat),(long)&sensor=false&key=(key)
 * @author Luke
 * @version 8.6
 * @since 2.3
 */

public class GetGoogleGeocode extends DAO {
	
	private InputStream getStream(GeoLocation loc) throws IOException {
		StringBuilder buf = new StringBuilder("https://maps.googleapis.com/maps/api/geocode/xml?sensor=false&oe=utf-8&key=");
		buf.append(SystemData.get("security.key.google"));
		buf.append("&address=");
		buf.append(loc.getLatitude());
		buf.append(',');
		buf.append(loc.getLongitude());
		init(buf.toString());
		return getIn();
	}
	
	/**
	 * Retrieves the Geocoding results for the location.
	 * @param loc the GeoLocation
	 * @return a List of GeocodeResult beans
	 * @throws DAOException if an I/O error occurs
	 */
	public GeocodeResult getGeoData(GeoLocation loc) throws DAOException {
		Document doc = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(new InputStreamReader(getStream(loc), "UTF-8"));
		} catch (Exception e) {
			throw new DAOException(e);
		}
		
		// Process the document
		Element re = doc.getRootElement();
		Element rse = re.getChild("result");
		if (rse == null)
			return null;
		
		// Get the address for sanity checking
		String addr = rse.getChildTextTrim("formatted_address");
		if (StringUtils.isEmpty(addr))
			return null;
		
		GeocodeResult result = new GeocodeResult();
		result.setAddress(addr);
		for (Element ace : rse.getDescendants(new ElementFilter("address_component"))) {
			String type = ace.getChildTextTrim("type");
			if (type == null)
				continue;
			
			String value = ace.getChildTextTrim("long_name");
			if (value == null)
				value = ace.getChildTextTrim("short_name");
			
			// Set fields
			switch (type) {
				case "locality":
					result.setCity(value);
					break;
					
				case "postal_code":
					result.setPostalCode(value);
					break;
					
				case "country":
					result.setCountry(value);
					result.setCountryCode(ace.getChildTextTrim("short_name"));
					break;
					
				case "administrative_area_level_1":
					result.setState(value);
					result.setStateCode(ace.getChildTextTrim("short_name"));
					break;
					
				default:
					break;
			}
		}
			
		// Get the lat/lon
		Element le = rse.getChild("geometry").getChild("location");
		result.setLocation(new GeoPosition(StringUtils.parse(le.getChildTextTrim("lat"), 0.0d), StringUtils.parse(le.getChildTextTrim("lng"), 0.0d)));
		return result;
	}
}