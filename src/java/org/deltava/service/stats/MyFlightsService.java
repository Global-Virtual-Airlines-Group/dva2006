// Copyright 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 2.6
 * @since 2.1
 */

public class MyFlightsService extends WebService {

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
			results = stdao.getPIREPStatistics(userID, labelType, sortType, true);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("pie");
		doc.setRootElement(re);
		
		// Create the entries
		for (FlightStatsEntry entry : results) {
			Element e = new Element("slice");
			e.setAttribute("title", entry.getLabel());
			e.setAttribute("alpha", "75");
			
			// Set value
			switch (sortCode) {
				case 1:
					e.setText(String.valueOf(entry.getMiles()));
					break;
					
				case 2:
					e.setText(StringUtils.format(entry.getHours(), "##0.0"));
					break;
					
				case 3:
					e.setText(StringUtils.format(entry.getAvgHours(), "##0.0"));
					break;
					
				case 4:
					e.setText(StringUtils.format(entry.getAvgMiles(), "##0.0"));
					break;
					
				case 6:
					e.setText(String.valueOf(entry.getACARSLegs()));
					break;
					
				case 7:
					e.setText(String.valueOf(entry.getOnlineLegs()));
					break;
					
				case 8:
					e.setText(String.valueOf(entry.getHistoricLegs()));
					break;

				default:
					e.setText(String.valueOf(entry.getLegs()));
			}

			re.addContent(e);
		}

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