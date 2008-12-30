// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

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
 * @version 2.3
 * @since 2.3
 */

public class GetGoogleGeocode extends DAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to use
	 */
	public GetGoogleGeocode(InputStream is) {
		super(is);
	}

	/**
	 * Retrieves the Geocoding results for the location.
	 * @throws DAOException if an I/O error occurs
	 */
	public List<GeocodeResult> getGeoData() throws DAOException {
		Document doc = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(getStream());
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
				double lng = StringUtils.parse(tk.nextToken(), 0.0d);
				double lat = StringUtils.parse(tk.nextToken(), 0.0d);
				GeoPosition pos = new GeoPosition(lat, lng);
				
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