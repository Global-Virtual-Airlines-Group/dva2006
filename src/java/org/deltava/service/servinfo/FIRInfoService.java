// Copyright 2010, 2011, 2012, 2016, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.json.*;

import org.deltava.beans.navdata.FIR;
import org.deltava.beans.servinfo.EuroControl;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display FIR boundaries on a map.
 * @author Luke
 * @version 8.2
 * @since 3.2
 */

public class FIRInfoService extends WebService {

	/**
	 * Executes the Web Service, returning ServInfo route data.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the code
		String code = ctx.getParameter("id");
		if (StringUtils.isEmpty(code))
			return SC_NOT_FOUND;
		
		Collection<FIR> results = new TreeSet<FIR>();
		try {
			GetFIR dao = new GetFIR(ctx.getConnection());
			
			// Loop through the codes
			Collection<String> codes = StringUtils.split(code.toUpperCase(), ",");
			for (String id : codes) {
				EuroControl ec = null;
				if (id.startsWith("EUR")) {
					try {
						ec = EuroControl.valueOf(getID(id));
						for (String eid : ec.getFIRs()) {
							FIR fir = dao.get(eid, false);
							if (fir != null)
								results.add(fir);			
						}
					} catch (Exception e) {
						// empty
					}
				}

				if (ec == null) {
					FIR fir = dao.get(getID(id), id.endsWith("_FSS"));				
					if (fir != null)
						results.add(fir);
				}
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Save info and coordinates
		JSONObject jo = new JSONObject();
		for (FIR fir : results) {
			JSONObject fo = new JSONObject();
			fo.put("id", fir.getID());
			fo.put("name", fir.getName());
			fo.put("oceanic", fir.isOceanic());
			fir.getBorder().forEach(loc -> fo.append("border", JSONUtils.format(loc)));
			jo.append("firs", fo);
		}
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "firs");
		try {
			boolean isJSONP = !StringUtils.isEmpty(ctx.getParameter("jsonp"));
			ctx.setContentType(isJSONP ? "application/javascript": "application/json", "UTF-8");
			ctx.setExpiry(7200);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	@Override
	public final boolean isLogged() {
		return false;
	}
	
	private static String getID(String code) {
		int pos = code.indexOf('_');
		return (pos != -1) ? code.substring(0, pos) : code;
	}
}