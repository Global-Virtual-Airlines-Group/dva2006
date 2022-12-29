// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import org.json.JSONObject;

import org.deltava.beans.*;

import org.deltava.service.WebService;

import org.deltava.util.JSONUtils;

/**
 * An abstract class to support Web Services formatting common JSON objects
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

abstract class JSONDataService extends WebService {

	/**
	 * Formats a Pilot as a JSON Object.
	 * @param p the Pilot
	 * @param net the OnlineNetwork
	 * @return a JSONObject 
	 */
	protected static JSONObject format(Pilot p, OnlineNetwork net) {
		JSONObject po = new JSONObject();
		po.put("id", p.getID());
		po.put("name", p.getName());
		po.put("networkID", p.getNetworkID(net));
		po.put("flights", p.getLegs());
		po.put("hours", p.getHours());
		if (p.getLastFlight() != null)
			po.put("lastFlight", JSONUtils.formatDate(p.getLastFlight()));
		
		return po;
	}
}