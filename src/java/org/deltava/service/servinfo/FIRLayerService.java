// Copyright 2014, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.deltava.beans.navdata.FIR;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

import org.json.*;

/**
 * A Web Service to display all FIR data as a Map layer.
 * @author Luke
 * @version 7.3
 * @since 6.0
 */

public class FIRLayerService extends WebService {

	/**
	 * Executes the Web Service, returning FIR polygon data.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		Collection<FIR> results = new TreeSet<FIR>();
		try {
			GetFIR dao = new GetFIR(ctx.getConnection());
			results.addAll(dao.getAll());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Format the data
		JSONArray ja = new JSONArray();
		for (FIR f : results) {
			JSONObject fo = new JSONObject();
			fo.put("id", f.getID());
			fo.put("name", f.toString());
			fo.put("oceanic", f.isOceanic());
			fo.put("aux", f.isAux());
			f.getBorder().forEach(loc -> fo.append("border", JSONUtils.format(loc)));
			ja.put(fo);
		}
		 
		// Dump to the output stream
		String cb = ctx.getParameter("jsonp");
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(86400 * 7);
			if (!StringUtils.isEmpty(cb)) {
				ctx.print(cb);
				ctx.print("(");
			}
				
			ctx.println(ja.toString());
			if (!StringUtils.isEmpty(cb))
				ctx.print(")");
			
			ctx.commit();
		} catch (Exception e) {
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
}