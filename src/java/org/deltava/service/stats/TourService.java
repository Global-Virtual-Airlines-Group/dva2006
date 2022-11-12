// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import org.json.*;

import org.deltava.beans.stats.Tour;

import org.deltava.util.JSONUtils;

/**
 * A Web Service to 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */
abstract class TourService extends org.deltava.service.WebService {

	/**
	 * Serializes a Tour to JSON.
	 * @param t the Tour
	 * @return a JSONObject
	 */
	protected static JSONObject serialize(Tour t) {
		
		JSONObject to = new JSONObject();
		to.put("id", t.getID());
		to.put("owner", t.getOwner().getCode());
		to.put("name", t.getName());
		to.put("active", t.getActive());
		to.put("acarsOnly", t.getACARSOnly());
		to.put("allowOffline", t.getAllowOffline());
		to.put("matchEQ", t.getMatchEquipment());
		to.put("matchLeg", t.getMatchLeg());
		to.put("startDate", t.getStartDate().toEpochMilli() / 1000);
		to.put("endDate", t.getEndDate().toEpochMilli() / 1000);
		t.getNetworks().forEach(net -> to.accumulate("networks", net.name()));
		t.getFlights().forEach(se -> to.accumulate("flights", JSONUtils.format(se)));
		JSONUtils.ensureArrayPresent(to, "networks", "flights");
		return to;
	}
	
	@Override
	public final boolean isSecure() {
		return true;
	}
}