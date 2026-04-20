// Copyright 2022, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.logbook;

import java.time.*;

import org.json.JSONObject;

import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Aircraft;

import org.deltava.util.JSONUtils;

/**
 * A log book export class to generate JSON-formatted log books.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

class JSONFlightExport {

	// static class
	private JSONFlightExport() {
		super();
	}
	
	/*
	 * Helper method to format Instants as epoch times if present.
	 */
	private static void putEpoch(JSONObject jo, String name, Instant dt) {
		if (dt != null) jo.put(name, dt.toEpochMilli());
	}
	
	/*
	 * Helper method to format Durations as seconds if present
	 */
	private static void putDuration(JSONObject jo, String name, Duration d) {
		if (d != null) jo.put(name, d.toSeconds());
	}

	/**
	 * Formats a FlightReport into a JSON object.
	 * @param ac the Aircraft used 
	 * @param fr a FlightReport
	 * @return a JSONObject
	 */
	static JSONObject format(Aircraft ac, FlightReport fr) {
		
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
		po.put("attrs", fr.getAttributes());
		
		// Load aircraft
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
		
		JSONUtils.ensureArrayPresent(po, "promotionEQ", "updates", "capabilities");
		
		// Convert to FDR FlightReport
		if (fr.getFDR() == null) return po;
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
		if (fdr instanceof ACARSFlightReport afr) jlo.put("g", afr.getLandingG());
		po.put("landing", jlo);
		
		// Format end
		JSONObject jeo = new JSONObject();
		putEpoch(jeo, "time", fdr.getEndTime());
		jeo.put("fuel", fdr.getGateFuel());
		jeo.put("weight", fdr.getGateWeight());
		po.put("end", jeo);
		
		// Add ACARS/simFDR data
		if (fr.getFDR() == Recorder.XACARS) return po;
		ACARSFlightReport afr = (ACARSFlightReport) fdr;
		po.put("clientBuild", afr.getClientBuild());
		if (afr.getBeta() > 0) po.put("beta", afr.getBeta());
		po.put("acCode", afr.getAirborneTime());
		po.put("fde", afr.getFDE());
		po.putOpt("tailCode", afr.getTailCode());
		po.put("avgFrameRate", afr.getAverageFrameRate());
		po.put("cargoWeight", afr.getCargoWeight());
		po.put("paxWeight", afr.getPaxWeight());
		po.put("restoreCount", afr.getRestoreCount());
		putDuration(po, "boardTime", afr.getBoardTime());
		putDuration(po, "deboardTime", afr.getDeboardTime());
		po.put("onTime", afr.getOnTime().toString());
		
		// Format capabilities
		for (int x = 0; x < Capabilities.values().length; x++) {
			Capabilities c = Capabilities.values()[x];
			if (c.has(afr.getCapabilities()))
				po.put("capabilities", c.name());
		}
		
		return po;
	}
	
	/**
	 * Formats a RouteEntry bean into a JSON object.
	 * @param r the Recorder used
	 * @param re the RouteEntry
	 * @return a JSONOject
	 */
	static JSONObject format(Recorder r, RouteEntry re) {

		// Set core fields
		JSONObject ro = new JSONObject();
		ro.put("lat", re.getLatitude());
		ro.put("lng", re.getLocation());
		ro.put("msl", re.getAltitude());
		ro.put("alt", re.getAltitude());
		ro.put("aSpeed", re.getAirSpeed());
		ro.put("gSpeed", re.getGroundSpeed());
		ro.put("mach", re.getMach());
		ro.put("hdg", re.getHeading());
		ro.put("fuel", re.getFuelRemaining());
		putEpoch(ro, "dt", re.getDate());
		ro.put("phase", re.getPhase().toString());
		ro.put("isWarning", re.isWarning());
		re.getWarnings().forEach(w -> ro.accumulate("warnings", w.toString()));
		for (int x = 0; x < ACARSFlags.values().length; x++) {
			ACARSFlags af = ACARSFlags.values()[x];
			if (re.isFlagSet(af))
				ro.accumulate("flags", af.toString());
		}
		
		JSONUtils.ensureArrayPresent(ro, "flags", "groundOps");
		if (r == Recorder.XACARS) return ro;
		
		// Format ACARS fields
		ACARSRouteEntry ae = (ACARSRouteEntry) re;
		putEpoch(ro, "simUTC", ae.getSimUTC());
		ro.put("agl", re.getRadarAltitude());
		ro.put("aoa", ae.getAOA());
		ro.put("pitch", ae.getPitch());
		ro.put("bank", ae.getBank());
		ro.put("g", ae.getG());
		ro.put("flaps", ae.getFlaps());
		ro.put("ff", ae.getFuelFlow());
		ro.put("pressure", ae.getPressure());
		ro.put("temp", ae.getTemperature());
		ro.put("viz", ae.getVisibility());
		ro.put("wSpeed", ae.getWindSpeed());
		ro.put("wHdg", ae.getWindHeading());
		ro.put("simRate", ae.getSimRate());
		ro.put("frameRate", ae.getFrameRate());

		// Format ground ops
		for (int x = 0; x < GroundOperations.values().length; x++) {
			GroundOperations go = GroundOperations.values()[x];
			if (go.has(ae.getGroundOperations()))
				ro.accumulate("groundOps", go.name());
		}
		
		// Format N1 / N2
		JSONObject n1o = new JSONObject(); JSONObject n2o = new JSONObject();
		n1o.put("avg", ae.getN1());
		n2o.put("avg", ae.getN2());
		for (int x = 0; x < ae.getEngineCount(); x++) {
			n1o.accumulate("eng", Double.valueOf(ae.getN1(x)));
			n2o.accumulate("eng", Double.valueOf(ae.getN2(x)));
		}
		
		return ro;
	}
	
	/**
	 * Formats a NavigationDataBean into a JSON object.
	 * @param ndb a NavigationDataBean
	 * @return a JSONObject
	 */
	 static JSONObject format(NavigationDataBean ndb) {
		
		// Create the object
		JSONObject jo = new JSONObject();
		jo.put("type", ndb.getType().toString());
		jo.put("id", ndb.getCode());
		jo.put("name", ndb.getName());
		jo.put("lat", ndb.getLatitude());
		jo.put("lng", ndb.getLongitude());
		jo.putOpt("airway", ndb.getAirway());
		jo.putOpt("region", ndb.getRegion());
		switch (ndb.getType()) {
			case GATE:
				Gate g = (Gate) ndb;
				jo.put("hdg", g.getHeading());
				g.getAirlines().forEach(al -> jo.accumulate("airlines", al.getCode()));
				JSONUtils.ensureArrayPresent(jo, "airlines");
				break;
				
			case VOR:
			case NDB:
				NavigationFrequencyBean nfb = (NavigationFrequencyBean) ndb;
				jo.put("freq", nfb.getFrequency());
				break;
				
			case RUNWAY:
				Runway r = (Runway) ndb;
				jo.put("hdg", r.getHeading());
				jo.put("length", r.getLength());
				jo.put("threshold", r.getThresholdLength());
				jo.put("sfc", r.getSurface().toString());
				break;
				
			default:
				// empty
		}
		
		return jo;
	}
}