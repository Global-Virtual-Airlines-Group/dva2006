// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.json.JSONArray;

import org.deltava.beans.stats.Tour;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.security.command.TourAccessControl;

/**
 * A Web Service to display one or more Flight Tours.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class TourListService extends TourService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		Collection<Tour> tours = new ArrayList<Tour>();
		try {
			GetTour dao = new GetTour(ctx.getConnection());
			tours.addAll(dao.getAll());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Check access and serialize to JSON
		JSONArray ja = new JSONArray();
		for (Iterator<Tour> i = tours.iterator(); i.hasNext(); ) {
			Tour t = i.next();
			TourAccessControl ac = new TourAccessControl(ctx, t);
			ac.validate();
			if (ac.getCanRead())
				ja.put(serialize(t));
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.println(ja.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	@Override
	public final boolean isLogged() {
		return false;
	}
}