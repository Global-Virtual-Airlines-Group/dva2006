// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.logbook;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.util.Collection;
import java.io.IOException;
import java.sql.Connection;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.simbrief.BriefingPackage;

import org.deltava.dao.*;
import org.deltava.dao.http.GetSimBrief;
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
			
			// Check for simbrief ID
			String sbID = jo.optString("simBriefID");

			// Ensure we're populated
			if (!dfr.isPopulated())
				throw new JSONException(String.format("Invalid Airport Pair - %s / %s", jo.getString("airportD"), jo.getString("airportA")));
			
			// Get the connection and any draft flight reports
			Connection con = ctx.getConnection();
			GetFlightReports frdao = new GetFlightReports(con);
			Collection<FlightReport> flights = frdao.getDraftReports(ctx.getUser().getID(), dfr, ctx.getDB());
			flights.removeIf(fr -> (fr.getID() == id));
			
			// TODO: If the IDs match, update that one
			
			
			// Get the write DAO and start transaction
			ctx.startTX();
			SetFlightReport frwdao = new SetFlightReport(con);
			
			// Get the SimBrief briefing
			BriefingPackage sbPkg = null;
			GetSimBrief sbdao = new GetSimBrief();
			if (!StringUtils.isEmpty(sbID) && ctx.getUser().hasID(ExternalID.NAVIGRAPH)) {
				sbPkg = sbdao.load(sbID);
				ro.put("isSimBrief", true);
			}
			
			// Write the flight report
			frwdao.write(dfr, ctx.getDB());
			if (sbPkg != null)
				frwdao.writeSimBrief(sbPkg);

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