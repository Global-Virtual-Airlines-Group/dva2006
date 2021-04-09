// Copyright 2011, 2015, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.deltava.beans.Pilot;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to export a Pilot's log book in CSV format. 
 * @author Luke
 * @version 10.0
 * @since 3.6
 */

public class LogbookService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the user ID
		Pilot p = ctx.getUser();
		int userID = p.getID();
		int id = StringUtils.parse(ctx.getParameter("id"), userID);
		if (ctx.isUserInRole("PIREP") || ctx.isUserInRole("HR"))
			userID = id;
		
		// Get the Flight Reports
		Collection<FlightReport> pireps = null;
		try {
			GetFlightReports frdao = new GetFlightReports(ctx.getConnection());
			pireps = frdao.getByPilot(userID, new ScheduleSearchCriteria("DATE, PR.SUBMITTED"));
			frdao.loadCaptEQTypes(userID, pireps, ctx.getDB());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Write the CSV header
		Map<String, Integer> promoCounts = new HashMap<String, Integer>();
		ctx.println("Date,Submitted,Flight,Network,Departed,DCode,Arrived,ACode,Equipment,Distance,Time,ACARS,Promotion");
		for (Iterator<FlightReport> i = pireps.iterator(); i.hasNext(); ) {
			FlightReport fr = i.next();
			
			// Calculate promotion count
			int maxPromoCount = 0;
			for (String captEQ : fr.getCaptEQType()) {
				int promoCount = 0;
				Integer pCount = promoCounts.get(captEQ);
				if (pCount != null)
					promoCount = pCount.intValue();
				
				promoCount++;
				promoCounts.put(captEQ, Integer.valueOf(promoCount));
				maxPromoCount = Math.max(maxPromoCount, promoCount);
			}
			
			// Write data
			ctx.print(StringUtils.format(fr.getDate(), p.getDateFormat()));
			ctx.print(",");
			ctx.print((fr.getSubmittedOn() == null) ? "-" : StringUtils.format(fr.getSubmittedOn(), p.getDateFormat()));
			ctx.print(",");
			ctx.print(fr.getFlightCode());
			ctx.print(",");
			ctx.print((fr.getNetwork() == null) ? "-" : fr.getNetwork().toString());
			ctx.print(",");
			ctx.print(fr.getAirportD().getName());
			ctx.print(",");
			ctx.print((p.getAirportCodeType() == Airport.Code.ICAO) ? fr.getAirportD().getICAO() : fr.getAirportD().getIATA());
			ctx.print(",");
			ctx.print(fr.getAirportA().getName());
			ctx.print(",");
			ctx.print((p.getAirportCodeType() == Airport.Code.ICAO) ? fr.getAirportA().getICAO() : fr.getAirportA().getIATA());
			ctx.print(",");
			ctx.print(fr.getEquipmentType());
			ctx.print(",");
			ctx.print(String.valueOf(fr.getDistance()));
			ctx.print(",");
			ctx.print(StringUtils.format(fr.getLength() / 10.0f, "#0.0"));
			ctx.print(",");
			ctx.print(fr.hasAttribute(FlightReport.ATTR_ACARS) ? "Y" : "-");
			if (maxPromoCount > 0) {
				ctx.print(",");
				ctx.print(String.valueOf(maxPromoCount));
				ctx.print(",");
				ctx.println(StringUtils.listConcat(fr.getCaptEQType(), " "));
			} else
				ctx.println("");
		}
		
		// Write the response
		try {
			ctx.setContentType("text/csv", "utf-8");
			ctx.setHeader("Content-disposition", "attachment; filename=logbook.csv");
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	@Override
	public final boolean isSecure() {
		return true;
	}
}