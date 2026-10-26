// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.*;
import java.time.Instant;

import org.json.*;

import org.deltava.beans.finance.AWSCostEntry;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load AWS costs from an internal Lambda function. 
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class GetAWSCosts extends DAO {

	/**
	 * Loads AWS costs from an internal Lambda function.
	 * @param daysBack the number of days back to retrieve
	 * @return a Collection of AWSCost beans
	 * @throws DAOException if an error occurs
	 */
	public Collection<AWSCostEntry> getCosts(int daysBack) throws DAOException {
		
		String url = String.format("%s?days=%d", SystemData.get("finance.aws.url"), Integer.valueOf(daysBack));
		setConnectTimeout(1000);
		setReadTimeout(5000);
		setCompression(Compression.GZIP);
		
		try {
			init(url);
			int rspCode = getResponseCode();
			if (rspCode >= 400)
				throw new HTTPDAOException("AWS Lambda Error", rspCode);
			
			Collection<AWSCostEntry> results = new ArrayList<AWSCostEntry>();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(getIn(), "utf-8"))) {
				JSONArray ja = new JSONArray(new JSONTokener(br));
				for (int x = 0; x < ja.length(); x++) {
					JSONObject dco = ja.getJSONObject(x);
					Instant dt = StringUtils.parseInstant(dco.getString("date"), "yyyy-MM-dd");
					JSONObject so = dco.getJSONObject("services");
					for (String svc : so.keySet()) {
						AWSCostEntry ce = new AWSCostEntry(dt, so.getInt(svc));
						ce.setService(svc);
						results.add(ce);
					}
				}
			}

			return results;
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}