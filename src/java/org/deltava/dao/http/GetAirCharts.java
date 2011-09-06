// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.input.*;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to fetch Air Charts data.
 * @author Luke
 * @version 4.0
 * @since 4.0
 */

public class GetAirCharts extends DAO {
	
	private static final Logger log = Logger.getLogger(GetAirCharts.class);
	
	private static final int[] CAT_TYPE_MAP = {Chart.UNKNOWN, Chart.GROUND, Chart.SID, Chart.STAR, Chart.APR, Chart.ILS};
	
	/**
	 * Helper method to parse XML from remote HTTP host.
	 */
	private Document loadXML() throws IOException, DAOException {
		int statusCode = getResponseCode();
		if (statusCode != HTTP_OK)
			throw new HTTPDAOException("Invalid Response Code", statusCode);
		
		try {
			SAXBuilder builder = new SAXBuilder(false);
			return builder.build(new InputStreamReader(getIn()));
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
			for (Iterator<?> i = re.getChildren("CountryInfo").iterator(); i.hasNext(); ) {
				Element ce = (Element) i.next();
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
			for (Iterator<?> i = re.getChildren("AirportInfo").iterator(); i.hasNext(); ) {
				Element ae = (Element) i.next();	
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
			for (Iterator<?> i = re.getChildren("AirportInfo").iterator(); i.hasNext(); ) {
				Element ae = (Element) i.next();
				Airport aC = SystemData.getAirport(ae.getAttributeValue("ICAO"));
				if (!a.equals(aC)) {
					log.warn("Unexpected airport code - " + ae.getAttributeValue("ICAO"));
					continue;
				}

				// Walk through categories
				for (Iterator<?> cci = ae.getChildren("ChartCategory").iterator(); cci.hasNext(); ) {
					Element cce = (Element) cci.next();
					int type = StringUtils.parse(cce.getAttributeValue("id"), 0);
					if ((type < 1) || (type >=  CAT_TYPE_MAP.length)) {
						log.warn("Unexpected Chart category - " + cce.getAttributeValue("id"));
						continue;
					}
					
					// Walk through charts
					for (Iterator<?> ci = cce.getChildren("Chart").iterator(); ci.hasNext(); ) {
						Element ce = (Element) ci.next();
						ExternalChart c = new ExternalChart(ce.getAttributeValue("Name"), a);
						c.setImgType(Chart.PDF);
						c.setSource("AirCharts");
						c.setType(CAT_TYPE_MAP[type]);
						if ((c.getType() == Chart.ILS) && (!c.getName().contains("ILS")))
							c.setType(Chart.APR);
						
						c.setLastModified(new Date());
						c.setURL(ce.getTextTrim());
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