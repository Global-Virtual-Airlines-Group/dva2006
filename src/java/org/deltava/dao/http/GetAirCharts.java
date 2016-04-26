// Copyright 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.*;
import java.time.Instant;
import java.util.*;

import org.jdom2.*;
import org.jdom2.input.*;
import org.jdom2.input.sax.XMLReaders;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to fetch Air Charts data.
 * @author Luke
 * @version 7.0
 * @since 4.0
 */

public class GetAirCharts extends DAO {
	
	private static final Logger log = Logger.getLogger(GetAirCharts.class);
	
	private static final Chart.Type[] CAT_TYPE_MAP = {Chart.Type.UNKNOWN, Chart.Type.GROUND, Chart.Type.SID, Chart.Type.STAR, Chart.Type.APR, Chart.Type.ILS};
	
	/*
	 * Helper method to parse XML from remote HTTP host.
	 */
	private Document loadXML() throws IOException, DAOException {
		int statusCode = getResponseCode();
		if (statusCode != HTTP_OK)
			throw new HTTPDAOException("Invalid Response Code", statusCode);
		
		try (InputStream is = getIn()) {
			SAXBuilder builder = new SAXBuilder(XMLReaders.NONVALIDATING);
			return builder.build(new InputStreamReader(is));
		} catch (JDOMException je) {
			throw new DAOException(je);
		}
	}

	/**
	 * Returns available countries.
	 * @return a Collection of Country beans
	 * @throws DAOException if an error occurs
	 */
	public Collection<Country> getCountries() throws DAOException {
		try {
			init("http://www.aircharts.org/API/countries/XML/" + SystemData.get("security.key.airCharts"));
			Document doc = loadXML();
			
			// Parse the XML
			Collection<Country> results = new TreeSet<Country>();
			Element re = doc.getRootElement();
			for (Element ce : re.getChildren("CountryInfo")) {
				Country c = Country.get(ce.getAttributeValue("CountryCode", ""));
				if (c != null)
					results.add(c);
			}
			
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		} finally {
			reset();
		}
	}

	/**
	 * Returns available airports in a country.
	 * @param c a Country
	 * @return a Collection of Airports
	 * @throws DAOException if an error occurs
	 */
	public Collection<Airport> getAirports(Country c) throws DAOException {
		try {
			init("http://www.aircharts.org/API/airports/" + c.getCode() + "/XML/" + SystemData.get("security.key.airCharts"));
			Document doc = loadXML();
			
			// Parse the XML
			Collection<Airport> results = new TreeSet<Airport>();
			Element re = doc.getRootElement();
			for (Element ae : re.getChildren("AirportInfo")) {
				Airport a = SystemData.getAirport(ae.getAttributeValue("ICAO"));
				if (a != null)
					results.add(a);
			}
			
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		} finally {
			reset();
		}
	}
	
	/**
	 * Returns available charts for an airport.
	 * @param a an Airport
	 * @return a Collection of ExternalCharts
	 * @throws DAOException if an error occurs
	 */
	public Collection<ExternalChart> getCharts(Airport a) throws DAOException {
		try {
			init("http://www.aircharts.org/API/airport/" + a.getICAO() + "/XML/" + SystemData.get("security.key.airCharts"));
			Document doc = loadXML();
			
			// Parse the XML
			Collection<ExternalChart> results = new ArrayList<ExternalChart>();
			Element re = doc.getRootElement();
			for (Element ae : re.getChildren("AirportInfo")) {
				Airport aC = SystemData.getAirport(ae.getAttributeValue("ICAO"));
				if (!a.equals(aC)) {
					log.warn("Unexpected airport code - " + ae.getAttributeValue("ICAO"));
					continue;
				}

				// Walk through categories
				for (Element cce : ae.getChildren("ChartCategory")) {
					int type = StringUtils.parse(cce.getAttributeValue("id"), 0);
					if ((type < 1) || (type >=  CAT_TYPE_MAP.length)) {
						log.warn("Unexpected Chart category - " + cce.getAttributeValue("id"));
						continue;
					}
					
					// Walk through charts
					for (Element ce : cce.getChildren("Chart")) {
						String name = ce.getAttributeValue("Name", "?");
						if (name.length() > 94) {
							log.warn("Truncating chart name " + name);
							name = name.substring(0, 95);
						}
						
						// Extract the ID from the URL
						String url = ce.getTextTrim();
						Map<String, String> params = StringUtils.getURLParameters(url);
						
						// Create the chart
						ExternalChart c = new ExternalChart(name, a);
						c.setImgType(Chart.ImageType.PDF);
						c.setSource("AirCharts");
						c.setType(CAT_TYPE_MAP[type]);
						if ((c.getType() == Chart.Type.ILS) && (!c.getName().contains("ILS")))
							c.setType(Chart.Type.APR);
						
						c.setLastModified(Instant.now());
						c.setURL(url);
						c.setExternalID(params.get("id"));
						results.add(c);
					}
				}
			}
			
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		} finally {
			reset();
		}
	}
}