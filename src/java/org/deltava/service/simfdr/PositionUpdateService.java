// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simfdr;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.Aircraft;

import org.deltava.dao.*;
import org.deltava.dao.redis.SetTrack;

import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to receive simFDR position updates.
 * @author Luke
 * @version 8.3
 * @since 8.3
 */

public class PositionUpdateService extends SimFDRService {
	
	private static final Cache<CacheableMap<String, MapRouteEntry>> _simFDRFlightCache = CacheManager.getMap(String.class, MapRouteEntry.class, "simFDRFlightID");

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		authenticate(ctx);
		
		// Get the XML
		OfflineFlight<SimFDRFlightReport, ACARSRouteEntry> info = null;
		StringBuilder flightID = new StringBuilder(ctx.getRequest().getHeader("X-simFDR-FlightID"));
		try {
			info = OfflineFlightParser.create(ctx.getBody());
		} catch (Exception e) {
			throw error(SC_BAD_REQUEST, e.getMessage());
		}
		
		Pilot p  = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the user
			GetPilot pdao = new GetPilot(con);
			UserID id = new UserID(ctx.getRequest().getHeader("X-simFDR-User"));
			if (id.hasAirlineCode())
				p = pdao.getPilotByCode(id.getUserID(), SystemData.get("airline.db"));
			else
				p = pdao.get(id.getUserID());
			if (p == null)
				throw error(SC_NOT_FOUND, "Unknown Pilot - " + id, false);
			
			// Load the aircraft type
			GetAircraft acdao = new GetAircraft(con);
			SimFDRFlightReport fr = info.getFlightReport();
			Aircraft a = acdao.getIATA(fr.getAircraftCode());
			Collection<String> iataCodes = StringUtils.split(fr.getIATACodes(), ",");
			for (Iterator<String> i = iataCodes.iterator(); (i.hasNext() && (a == null)); )
				a = acdao.getIATA(i.next());
		
			if (a != null) {
				fr.setEquipmentType(a.getName()); 
				info.getInfo().setEquipmentType(a.getName());
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Add the userID to the flight ID to guarantee uniqueness
		flightID.append('-').append(p.getHexID());
		
		
		// Build the Map entry
		ACARSRouteEntry re = info.getPositions().first(); FlightInfo inf = info.getInfo();
		MapRouteEntry result = new MapRouteEntry(re.getDate(), re, p, info.getInfo().getEquipmentType());
		result.setRecorder(Recorder.SIMFDR);
		result.setSimulator(inf.getSimulator());
		result.setAutopilotType(inf.getAutopilotType());
		result.setClientBuild(info.getFlightReport().getClientBuild(), info.getFlightReport().getBeta());
		result.setLoadFactor(inf.getLoadFactor());
		result.setPassengers(inf.getPassengers());
		result.setDispatchPlan(inf.isDispatchPlan());
		result.setPhaseName(re.getPhase().getName());
		result.setFlightNumber(inf.getFlightCode());
		result.setAirSpeed(re.getAirSpeed());
		result.setGroundSpeed(re.getGroundSpeed());
		result.setVerticalSpeed(re.getVerticalSpeed());
		result.setAltitude(re.getAltitude());
		result.setRadarAltitude(re.getRadarAltitude());
		result.setFlags(re.getFlags());
		result.setFlaps(re.getFlaps());
		result.setHeading(re.getHeading());
		result.setN1(re.getN1());
		result.setN2(re.getN2());
		result.setMach(re.getMach());
		result.setFuelFlow(re.getFuelFlow());
		result.setAirportD(inf.getAirportD());
		result.setAirportA(inf.getAirportA());
		result.setAOA(re.getAOA());
		result.setG(re.getG());
		result.setFuelRemaining(re.getFuelRemaining());
		result.setFrameRate(re.getFrameRate());
		result.setVASFree(re.getVASFree());
		result.setVisibility(re.getVisibility());
		result.setNAV1(re.getNAV1());
		result.setNAV2(re.getNAV2());
		result.setADF1(re.getADF1());
		
		// Get the track IDs
		CacheableMap<String, MapRouteEntry> trackIDs = _simFDRFlightCache.get(MapRouteEntry.class);
		if (trackIDs == null)
			trackIDs = new CacheableMap<String, MapRouteEntry>(MapRouteEntry.class);
		
		// Add our ID
		trackIDs.put(flightID.toString(), result);
		
		// Save the position update and return
		SetTrack twdao = new SetTrack();
		twdao.write(false, flightID.toString(), result);
		_simFDRFlightCache.add(trackIDs);
		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE always
	 */
	@Override
	public final boolean isLogged() {
		return false;
	}
}