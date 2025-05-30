// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.time.Instant;
import java.io.IOException;

import org.json.*;

import org.deltava.beans.system.BrowserReport;

import org.deltava.dao.*;

/**
 * A Web Service to act as a Reporting API endpoint.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class ReportingService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Parse the data
		JSONArray ja = null;
		try {
			ja = new JSONArray(new JSONTokener(ctx.getRequest().getInputStream()));
		} catch (IOException ie) {
			throw error(SC_BAD_REQUEST, ie.getMessage(), false);
		} catch (Exception e) {
			throw error(SC_BAD_REQUEST, e.getMessage());
		}
		
		// Generate the reports
		Instant now = Instant.now();
		Collection<BrowserReport> reports = new ArrayList<BrowserReport>();
		for (int x = 0; x < ja.length(); x++) {
			JSONObject jo = ja.getJSONObject(x);
			JSONObject bo = jo.getJSONObject("body");
			String p = bo.optString("originalPolicy");
			if ((p != null) && (p.length() > 60))
				bo.put("originalPolicy", p.replace("; ", "; " + System.getProperty("line.separator")));

			// Build the bean
			BrowserReport br = new BrowserReport(jo.getString("type"));
			br.setCreatedOn(now.minusSeconds(jo.optInt("age")));
			br.setCreatedOn(Instant.now());
			br.setURL(jo.getString("url"));
			br.setBody(jo.getJSONObject("body").toString(1));
			reports.add(br);
		}

		// Write the reports
		try {
			SetSystemData wdao = new SetSystemData(ctx.getConnection());
			for (BrowserReport br : reports)
				wdao.write(br);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());			
		} finally {
			ctx.release();
		}

		return SC_OK;
	}
}