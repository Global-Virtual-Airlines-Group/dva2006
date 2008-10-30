// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.io.*;
import java.util.*;
import java.text.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.navdata.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to export the navigation data database to ACARS dispatch clients.
 * @author Luke
 * @version 2.2
 * @since 2.0
 */

public class NavigationAidService extends DispatchDataService {
	
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
		
		// Get the navaid type and check the cache
		int type = StringUtils.parse(ctx.getParameter("type"), NavigationDataBean.VOR);
		File f = _dataCache.get(Integer.valueOf(type));
		if (f != null) {
			ctx.getResponse().setContentType("text/plain");
			ctx.getResponse().setHeader("Cache-Control", "private");
			ctx.getResponse().setIntHeader("max-age", 600);
			sendFile(f, ctx.getResponse());
			return SC_OK;
		}

		// Get the DAO and the airways/navaids
		Collection<NavigationDataBean> navaids = null;
		try {
			GetNavRoute navdao = new GetNavRoute(ctx.getConnection());
			navaids = navdao.getAll(type);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Write to a temp file
		File cacheDir = new File(SystemData.get("schedule.cache"));
		f = new File(cacheDir, "navaid" + NavigationDataBean.NAVTYPE_NAMES[type] + ".txt");

		// Format navaids
		try {
			PrintWriter pw = new PrintWriter(new BufferedOutputStream(new FileOutputStream(f), 65536));
			pw.println("[navaids]");
			final NumberFormat df = new DecimalFormat("#0.000000");
			for (Iterator<NavigationDataBean> i = navaids.iterator(); i.hasNext();) {
				NavigationDataBean nb = i.next();
				pw.print(nb.getCode() + "." + nb.getTypeName() + "=");
				pw.print(df.format(nb.getLatitude()) + "," + df.format(nb.getLongitude()));
				if ((nb.getType() == NavigationDataBean.VOR) || (nb.getType() == NavigationDataBean.NDB))  {
					pw.print("," + nb.getName() + ",");
					pw.print(((NavigationFrequencyBean) nb).getFrequency());
				}
			
				ctx.print(",");
				if (nb.getRegion() != null)
					pw.println(nb.getRegion());
				else
					pw.println("");
				
				i.remove();
			}
			
			// Close the file and add to the cache
			pw.close();
			addCacheEntry(Integer.valueOf(type), f);

			// Format and write
			ctx.getResponse().setContentType("text/plain");
			ctx.getResponse().setHeader("Cache-Control", "private");
			ctx.getResponse().setIntHeader("max-age", 600);
			sendFile(f, ctx.getResponse());
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