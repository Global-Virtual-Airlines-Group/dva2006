// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.wx;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.json.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.mc.GetTiles;

import org.deltava.service.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to enumerate the available Weather imagery.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class WUImageService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the available imagery
		Map<String, Collection<Date>> results = new TreeMap<String, Collection<Date>>();
		try {
			GetTiles trdao = new GetTiles();
			Collection<String> types = trdao.getTypes();
			for (String imgType : types)
				results.put(imgType, trdao.getDates(imgType));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		}
		
		// Convert results to JSON
		JSONObject jo = new JSONObject();
		try {
			jo.put("names", results.keySet());
			jo.put("timestamp", Long.valueOf(System.currentTimeMillis()));
			for (Map.Entry<String, Collection<Date>> me : results.entrySet()) {
				Collection<Long> dates = new ArrayList<Long>();
				for (Date dt : me.getValue())
					dates.add(Long.valueOf(dt.getTime()));
			
				jo.put(me.getKey(), dates);
			}
		} catch (JSONException je) {
			throw error(SC_INTERNAL_SERVER_ERROR, je.getMessage(), je);
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("text/javascript", "UTF-8");
			ctx.setExpiry(30);
			if (!StringUtils.isEmpty(ctx.getParameter("function"))) {
				ctx.print(ctx.getParameter("function"));
				ctx.print("(");
				ctx.print(jo.toString(2));
				ctx.println(")");
			} else
				ctx.println(jo.toString());
			
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
}
