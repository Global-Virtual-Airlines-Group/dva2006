// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.exam;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.json.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.testing.EquipmentRideScript;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.JSONUtils;

/**
 * A Web Service to display available Check Ride program/aircraft/simulator combinations.
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public class CheckRideSimulatorService extends WebService {
	
	private static final List<Simulator> DEFAULT_SIMS = List.of(Simulator.FS9, Simulator.FSX, Simulator.P3D, Simulator.P3Dv4, Simulator.XP10, Simulator.XP11);
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		Collection<EquipmentRideScript> crDescs = new ArrayList<EquipmentRideScript>();
		try {
			GetExamProfiles epdao = new GetExamProfiles(ctx.getConnection());
			crDescs.addAll(epdao.getScripts());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Build the JSON structure
		JSONObject jo = new JSONObject();
		for (EquipmentRideScript sc : crDescs) {
			JSONObject epo = jo.optJSONObject(sc.getProgram());
			if (epo == null) {
				epo = new JSONObject();
				jo.put(sc.getProgram(), epo);
			}
			
			final JSONObject fpo = epo; 
			Collection<Simulator> sims = sc.getSimulators().isEmpty() ? DEFAULT_SIMS : sc.getSimulators();
			sims.forEach(s -> fpo.accumulate(s.name(), sc.getEquipmentType()));
			JSONUtils.ensureArrayPresent(epo, DEFAULT_SIMS.toArray());
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
}