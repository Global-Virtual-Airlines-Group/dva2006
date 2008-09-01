// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.util.*;
import java.text.*;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.navdata.*;

import org.deltava.dao.*;
import org.deltava.service.*;

/**
 * A Web Service to export Airway data to ACARS dispatch clients.
 * @author Luke
 * @version 2.2
 * @since 2.0
 */

public class AirwayService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Ensure we are a dispatcher
		if (!ctx.isUserInRole("Dispatch"))
			throw error(SC_UNAUTHORIZED, "Not in Dispatch role");
		
		Collection<Airway> airways = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the airways/navaids
			GetNavRoute navdao = new GetNavRoute(con);
			airways = navdao.getAirways();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Format airways
		final NumberFormat df = new DecimalFormat("#0.000000"); 
		for (Iterator<Airway> i = airways.iterator(); i.hasNext(); ) {
			Airway a = i.next();
			ctx.print("[" + a.getCode() + "-");
			if (a.isHighLevel())
				ctx.print("H");
			if (a.isLowLevel())
				ctx.print("L");
			
			ctx.println("]");
			for (Iterator<NavigationDataBean> ii = a.getWaypoints().iterator(); ii.hasNext(); ) {
				NavigationDataBean ai = ii.next();
				ctx.print(ai.getCode());
				ctx.print(",");
				ctx.print(df.format(ai.getLatitude()));
				ctx.print(",");
				ctx.print(df.format(ai.getLongitude()));
				ctx.print(",");
				ctx.print(String.valueOf(ai.getType()));
				ctx.print(",");
				ctx.println((ai.getRegion() == null) ? "" : ai.getRegion());
			}
			
			ctx.println("");
			i.remove();
		}

		// Format and write
		try {
			ctx.getResponse().setContentType("text/plain");
			ctx.getResponse().setHeader("Cache-Control", "private");
			ctx.getResponse().setIntHeader("max-age", 600);
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error");
		}
		
		// Write success code
		return SC_OK;
	}
	
	/**
	 * Returns wether this web service requires authentication.
	 * @return TRUE always
	 */
	public boolean isSecure() {
		return true;
	}
}