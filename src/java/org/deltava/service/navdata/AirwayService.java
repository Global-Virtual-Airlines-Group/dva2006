// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.io.*;
import java.util.*;
import java.text.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.navdata.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Service to export Airway data to ACARS dispatch clients.
 * @author Luke
 * @version 2.3
 * @since 2.0
 */

public class AirwayService extends DispatchDataService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Ensure we are a dispatcher
		if (!ctx.isUserInRole("Dispatch"))
			throw error(SC_UNAUTHORIZED, "Not in Dispatch role", false);

		// Check the cache
		File f = _dataCache.get("AIRWAY");
		if (f != null) {
			ctx.getResponse().setContentType("text/plain");
			ctx.getResponse().setHeader("Cache-Control", "private");
			ctx.getResponse().setIntHeader("max-age", 600);
			sendFile(f, ctx.getResponse());
			return SC_OK;
		}

		// Get the DAO and the airways/navaids
		Collection<Airway> airways = null;
		try {
			GetNavRoute navdao = new GetNavRoute(ctx.getConnection());
			airways = navdao.getAirways();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Write to a temp file
		File cacheDir = new File(SystemData.get("schedule.cache"));
		f = new File(cacheDir, "airway.txt");

		// Format airways
		try {
			PrintWriter pw = new PrintWriter(new BufferedOutputStream(new FileOutputStream(f), 65536));
			final NumberFormat df = new DecimalFormat("#0.000000");
			for (Iterator<Airway> i = airways.iterator(); i.hasNext();) {
				Airway a = i.next();
				pw.print("[" + a.getCode() + "-");
				if (a.isHighLevel())
					pw.print("H");
				if (a.isLowLevel())
					pw.print("L");

				pw.println("]");
				for (Iterator<NavigationDataBean> ii = a.getWaypoints().iterator(); ii.hasNext();) {
					NavigationDataBean ai = ii.next();
					pw.print(ai.getCode());
					pw.print(",");
					pw.print(df.format(ai.getLatitude()));
					pw.print(",");
					pw.print(df.format(ai.getLongitude()));
					pw.print(",");
					pw.print(String.valueOf(ai.getType()));
					pw.print(",");
					pw.println((ai.getRegion() == null) ? "" : ai.getRegion());
				}

				pw.println("");
				i.remove();
			}
			
			// Close the file and add to the cache
			pw.close();
			addCacheEntry("AIRWAY", f);

			// Format and write
			ctx.getResponse().setContentType("text/plain");
			ctx.getResponse().setHeader("Cache-Control", "private");
			ctx.getResponse().setIntHeader("max-age", 600);
			sendFile(f, ctx.getResponse());
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
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