// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import java.time.Instant;

import org.json.JSONObject;

import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.Aircraft;
import org.deltava.util.JSONUtils;

/**
 * A log book export class to generate JSON-formatted log books.
 * @author Luke
 * @version 10.3
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
	
	private static void putEpoch(JSONObject jo, String name, Instant dt) {
		if (dt != null) jo.put(name, dt.toEpochMilli());
	}

	@Override
	public void add(FlightReport fr) {
		
		// Write core fields
		JSONObject po = new JSONObject();
		po.put("id", fr.getID());
		po.put("status", fr.getStatus().name());
		po.put("airline", fr.getAirline().getCode());
		po.put("flight", fr.getFlightNumber());
		po.put("leg", fr.getLeg());
		po.put("eqType", fr.getEquipmentType());
		po.put("sim", fr.getSimulator().name());
		po.put("airportD", JSONUtils.format(fr.getAirportD()));
		po.put("airportA", JSONUtils.format(fr.getAirportA()));
		po.put("distance", fr.getDistance());
		po.put("pax", fr.getPassengers());
		po.put("date", JSONUtils.formatDate(fr.getDate()));
		po.put("duration", fr.getDuration().toMillis());
		putEpoch(po, "submittedOn", fr.getSubmittedOn());
		putEpoch(po, "disposedOn", fr.getDisposedOn());
		po.put("network", fr.getNetwork());
		fr.getCaptEQType().forEach(eq -> po.accumulate("promotionEQ", eq));
		po.put("comments", fr.getComments());
		po.put("remarks", fr.getRemarks());
		po.put("route", fr.getRoute());
		
		// Load aircraft
		Aircraft ac = getAircraft(fr.getEquipmentType());
		JSONObject jao = new JSONObject();
		jao.put("name", fr.getEquipmentType());
		jao.put("icao", ac.getICAO());
		po.put("aircraft", jao);
		
		// Convert status updates
		for (FlightHistoryEntry upd : fr.getStatusUpdates()) {
			JSONObject uo = new JSONObject();
			uo.put("date", upd.getDate().toEpochMilli());
			uo.put("type", upd.getType().name());
			uo.put("msg", upd.getDescription());
			po.accumulate("updates", uo);
		}
		
		JSONUtils.ensureArrayPresent(po, "promotionEQ", "updates");
		_jo.accumulate("flights", po);
		
		// Convert to FDR FlightReport
		if (fr.getFDR() == null) return;
		FDRFlightReport fdr = (FDRFlightReport) fr;

		po.put("fdr", fr.getFDR().name());
		putEpoch(po, "startTime", fdr.getStartTime());
		putEpoch(po, "taxiTime", fdr.getTaxiTime());
		po.put("airborneTime", fdr.getAirborneTime().toMillis());
		po.put("blockTime", fdr.getBlockTime().toMillis());
		po.put("totalFuel", fdr.getTotalFuel());
		
		// Format takeoff
		JSONObject jto = new JSONObject();
		putEpoch(jto, "time", fdr.getTakeoffTime());
		jto.put("location", JSONUtils.format(fdr.getTakeoffLocation()));
		jto.put("distance", fdr.getTakeoffDistance());
		jto.put("hdg", fdr.getTakeoffHeading());
		jto.put("speed", fdr.getTakeoffSpeed());
		jto.put("weight", fdr.getTakeoffWeight());
		jto.put("fuel", fdr.getTakeoffFuel());
		po.put("takeoff", jto);

		// Format landing
		JSONObject jlo = new JSONObject();
		putEpoch(jlo, "time", fdr.getLandingTime());
		jlo.put("location", JSONUtils.format(fdr.getLandingLocation()));
		jlo.put("distance", fdr.getLandingDistance());
		jlo.put("hdg", fdr.getLandingHeading());
		jlo.put("speed", fdr.getLandingSpeed());
		jlo.put("vSpeed", fdr.getLandingVSpeed());
		jlo.put("weight", fdr.getLandingWeight());
		jlo.put("fuel", fdr.getLandingFuel());
		po.put("landing", jlo);
		
		// Format end
		JSONObject jeo = new JSONObject();
		putEpoch(jeo, "time", fdr.getEndTime());
		jeo.put("fuel", fdr.getGateFuel());
		jeo.put("weight", fdr.getGateWeight());
		po.put("end", jeo);
	}
}