// Copyright 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.*;
import java.time.Instant;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.filter.ElementFilter;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to download FAA chart metadata.
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public class GetFAACharts extends DAO {

	private static final Logger log = Logger.getLogger(GetFAACharts.class);
	private static final String[] TYPES = {"???", "IAP", "IAP", "STAR", "DP", "APD"};
	
	/**
	 * Loads the FAA chart metadata file.
	 * @param url the URL to fetch from
	 * @return a Map of Collections of ExternalCharts, keyed by Airport
	 * @throws DAOException if an error occurs
	 */
	public Collection<AirportCharts<ExternalChart>> getChartList(String url) throws DAOException {
		
		Document doc = null;
		try {
			init(url);
			try (InputStream in = getIn()) {
				SAXBuilder builder = new SAXBuilder();
				doc = builder.build(in);
			}
		} catch (Exception e) {
			throw new DAOException(e);
		} finally {
			reset();
		}

		// Load the charts
		Collection<AirportCharts<ExternalChart>> results = new ArrayList<AirportCharts<ExternalChart>>();
		Element re = doc.getRootElement();
		for (Element ae : re.getDescendants(new ElementFilter("airport_name"))) {
			String icao = ae.getAttributeValue("icao_ident").toUpperCase();
			Airport a = SystemData.getAirport(icao);
			if (a == null) {
				log.info("Skipping airport " + icao);
				continue;
			}

			// Parse the charts per airport
			AirportCharts<ExternalChart> charts = new AirportCharts<ExternalChart>(a);
			for (Element ce : ae.getDescendants(new ElementFilter("record"))) {
				String chartName = ce.getChildTextTrim("chart_name").replace(",", "").replace("  ", " ");
				
				// Check for deletions
				String opCode = ce.getChildTextTrim("useraction");
				if ("D".equals(opCode)) {
					log.info("Skipping deleted chart " + chartName);
					continue;
				}

				ExternalChart c = new ExternalChart(chartName, a);
				c.setLastModified(Instant.now());
				c.setImgType(Chart.ImageType.PDF);
				c.setExternalID(ce.getChildTextTrim("pdf_name"));
				c.setSource("FAA");
				String typeCode = ce.getChildTextTrim("chart_code");
				c.setType(Chart.Type.values()[StringUtils.arrayIndexOf(TYPES, typeCode, 0)]);
				if ((c.getType() == Chart.Type.ILS) && (!c.getName().contains("ILS")))
					c.setType(Chart.Type.APR);
				else if ((c.getType() == Chart.Type.UNKNOWN) && ("DPO".equals(typeCode)))
					c.setType(Chart.Type.SID);
				else if ((c.getType() == Chart.Type.UNKNOWN) && ("MIN".equals(typeCode)))
					c.setType(Chart.Type.MIN);
				
				if (c.getType() == Chart.Type.UNKNOWN)
					log.warn("Unknown chart type - " + typeCode);
				else
					charts.add(c);
			}
			
			results.add(charts);
		}

		return results;
	}
	
	/**
	 * Fetches the size of an FAA chart.
	 * @param ec the ExternalChart
	 * @throws DAOException if an error occurs
	 */
	public void loadSize(ExternalChart ec) throws DAOException {
		try {
			setMethod("HEAD");
			init(ec.getURL());
			ec.setSize(StringUtils.parse(getHeaderField("Content-Length"), 0));
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}

	/**
	 * Populates an FAA chart.
	 * @param ec the ExternalChart
	 * @throws DAOException if an error occurs
	 */
	public void load(ExternalChart ec) throws DAOException {
		try {
			init(ec.getURL());
			try (InputStream in = getIn()) {
				ec.load(in);
				ec.setLastModified(Instant.now());
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}