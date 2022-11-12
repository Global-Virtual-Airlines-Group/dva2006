// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.stats.Tour;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.security.command.TourAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Service to display a JSON-serialized Flight Tour.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class TourInfoService extends TourService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		int id = StringUtils.parse(ctx.getParameter("id"), 0); Tour t = null;
		try {
			GetTour dao = new GetTour(ctx.getConnection());
			t = dao.get(id, ctx.getDB());
			if (t == null)
				return SC_NOT_FOUND;
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Check our access
		TourAccessControl ac = new TourAccessControl(ctx, t);
		ac.validate();
		if (!ac.getCanRead())
			return SC_FORBIDDEN;
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.println(serialize(t).toString());
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