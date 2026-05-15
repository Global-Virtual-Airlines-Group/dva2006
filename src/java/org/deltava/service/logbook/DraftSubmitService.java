// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.logbook;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.time.Instant;
import java.io.IOException;
import java.sql.Connection;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.simbrief.BriefingPackage;

import org.deltava.dao.*;
import org.deltava.dao.http.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to allow population of Draft Flight Reports from external applications.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class DraftSubmitService extends WebService {
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Parse the inbound payload
		DraftFlightReport dfr = null; JSONObject ro = new JSONObject();
		try {
			JSONObject jo = new JSONObject(new JSONTokener(ctx.getBody()));
			
			// Get the flight ID
			final int id = jo.optInt("id");
			
			// Create the draft flight object
			dfr = new DraftFlightReport(SystemData.getAirline(jo.getString("airline")), jo.getInt("flight"), jo.optInt("leg", 1));
			dfr.setAuthorID(ctx.getUser().getID());
			dfr.setRank(ctx.getUser().getRank());
			dfr.setDate(Instant.now());
			dfr.setStatus(FlightStatus.DRAFT);
			dfr.setAirportD(SystemData.getAirport(jo.getString("airportD")));
			dfr.setAirportA(SystemData.getAirport(jo.getString("airportA")));
			dfr.setEquipmentType(jo.getString("eqType"));
			dfr.setNetwork(EnumUtils.parse(OnlineNetwork.class, jo.optString("network"), null));
			dfr.setPassengers(jo.optInt("pax"));
			dfr.setAltitude(jo.optString("alt"));
			dfr.setRemarks(jo.optString("remarks"));
			dfr.setRoute(jo.optString("route"));
			if (id > 0) dfr.setID(id);
			
			// Ensure we're populated
			if (!dfr.isPopulated())
				throw new JSONException(String.format("Invalid Airport Pair - %s / %s", jo.getString("airportD"), jo.getString("airportA")));
			
			// Get the connection and any draft flight reports
			Connection con = ctx.getConnection();
			
			// If fr is not null, check that it's actually ours
			if (id > 0) {
				GetFlightReports frdao = new GetFlightReports(con);
				FlightReport fr = frdao.get(id, ctx.getDB());
				if (fr != null) {
					PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
					ac.validate();
					if (!ac.getCanEdit())
						throw error(SC_UNAUTHORIZED, "Cannot modify Flight Report " + id, false);
					
					dfr.setDatabaseID(DatabaseID.ASSIGN, fr.getDatabaseID(DatabaseID.ASSIGN));
					dfr.setSimulator(fr.getSimulator());
				}
			}
			
			// Get the aircraft to calculate load factor
			GetAircraft acdao = new GetAircraft(con);
			Aircraft a = acdao.get(dfr.getEquipmentType());
			AircraftPolicyOptions opts = a.getOptions(SystemData.get("airline.code"));
			if (opts.getSeats() > 0)
				dfr.setLoadFactor(dfr.getPassengers() * 1.0d / opts.getSeats());
			
			// Get the write DAO and start transaction
			ctx.startTX();
			SetFlightReport frwdao = new SetFlightReport(con);
			
			// Get the SimBrief briefing
			BriefingPackage sbPkg = null;
			GetSimBrief sbdao = new GetSimBrief();
			String sbID = jo.optString("simBriefID");
			if (!StringUtils.isEmpty(sbID) && ctx.getUser().hasID(ExternalID.NAVIGRAPH)) {
				try {
					sbPkg = sbdao.load(sbID);
				} catch (HTTPDAOException hde) {
					dfr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Error fetching SimBrief package - %s", hde.getMessage()));
				}
					
				dfr.setAttribute(Attribute.SIMBRIEF, (sbPkg != null));
				if (sbPkg != null) {
					ro.put("isSimBrief", true);
					dfr.setRoute(sbPkg.getRoute());
					dfr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Linked to SimBrief plan %s", sbID));
				}
			}
			
			// Write the flight report and the SimBrief package
			frwdao.write(dfr, ctx.getDB());
			if (sbPkg != null) {
				sbPkg.setID(dfr.getID());
				frwdao.writeSimBrief(sbPkg);
			}

			ro.put("id", dfr.getID());
			ctx.commitTX();
		} catch (JSONException je) {
			throw error(SC_BAD_REQUEST, je.getMessage());
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.println(ro.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	@Override
	public boolean isSecure() {
		return true;
	}
}