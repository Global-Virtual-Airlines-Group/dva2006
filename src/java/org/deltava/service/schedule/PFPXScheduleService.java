// Copyright 2015, 2016, 2017, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 10.1
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
			GetRawSchedule rsdao = new GetRawSchedule(ctx.getConnection());
			Collection<ScheduleSourceInfo> srcs = rsdao.getSources(true, ctx.getDB());
			isInvalid = srcs.stream().map(ScheduleSourceInfo::getDate).anyMatch(dt -> dt.toEpochMilli() > f.lastModified());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de); 
		} finally {
			ctx.release();
		}
		
		// Check the cache
		if (!isInvalid) {
			ctx.setHeader("Content-disposition", "attachment; filename=pfpxsched.xml");
			ctx.setContentType("text/xml", "utf-8");
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
			acInfo.putAll(CollectionUtils.createMap(acdao.getAircraftTypes(), Aircraft::getName));
			
			// Load the schedule
			GetRawSchedule rsdao = new GetRawSchedule(con);
			GetSchedule sdao = new GetSchedule(con);
			sdao.setSources(rsdao.getSources(true, ctx.getDB()));
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
			
			AircraftPolicyOptions opts = ac.getOptions(SystemData.get("airline.code"));
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
			fe.addContent(XMLUtils.createElement("MaxPax", String.valueOf(opts.getSeats())));
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
			try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(f), 65536))) {
				pw.println(XMLUtils.format(doc, "UTF-8"));
			}
			
			ctx.setHeader("Content-disposition", "attachment; filename=pfpxsched.xml");
			ctx.setContentType("text/xml", "utf-8");
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