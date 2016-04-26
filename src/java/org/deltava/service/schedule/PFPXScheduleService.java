// Copyright 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.temporal.*;

import org.jdom2.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Serivce to export the Flight Schedule in PFPX format. 
 * @author Luke
 * @version 7.0
 * @since 6.1
 */

public class PFPXScheduleService extends DownloadService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check if the file exists
		File cacheDir = new File(SystemData.get("schedule.cache"));
		File f = new File(cacheDir, SystemData.get("airline.code").toLowerCase() + "_pfpxsched.xml");
		
		// Check if the schedule has been updated
		boolean isInvalid = true;
		try {
			GetMetadata mddao = new GetMetadata(ctx.getConnection());
			String lastImport = mddao.get(SystemData.get("airline.code").toLowerCase() + ".schedule.import", "0");
			long schedAge = StringUtils.parse(lastImport, 0) * 1000L;
			isInvalid = (f.lastModified() == 0) || (schedAge > (f.lastModified() + 60_000));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de); 
		} finally {
			ctx.release();
		}
		
		// Check the cache
		if (!isInvalid) {
			ctx.setHeader("Content-disposition", "attachment; filename=pfpxsched.xml");
			ctx.setContentType("text/xml", "UTF-8");
			ctx.setHeader("max-age", 3600);
			sendFile(f, ctx.getResponse());
			return SC_OK;
		}
		
		// Load the flights
		Collection<ScheduleEntry> sched = null;
		Map<String, Aircraft> acInfo = new HashMap<String, Aircraft>();
		try {
			Connection con = ctx.getConnection();
			
			// Load all aircraft
			GetAircraft acdao = new GetAircraft(con);
			acInfo.putAll(CollectionUtils.createMap(acdao.getAircraftTypes(), "name"));
			
			// Load the schedule
			GetSchedule sdao = new GetSchedule(con);
			sched = sdao.export();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Add to XML
		LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
		String today = String.valueOf(now.get(ChronoField.DAY_OF_WEEK));
		Document doc = new Document();
		Element re = new Element("PFPX_FLIGHT_LIST");
		re.setAttribute("airline", SystemData.get("airline.code"));
		doc.setRootElement(re);
		for (ScheduleEntry se : sched) {
			Aircraft ac = acInfo.get(se.getEquipmentType());
			if (ac == null) continue;
			
			Element fe = new Element("FLIGHT");
			fe.addContent(XMLUtils.createElement("Airline", se.getAirline().getCode()));
			fe.addContent(XMLUtils.createElement("FlightNumber", String.valueOf(se.getFlightNumber())));
			fe.addContent(XMLUtils.createElement("From", se.getAirportD().getICAO()));
			fe.addContent(XMLUtils.createElement("To", se.getAirportA().getICAO()));
			fe.addContent(XMLUtils.createElement("InitAlt", "OPT"));
			fe.addContent(XMLUtils.createElement("MTOW", String.valueOf(ac.getMaxTakeoffWeight())));
			fe.addContent(XMLUtils.createElement("MLW", String.valueOf(ac.getMaxLandingWeight())));
			fe.addContent(XMLUtils.createElement("STD", StringUtils.format(se.getTimeD(), "MM/dd/yyyy HH:mm")));
			fe.addContent(XMLUtils.createElement("STA", StringUtils.format(se.getTimeA(), "MM/dd/yyyy HH:mm")));
			fe.addContent(XMLUtils.createElement("MaxPax", String.valueOf(ac.getSeats())));
			fe.addContent(XMLUtils.createElement("MaxCargo", "-1"));
			fe.addContent(XMLUtils.createElement("Type", "1"));
			fe.addContent(XMLUtils.createElement("Repetative", "1"));
			fe.addContent(XMLUtils.createElement("IsMaster", "1"));
			fe.addContent(XMLUtils.createElement("Unlimited", "Y"));
			fe.addContent(XMLUtils.createElement("Days", today));
			fe.addContent(XMLUtils.createElement("Payload", "-1"));
			fe.addContent(XMLUtils.createElement("Notes", ""));
			re.addContent(fe);
		}
		
		// Write to the file and send to the client
		try {
			try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
				pw.println(XMLUtils.format(doc, "UTF-8"));
			}
			
			ctx.setHeader("Content-disposition", "attachment; filename=pfpxsched.xml");
			ctx.setContentType("text/xml", "UTF-8");
			ctx.setHeader("max-age", 3600);
			sendFile(f, ctx.getResponse());
		} catch (IOException ie) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE
	 */
	@Override
	public boolean isSecure() {
		return true;
	}
}