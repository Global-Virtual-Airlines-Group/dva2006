// Copyright 2007, 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;
import static org.deltava.commands.stats.AbstractStatsCommand.*;

import org.jdom.*;

import org.deltava.beans.stats.FlightStatsEntry;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display a Pilot's Flight Report statistics to an Ampie Flash chart.
 * @author Luke
 * @version 3.0
 * @since 2.1
 */

public class MyFlightsService extends WebService {
	
	private static final int MAX_ENTRIES = 8;

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get sorting type
		int sortCode = StringUtils.arrayIndexOf(SORT_CODE, ctx.getParameter("sortType"), 0);
		String sortType = SORT_CODE[sortCode];
		
		// Get grouping type
		String labelType = ctx.getParameter("groupType");
		if (StringUtils.arrayIndexOf(GROUP_CODE, labelType) == -1)
			labelType = GROUP_CODE[2];
		else if (GROUP_CODE[6].equals(labelType))
			labelType = MONTH_SQL;
		
		// Get the user ID
		int userID = ctx.getUser().getID();
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		if ((ctx.isUserInRole("PIREP") || ctx.isUserInRole("HR")) && (id > 0))
			userID = id;

		// Get the Flight Report statistics
		Collection<FlightStatsEntry> results = null;
		try {
			GetFlightReportStatistics stdao = new GetFlightReportStatistics(ctx.getConnection());
			results = stdao.getPIREPStatistics(userID, labelType, sortType, true, false);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("pie");
		doc.setRootElement(re);
		
		// Create element for "everything else"
		Element ee = new Element("slice");
		ee.setAttribute("title", "All Others");
		ee.setAttribute("alpha", "75");
		
		// Create the entries
		int entryCount = 0; double eeValue = 0;
		for (FlightStatsEntry entry : results) {
			entryCount++;
			Element e = new Element("slice");
			e.setAttribute("title", entry.getLabel());
			e.setAttribute("alpha", "75");
			
			// Set value
			switch (sortCode) {
				case 1:
					if (entryCount <= MAX_ENTRIES)
						e.setText(String.valueOf(entry.getMiles()));
					else
						eeValue += entry.getMiles();
					break;
					
				case 2:
					if (entryCount <= MAX_ENTRIES)
						e.setText(StringUtils.format(entry.getHours(), "##0.0"));
					else
						eeValue += entry.getHours();
					break;
					
				case 3:
					if (entryCount <= MAX_ENTRIES)
						e.setText(StringUtils.format(entry.getAvgHours(), "##0.0"));
					else
						eeValue += entry.getAvgHours();
					break;
					
				case 4:
					if (entryCount <= MAX_ENTRIES)
						e.setText(StringUtils.format(entry.getAvgMiles(), "##0.0"));
					else
						eeValue += entry.getAvgMiles();
					break;
					
				case 6:
					if (entryCount <= MAX_ENTRIES)
						e.setText(String.valueOf(entry.getACARSLegs()));
					else
						eeValue += entry.getACARSLegs();
					break;
					
				case 7:
					if (entryCount <= MAX_ENTRIES)
						e.setText(String.valueOf(entry.getOnlineLegs()));
					else
						eeValue += entry.getOnlineLegs();
					break;
					
				case 8:
					if (entryCount <= MAX_ENTRIES)
						e.setText(String.valueOf(entry.getHistoricLegs()));
					else
						eeValue += entry.getHistoricLegs();
					break;

				default:
					if (entryCount <= MAX_ENTRIES)
						e.setText(String.valueOf(entry.getLegs()));
					else
						eeValue += entry.getLegs();
			}

			if (entryCount <= MAX_ENTRIES)
				re.addContent(e);
		}
		
		// Add the "everything else" entry
		ee.setText(String.valueOf(eeValue));
		re.addContent(ee);

		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.getResponse().setCharacterEncoding("UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		// Return success code
		return SC_OK;
	}
	
	   /**
	    * Returns wether this web service requires authentication.
	    * @return TRUE always
	    */
	   public final boolean isSecure() {
	      return true;
	   }
}