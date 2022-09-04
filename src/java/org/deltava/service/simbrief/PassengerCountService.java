// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simbrief;

import static javax.servlet.http.HttpServletResponse.*;

import java.sql.Connection;
import java.time.Instant;

import org.json.JSONObject;

import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to assign passenger counts to flights prior to SimBrief dispatch.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class PassengerCountService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		JSONObject po = new JSONObject();
		try {
			Connection con = ctx.getConnection();
			
			// Load the flight report
			GetFlightReports frdao = new GetFlightReports(con);
			DraftFlightReport fr = frdao.getDraft(StringUtils.parse(ctx.getParameter("id"), 0), ctx.getDB());
			if (fr == null)
				throw error(SC_NOT_FOUND, "Invalid Flight Report - " + ctx.getParameter("id"), false);
			
			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			if (!ac.getCanUseSimBrief())
				throw error(SC_FORBIDDEN, "Cannot request SimBrief pax load for Flight Report " + fr.getID(), false);
			
			// Get the calculator
			EconomyInfo eInfo = (EconomyInfo) SystemData.getObject(SystemData.ECON_DATA);
			if (eInfo == null)
				throw error(SC_INTERNAL_SERVER_ERROR, "No Economy data for Airline");
			
			// Assign passengers if we don't have anything
			po.put("isCalculated", (fr.getPassengers() == 0));
			if (fr.getPassengers() == 0) {
				GetAircraft acdao = new GetAircraft(con);
				Aircraft a = acdao.get(fr.getEquipmentType());
				AircraftPolicyOptions opts = a.getOptions(SystemData.get("airline.code"));
				
				// Calculate the load factor
				LoadFactor lf = new LoadFactor(eInfo);
				double loadFactor = lf.generate(Instant.now());
				fr.setPassengers((int) Math.round(opts.getSeats() * loadFactor));
				fr.setLoadFactor(loadFactor);
				fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, "Requested pre-flight Load Factor for SimBrief");
				
				// Write the flight report
				SetFlightReport frwdao = new SetFlightReport(con);
				frwdao.write(fr);
			}
			
			// Update response
			po.put("id", fr.getID());
			po.put("pax", fr.getPassengers());	
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Format the response
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.println(po.toString());
			ctx.commit();			
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);			
		}
		
		return SC_OK;
	}
	
	@Override
	public final boolean isSecure() {
		return true;
	}
}