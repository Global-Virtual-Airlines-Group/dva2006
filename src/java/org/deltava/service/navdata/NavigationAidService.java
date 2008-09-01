// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.util.*;
import java.text.*;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.navdata.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to export the navigation data database to ACARS dispatch clients.
 * @author Luke
 * @version 2.2
 * @since 2.0
 */

public class NavigationAidService extends WebService {

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

		Collection<NavigationDataBean> navaids = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the airways/navaids
			GetNavRoute navdao = new GetNavRoute(con);
			navaids = navdao.getAll(StringUtils.parse(ctx.getParameter("type"), NavigationDataBean.VOR));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Format navaids
		ctx.println("[navaids]");
		final NumberFormat df = new DecimalFormat("#0.000000");
		for (Iterator<NavigationDataBean> i = navaids.iterator(); i.hasNext();) {
			NavigationDataBean nb = i.next();
			ctx.print(nb.getCode() + "." + nb.getTypeName() + "=");
			ctx.print(df.format(nb.getLatitude()) + "," + df.format(nb.getLongitude()));
			switch (nb.getType()) {
			case NavigationDataBean.VOR:
				ctx.print("," + nb.getName() + ",");
				ctx.print(((VOR) nb).getFrequency());
				break;

			case NavigationDataBean.NDB:
				ctx.print("," + nb.getName() + ",");
				ctx.print(((NDB) nb).getFrequency());
				break;
			}
			
			ctx.print(",");
			if (nb.getRegion() != null)
				ctx.println(nb.getRegion());
			else
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