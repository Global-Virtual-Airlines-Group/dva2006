// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.filter.ElementFilter;

import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.stats.GeocodeResult;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to do reverse geocoding using the Google HTTP API. The GeoLocation
 * URL is http://maps.google.com/maps/geo?q=(lat),(long)&sensor=false&key=(key)
 * @author Luke
 * @version 2.4
 * @since 2.3
 */

public class GetGoogleGeocode extends DAO {
	
	private String _apiKey;
	
	private InputStream getStream(double lat, double lng) throws IOException {
		StringBuilder buf = new StringBuilder("http://maps.google.com/maps/geo?sensor=false&output=xml&q=");
		buf.append(lat);
		buf.append(',');
		buf.append(lng);
		buf.append("&key=");
		buf.append(_apiKey);
		return getStream(buf.toString());
	}
	
	/**
	 * Sets the Google Maps API key to use for this request.
	 * @param key the API key
	 */
	public void setAPIKey(String key) {
		_apiKey = key;
	}

	/**
	 * Retrieves the Geocoding results for the location.
	 * @param lat the Latitude of the location in degrees
	 * @param lng the Longitude of the location in degrees
	 * @return a List of GeocodeResult beans
	 * @throws DAOException if an I/O error occurs
	 */
	public List<GeocodeResult> getGeoData(double lat, double lng) throws DAOException {
		Document doc = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(getStream(lat, lng));
		} catch (Exception e) {
			throw new DAOException(e);
		}
		
		// Process the document
		Element re = doc.getRootElement();
		List<GeocodeResult> results = new ArrayList<GeocodeResult>();
		for (Iterator i = re.getDescendants(new ElementFilter("Placemark")); i.hasNext(); ) {
			Element ple = (Element) i.next();
			
			// Get the details
			Element de = (Element) ple.getDescendants(new ElementFilter("AddressDetails")).next();
			Element pe = ple.getChild("Point", ple.getNamespace());
			if ((de != null) && (pe != null)) {
				Element ce = de.getChild("Country", de.getNamespace());
				int accuracy = StringUtils.parse(de.getAttributeValue("Accuracy", "1"), 1);
				
				// Get the lat/lon
				StringTokenizer tk = new StringTokenizer(pe.getChildTextTrim("coordinates", pe.getNamespace()), ",");
				GeoPosition pos = new GeoPosition(StringUtils.parse(tk.nextToken(), 0.0d), StringUtils.parse(tk.nextToken(), 0.0d));
				
				// Create the result
				GeocodeResult gr = new GeocodeResult(pos, GeocodeResult.GeocodeAccuracy.values()[accuracy]);
				gr.setCountryCode(ce.getChildTextTrim("CountryNameCode", ce.getNamespace()));
				gr.setCountry(ce.getChildTextTrim("CountryName", ce.getNamespace()));
				
				// Load the state
				Element se = ce.getChild("AdministrativeArea", ce.getNamespace());
				if ((accuracy > 1) && (se != null)) {
					gr.setState(se.getChildTextTrim("AdministrativeAreaName", se.getNamespace()));
					
					// Load the city or county
					Element cte = se.getChild("Locality", se.getNamespace());
					if (cte != null) {
						gr.setCity(cte.getChildTextTrim("LocalityName", cte.getNamespace()));
						Element pce = cte.getChild("PostalCode", cte.getNamespace());
						if (pce != null)
							gr.setPostalCode(pce.getChildTextTrim("PostalCodeNumber", pce.getNamespace()));
						Element ae = cte.getChild("Thoroughfare", cte.getNamespace());
						if (ae != null)
							gr.setAddress(ae.getChildTextTrim("ThoroughfareName", ae.getNamespace()));
					} else
						gr.setCity(se.getChildTextTrim("AddressLine"));
				}
				
				// Add to results
				results.add(gr);
			}			
		}
		
		// Sort the results based on accuracy
		Collections.sort(results, Collections.reverseOrder());
		return results;
	}
}