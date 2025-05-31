// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.net.*;
import java.util.*;
import java.time.Instant;
import java.io.IOException;

import org.json.*;
import org.apache.logging.log4j.*;

import org.deltava.beans.system.*;
import org.deltava.dao.*;

/**
 * A Web Service to act as a Reporting API endpoint.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class ReportingService extends WebService {
	
	private static final Logger log = LogManager.getLogger(ReportingService.class);

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
				bo.put("originalPolicy", p.replace("; ", ";" + System.getProperty("line.separator")));
			
			// Build the bean
			BrowserReport br = new BrowserReport(VersionInfo.BUILD, jo.getString("type"));
			br.setCreatedOn(now.minusSeconds(jo.optInt("age")));
			br.setCreatedOn(Instant.now());
			br.setURL(jo.getString("url"));
			br.setDirective(bo.optString("effectiveDirective"));
			br.setBody(jo.getJSONObject("body").toString(1));
			
			// Get the host name
			String blockedURL = bo.optString("blockedURL");
			if (blockedURL != null) {
				try {
					URI u = new URI(blockedURL);
					br.setHost(u.getHost());
				} catch (URISyntaxException ue) {
					log.warn("Invalid URL - {}", blockedURL);
				}
			}

			if (!"127.0.0.1".equals(br.getHost()))
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

		log.info("Processed {} Reports from {}", Integer.valueOf(reports.size()), ctx.getRequest().getRemoteHost());
		return SC_OK;
	}
}