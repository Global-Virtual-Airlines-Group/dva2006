// Copyright 2010, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.hr;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.json.*;

import org.deltava.beans.Pilot;

import org.deltava.comparators.*;
import org.deltava.dao.*;
import org.deltava.service.*;

/**
 * A Web Service to display Pilots eligible for promotion to Senior Captain.
 * @author Luke
 * @version 7.3
 * @since 3.3
 */

public class PilotNominationService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get eligible pilots
		Collection<Pilot> pilots = new TreeSet<Pilot>(new PilotComparator(PersonComparator.FIRSTNAME));
		try {
			GetPilotRecognition pdao = new GetPilotRecognition(ctx.getConnection());
			pilots.addAll(pdao.getByID(pdao.getNominationEligible(), "PILOTS").values());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Create the JSON document
		JSONArray ja = new JSONArray();
		for (Pilot p : pilots) {
			JSONObject po = new JSONObject();
			po.put("id", p.getHexID());
			po.put("name", p.getName());
			po.put("code", p.getPilotCode());
			ja.put(po);
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(1800);
			ctx.println(ja.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
}