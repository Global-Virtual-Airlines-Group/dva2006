// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;
import static org.deltava.commands.stats.AbstractStatsCommand.*;

import java.util.*;

import org.jdom.*;

import org.deltava.beans.stats.FlightStatsEntry;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display Flight Report statistics to an Amline Flash chart.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class AllFlightsService extends WebService {
	
	private static final String[] TITLES = {"Total Flights", "Online Flights", "ACARS Flights", "Historic Flights"};

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check if we're displaying legs or hours
		boolean isHours = Boolean.valueOf(ctx.getParameter("hours")).booleanValue();
		
		// Get the Flight Report statistics - remove the last entry
		List<FlightStatsEntry> results = new ArrayList<FlightStatsEntry>();
		try {
			GetStatistics stdao = new GetStatistics(ctx.getConnection());
			results.addAll(stdao.getPIREPStatistics(0, MONTH_SQL, "F.DATE", false));
			if (!results.isEmpty())
				results.remove(results.size() - 1);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("chart");
		doc.setRootElement(re);
		
		// Generate X-axis
		Element xae = new Element("xaxis");
		re.addContent(xae);
		
		// Generate Y- axes
		Element[] axes = new Element[4];
		Element ae = new Element("graphs");
		re.addContent(ae);
		for (int x = 1; x < 5; x++) {
			Element e = new Element("graph");
			e.setAttribute("gid", String.valueOf(x));
			e.setAttribute("title", TITLES[x - 1]);
			e.setAttribute("unit", " flights");
			axes[x - 1] = e;
			ae.addContent(e);
		}
		
		// Create the entries
		int xid = 1;
		for (FlightStatsEntry entry : results) {
			Element xe = new Element("value");
			xe.setAttribute("xid", String.valueOf(xid));
			xe.setText(entry.getLabel());
			xae.addContent(xe);
			
			// Add the Y-axis
			Element ve = new Element("value");
			ve.setAttribute("xid", String.valueOf(xid));
			
			// Add legs
			Element ve2 = (Element) ve.clone();
			ve2.setText(String.valueOf(isHours ? entry.getHours() : entry.getLegs()));
			axes[0].addContent(ve2);
			
			// Add Online legs
			ve2 = (Element) ve.clone();
			ve2.setText(String.valueOf(entry.getOnlineLegs()));
			axes[1].addContent(ve2);
			
			// Add ACARS legs
			ve2 = (Element) ve.clone();
			ve2.setText(String.valueOf(entry.getACARSLegs()));
			axes[2].addContent(ve2);
			
			// Add historic legs
			ve2 = (Element) ve.clone();
			ve2.setText(String.valueOf(entry.getHistoricLegs()));
			axes[3].addContent(ve2);
			xid++;
		}
		
		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error");
		}
		
		// Return success code
		return SC_OK;
	}
}