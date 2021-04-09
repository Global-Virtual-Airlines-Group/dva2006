// Copyright 2016, 2017, 2018, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simfdr;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import org.jdom2.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.redis.SetTrack;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to process simFDR submitted Flight Reports.
 * @author Luke
 * @version 10.0
 * @since 7.0
 */

public class FlightSubmitService extends SimFDRService {
	
	private static final Logger log = Logger.getLogger(FlightSubmitService.class);
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
		String xml = ctx.getBody();
		StringBuilder flightID = new StringBuilder(ctx.getRequest().getHeader("X-simFDR-FlightID"));
		OfflineFlight<SimFDRFlightReport, ACARSRouteEntry> ofr = OfflineFlightParser.create(xml);
		SimFDRFlightReport fr = ofr.getFlightReport();
		try {
			Connection con = ctx.getConnection();
			
			// Get the user
			GetPilot pdao = new GetPilot(con);
			UserID id = new UserID(ctx.getRequest().getHeader("X-simFDR-User")); Pilot p = null;
			if (id.hasAirlineCode())
				p = pdao.getPilotByCode(id.getUserID(), ctx.getDB());
			else
				p = pdao.get(id.getUserID());
			if (p == null)
				throw error(SC_NOT_FOUND, "Unknown Pilot - " + id, false);
			
			// Save user ID
			flightID.append('-').append(p.getHexID());
			ofr.getInfo().setAuthorID(p.getID());
			fr.setAuthorID(p.getID()); fr.setRank(p.getRank());
			
			// Create comments field
			fr.addStatusUpdate(0, HistoryType.LIFECYCLE, "Submitted for " + p.getName() + " by simFDR from " + ctx.getRequest().getRemoteHost());

			// Init the helper
			FlightSubmissionHelper fsh = new FlightSubmissionHelper(con, fr, p);
			fsh.setAirlineInfo(SystemData.get("airline.code"), ctx.getDB());
			fsh.setACARSInfo(ofr.getInfo());
			
			// Check for Draft PIREPs by this Pilot
			fsh.checkFlightReports();
			
			// Load the aircraft type
			GetAircraft acdao = new GetAircraft(con);
			Aircraft a = acdao.getIATA(fr.getAircraftCode());
			Collection<String> iataCodes = StringUtils.split(fr.getIATACodes(), ",");
			for (Iterator<String> i = iataCodes.iterator(); (i.hasNext() && (a == null)); )
				a = acdao.getIATA(i.next());
			
			// If we don't have an aircraft type
			if (a == null) {
				String acTypes = "ICAO=" +  fr.getAircraftCode() + ", IATA=" + iataCodes;
				log.warn("Unknown aircraft types, " + acTypes);
				throw error(SC_BAD_REQUEST, "Unknown Aircraft - " + acTypes, false);
			} 
			
			// Check ratings / promotion
			fr.setEquipmentType(a.getName());
			fsh.checkRatings();
			
			// Check Online network / Event
			fsh.checkOnlineNetwork();
			fsh.checkOnlineEvent();
			
			// Check Tour
			fsh.checkTour();

			// Check aircraft
			fsh.checkAircraft();
			
			// Check ETOPS / airspace
			fsh.addPositions(ofr.getPositions());
			fsh.checkAirspace();
			
			// Calculate the load factor
			fsh.calculateLoadFactor((EconomyInfo) SystemData.getObject(SystemData.ECON_DATA), false);
			
			// Check for inflight refueling
			fsh.checkRefuel();
			
			// Check the schedule database and check the route pair
			fsh.checkSchedule();
			
			// Calculate average frame rate
			fr.setAverageFrameRate(ofr.getPositions().stream().mapToInt(ACARSRouteEntry::getFrameRate).average().getAsDouble());
			
			// Calculate gates / runways
			fsh.calculateGates();
			fsh.calculateRunways();
				
			// Start transaction
			ctx.startTX();
			
			// Write the ACARS data
			SetACARSRunway awdao = new SetACARSRunway(con);
			awdao.createFlight(ofr.getInfo());
			fr.setDatabaseID(DatabaseID.ACARS, ofr.getInfo().getID());
			awdao.writeRunways(ofr.getInfo().getID(), fsh.getACARSInfo().getRunwayD(), fsh.getACARSInfo().getRunwayA());
			awdao.writeGates(ofr.getInfo().getID(), fsh.getACARSInfo().getGateD(), fsh.getACARSInfo().getGateA());
			awdao.writePositions(ofr.getInfo().getID(), ofr.getPositions());
			
			// Write the flight report
			SetFlightReport fwdao = new SetFlightReport(con);
			fwdao.write(fr);
			fwdao.writeACARS(fr, ctx.getDB());
			if (fwdao.updatePaxCount(fr.getID()))
				log.warn("Update Passnger count for PIREP #" + fr.getID());
			
			// Write ontime data if there is any
			if (fr.getOnTime() != OnTime.UNKNOWN) {
				SetACARSOnTime aowdao = new SetACARSOnTime(con);
				aowdao.write(ctx.getDB(), fr, fsh.getOnTimeEntry());
			}
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Get the track IDs
		CacheableMap<String, MapRouteEntry> trackIDs = _simFDRFlightCache.get(MapRouteEntry.class);
		if (trackIDs == null)
			trackIDs = new CacheableMap<String, MapRouteEntry>(MapRouteEntry.class);
		
		// Clear the track
		trackIDs.remove(flightID.toString());
		SetTrack twdao = new SetTrack();
		twdao.clear(false, flightID.toString());
		_simFDRFlightCache.add(trackIDs);
		
		// Build response
		Document doc = new Document();
		Element re = XMLUtils.createElement("flight", "https://" + SystemData.get("airline.url") + "/pirep.do?id=" + fr.getHexID());
		doc.setRootElement(re);
		re.setAttribute("id", fr.getHexID());
		
		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
}