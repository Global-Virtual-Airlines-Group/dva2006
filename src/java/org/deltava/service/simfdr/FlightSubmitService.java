// Copyright 2016, 2017, 2018, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
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
import org.deltava.beans.event.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.GeoComparator;

import org.deltava.dao.*;
import org.deltava.dao.redis.SetTrack;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to process simFDR submitted Flight Reports.
 * @author Luke
 * @version 9.0
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
				p = pdao.getPilotByCode(id.getUserID(), SystemData.get("airline.db"));
			else
				p = pdao.get(id.getUserID());
			if (p == null)
				throw error(SC_NOT_FOUND, "Unknown Pilot - " + id, false);
			
			// Save user ID
			flightID.append('-').append(p.getHexID());
			ofr.getInfo().setAuthorID(p.getID());
			fr.setAuthorID(p.getID()); fr.setRank(p.getRank());
			
			// Create comments field
			Collection<String> comments = new LinkedHashSet<String>();
			fr.addStatusUpdate(0, HistoryType.LIFECYCLE, "Submitted for " + p.getName() + " by simFDR from " + ctx.getRequest().getRemoteHost());
			
			// Check for Draft PIREPs by this Pilot
			GetFlightReports prdao = new GetFlightReports(con);
			List<FlightReport> dFlights = prdao.getDraftReports(p.getID(), fr, SystemData.get("airline.db"));
			if (!dFlights.isEmpty()) {
				FlightReport dfr = dFlights.get(0);
				fr.setID(dfr.getID());
				fr.setDatabaseID(DatabaseID.ASSIGN, dfr.getDatabaseID(DatabaseID.ASSIGN));
				fr.setDatabaseID(DatabaseID.EVENT, dfr.getDatabaseID(DatabaseID.EVENT));
				fr.setAttribute(FlightReport.ATTR_CHARTER, dfr.hasAttribute(FlightReport.ATTR_CHARTER));
				if (!StringUtils.isEmpty(dfr.getComments()))
					comments.add(dfr.getComments());
			}
			
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
			
			// Check if this Flight Report counts for promotion
			AircraftPolicyOptions opts = a.getOptions(SystemData.get("airline.code"));
			fr.setEquipmentType(a.getName()); ofr.getInfo().setEquipmentType(a.getName());
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Collection<String> promoEQ = eqdao.getPrimaryTypes(SystemData.get("airline.db"), fr.getEquipmentType());

			// Loop through the eq types, not all may have the same minimum promotion stage length!!
			if (promoEQ.contains(p.getEquipmentType())) {
				FlightPromotionHelper helper = new FlightPromotionHelper(fr);
				for (Iterator<String> i = promoEQ.iterator(); i.hasNext(); ) {
					String pType = i.next();
					EquipmentType pEQ = eqdao.get(pType, SystemData.get("airline.db"));
					boolean isOK = helper.canPromote(pEQ);
					if (!isOK) {
						i.remove();
						comments.add("Not eligible for promotion: " + helper.getLastComment());
					}
				}
				
				fr.setCaptEQType(promoEQ);
			}
			
			// Check that the user has an online network ID
			OnlineNetwork network = fr.getNetwork();
			if ((network != null) && (!p.hasNetworkID(network))) {
				fr.addStatusUpdate(0, HistoryType.SYSTEM, "No " + network.toString() + " ID, resetting Online Network flag");
				fr.setNetwork(null);
			} else if ((network == null) && (fr.getDatabaseID(DatabaseID.EVENT) != 0)) {
				fr.addStatusUpdate(0, HistoryType.SYSTEM, "Filed offline, resetting Online Event flag");
				fr.setDatabaseID(DatabaseID.EVENT, 0);
			}
			
			// Check if it's an Online Event flight
			GetEvent evdao = new GetEvent(con);
			EventFlightHelper efr = new EventFlightHelper(fr);
			if ((fr.getDatabaseID(DatabaseID.EVENT) == 0) && (fr.hasAttribute(FlightReport.ATTR_ONLINE_MASK))) {
				List<Event> events = evdao.getPossibleEvents(fr, SystemData.get("airline.code"));
				events.removeIf(e -> !efr.matches(e));
				if (!events.isEmpty()) {
					Event e = events.get(0);
					fr.addStatusUpdate(0, HistoryType.SYSTEM, "Detected participation in " + e.getName() + " Online Event");
					fr.setDatabaseID(DatabaseID.EVENT, e.getID());
				}
			}
			
			// Check that the event hasn't expired
			if (fr.getDatabaseID(DatabaseID.EVENT) != 0) {
				Event e = evdao.get(fr.getDatabaseID(DatabaseID.EVENT));
				if ((e != null) && !efr.matches(e)) {
					fr.addStatusUpdate(0, HistoryType.SYSTEM, efr.getMessage());
					fr.setDatabaseID(DatabaseID.EVENT, 0);
				} else
					fr.setDatabaseID(DatabaseID.EVENT, 0);
			}
			
			// Check if the user is rated to fly the aircraft
			fr.setAttribute(FlightReport.ATTR_HISTORIC, a.getHistoric());
			EquipmentType eq = eqdao.get(p.getEquipmentType());
			if (!p.getRatings().contains(fr.getEquipmentType()) && !eq.getRatings().contains(fr.getEquipmentType()))
				fr.setAttribute(FlightReport.ATTR_NOTRATED, !fr.hasAttribute(FlightReport.ATTR_CHECKRIDE));
			
			// Check for excessive distance
			if (fr.getDistance() > opts.getRange())
				fr.setAttribute(FlightReport.ATTR_RANGEWARN, true);

			// Check for excessive weight
			if ((a.getMaxTakeoffWeight() != 0) && (fr.getTakeoffWeight() > a.getMaxTakeoffWeight()))
				fr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
			else if ((a.getMaxLandingWeight() != 0) && (fr.getLandingWeight() > a.getMaxLandingWeight()))
				fr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
			
			// Check ETOPS
			ETOPSResult etopsClass = ETOPSHelper.classify(ofr.getPositions()); 
			fr.setAttribute(FlightReport.ATTR_ETOPSWARN, ETOPSHelper.validate(opts, etopsClass.getResult()));
			if (fr.hasAttribute(FlightReport.ATTR_ETOPSWARN))
				fr.addStatusUpdate(0, HistoryType.SYSTEM, "ETOPS classificataion: " + etopsClass);
			
			// Check prohibited airspace
			Collection<Airspace> rstAirspaces = AirspaceHelper.classify(ofr.getPositions(), false);
			if (!rstAirspaces.isEmpty()) {
				fr.setAttribute(FlightReport.ATTR_AIRSPACEWARN, true);
				fr.addStatusUpdate(0, HistoryType.SYSTEM, "Entered restricted airspace " + StringUtils.listConcat(rstAirspaces, ", "));
			}
			
			// Calculate the load factor
			EconomyInfo eInfo = (EconomyInfo) SystemData.getObject(SystemData.ECON_DATA);
			if (eInfo != null) {
				LoadFactor lf = new LoadFactor(eInfo);
				double loadFactor = lf.generate(fr.getSubmittedOn());
				fr.setPassengers((int) Math.round(opts.getSeats() * loadFactor));
				fr.setLoadFactor(loadFactor);
			}
			
			// Check for inflight refueling
			FuelUse fuelUse = FuelUse.validate(ofr.getPositions());
			fr.setAttribute(FlightReport.ATTR_REFUELWARN, fuelUse.getRefuel());
			fr.setTotalFuel(fuelUse.getTotalFuel());
			fuelUse.getMessages().forEach(fuelMsg -> fr.addStatusUpdate(0, HistoryType.SYSTEM, fuelMsg));
			
			// Check the schedule database and check the route pair
			GetRawSchedule rsdao = new GetRawSchedule(con);
			GetScheduleSearch sdao = new GetScheduleSearch(con);
			sdao.setSources(rsdao.getSources(true));
			FlightTime avgHours = sdao.getFlightTime(fr); ScheduleEntry onTimeEntry = null;
			boolean isAssignment = (fr.getDatabaseID(DatabaseID.ASSIGN) != 0);
			boolean isEvent = (fr.getDatabaseID(DatabaseID.EVENT) != 0);
			if ((avgHours.getType() == RoutePairType.UNKNOWN) && !isAssignment && !isEvent)
				fr.setAttribute(FlightReport.ATTR_ROUTEWARN, true);
			else if (avgHours.getFlightTime() > 0) {
				int minHours = (int) ((avgHours.getFlightTime() * 0.75) - (SystemData.getDouble("users.pirep.pad_hours", 0) * 10));
				int maxHours = (int) ((avgHours.getFlightTime() * 1.15) + (SystemData.getDouble("users.pirep.pad_hours", 0) * 10));
				if ((fr.getLength() < minHours) || (fr.getLength() > maxHours))
					fr.setAttribute(FlightReport.ATTR_TIMEWARN, true);
				
				// Calculate timeliness of flight
				ScheduleSearchCriteria ssc = new ScheduleSearchCriteria("TIME_D"); ssc.setDBName(SystemData.get("airline.db"));
				ssc.setAirportD(fr.getAirportD()); ssc.setAirportA(fr.getAirportA());
				OnTimeHelper oth = new OnTimeHelper(sdao.search(ssc));
				fr.setOnTime(oth.validate(fr));
				onTimeEntry = oth.getScheduleEntry();
			}
			
			// Calculate average frame rate
			if (!CollectionUtils.isEmpty(ofr.getPositions())) {
				int totalFrames = 0;
				for (ACARSRouteEntry rte : ofr.getPositions())
					totalFrames += rte.getFrameRate();
					
				fr.setAverageFrameRate(((double) totalFrames) / ofr.getPositions().size());  
			}
				
			// Save comments
			if (!comments.isEmpty())
				fr.setComments(StringUtils.listConcat(comments, "\r\n"));
			
			// Load the departure runway
			GetNavAirway navdao = new GetNavAirway(con);
			Runway rD = null;
			LandingRunways lr = navdao.getBestRunway(fr.getAirportD(), fr.getSimulator(), fr.getTakeoffLocation(), fr.getTakeoffHeading());
			Runway r = lr.getBestRunway();
			if (r != null) {
				int dist = r.distanceFeet(fr.getTakeoffLocation());
				rD = new RunwayDistance(r, dist);
				if (r.getLength() < opts.getTakeoffRunwayLength())
					fr.setAttribute(FlightReport.ATTR_RWYWARN, true);
				if (!r.getSurface().isHard() && !opts.getUseSoftRunways())
					fr.setAttribute(FlightReport.ATTR_RWYSFCWARN, true);
			}

			// Load the arrival runway
			Runway rA = null;
			lr = navdao.getBestRunway(fr.getAirportA(), fr.getSimulator(), fr.getLandingLocation(), fr.getLandingHeading());
			r = lr.getBestRunway();
			if (r != null) {
				int dist = r.distanceFeet(fr.getLandingLocation());
				rA = new RunwayDistance(r, dist);
				if (r.getLength() < opts.getLandingRunwayLength())
					fr.setAttribute(FlightReport.ATTR_RWYWARN, true);
				if (!r.getSurface().isHard() && !opts.getUseSoftRunways())
					fr.setAttribute(FlightReport.ATTR_RWYSFCWARN, true);
			}
			
			// Calculate gates
			Gate gD = null; Gate gA = null;
			if (ofr.getPositions().size() > 2) {
				GeoComparator dgc = new GeoComparator(ofr.getPositions().first(), true);
				GeoComparator agc = new GeoComparator(ofr.getPositions().last(), true);
			
				// Get the closest departure gate
				GetGates gdao = new GetGates(con);
				SortedSet<Gate> dGates = new TreeSet<Gate>(dgc);
				dGates.addAll(gdao.getAllGates(fr.getAirportD(), fr.getSimulator()));
				gD = dGates.isEmpty() ? null : dGates.first();
				
				// Get the closest arrival gate
				SortedSet<Gate> aGates = new TreeSet<Gate>(agc);
				aGates.addAll(gdao.getAllGates(fr.getAirportA(), fr.getSimulator()));
				gA = aGates.isEmpty() ? null : aGates.first();
			}
			
			// Start transaction
			ctx.startTX();
			
			// Write the ACARS data
			SetACARSRunway awdao = new SetACARSRunway(con);
			awdao.createFlight(ofr.getInfo());
			fr.setDatabaseID(DatabaseID.ACARS, ofr.getInfo().getID());
			awdao.writeRunways(ofr.getInfo().getID(), rD, rA);
			awdao.writeGates(ofr.getInfo().getID(), gD, gA);
			awdao.writePositions(ofr.getInfo().getID(), ofr.getPositions());
			
			// Write the flight report
			SetFlightReport fwdao = new SetFlightReport(con);
			fwdao.write(fr);
			fwdao.writeACARS(fr, SystemData.get("airline.db"));
			if (fwdao.updatePaxCount(fr.getID()))
				log.warn("Update Passnger count for PIREP #" + fr.getID());
			
			// Write ontime data if there is any
			if (fr.getOnTime() != OnTime.UNKNOWN) {
				SetACARSOnTime aowdao = new SetACARSOnTime(con);
				aowdao.write(SystemData.get("airline.db"), fr, onTimeEntry);
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