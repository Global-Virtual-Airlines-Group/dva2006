// Copyright 2022, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.logbook;

import org.json.JSONObject;

import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.schedule.Aircraft;

import org.deltava.util.JSONUtils;

/**
 * A log book export class to generate JSON-formatted log books.
 * @author Luke
 * @version 12.4
 * @since 10.3
 */

public class JSONExport extends LogbookExport {
	
	private final JSONObject _jo = new JSONObject();

	@Override
	public final String getContentType() {
		return "application/json";
	}
	
	@Override
	public final String getExtension() {
		return "json";
	}
	
	@Override
	public final String toString() {
		JSONUtils.ensureArrayPresent(_jo, "flights");
		return _jo.toString(2); 
	}
	
	@Override
	public void add(FlightReport fr) {
		Aircraft ac = getAircraft(fr.getEquipmentType());
		_jo.accumulate("flights", JSONFlightExport.format(ac, fr));
	}
}