// Copyright 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.time.Instant;

import org.deltava.beans.Helper;
import org.deltava.beans.flight.*;

/**
 * A utility class to convert XACARS Flight Information records into Flight Reports.
 * @author Luke
 * @version 7.0
 * @since 4.1
 */

@Helper(OfflineFlight.class)
public final class XACARSFlightHelper {

	// static class
	private XACARSFlightHelper() {
		super();
	}

	/**
	 * Converts a flight information bean into a Flight Report.
	 * @param inf an XAFlightInfo bean
	 * @return an XACARSFlightReport bean
	 */
	public static OfflineFlight<XACARSFlightReport, XARouteEntry> build(XAFlightInfo inf) {
		
		// Create the flight entry
		FlightInfo fi = new FlightInfo(0);
		fi.setFlightCode(inf.getFlightCode());
		fi.setAirportD(inf.getAirportD());
		fi.setAirportA(inf.getAirportA());
		fi.setEquipmentType(inf.getEquipmentType());
		fi.setStartTime(inf.getStartTime());
		fi.setEndTime(inf.getEndTime());
		fi.setRoute(inf.getRoute());
		fi.setAuthorID(inf.getAuthorID());
		fi.setFDR(Recorder.XACARS);
		
		// Basic PIREP fields
		XACARSFlightReport xfr = new XACARSFlightReport(inf.getAirline(), inf.getFlightNumber(), inf.getLeg());
		xfr.setAirportD(inf.getAirportD());
		xfr.setAirportA(inf.getAirportA());
		xfr.setAirportL(inf.getAirportL());
		xfr.setAuthorID(inf.getAuthorID());
		xfr.setDate(Instant.now());
		xfr.setSubmittedOn(Instant.now());
		xfr.setEquipmentType(inf.getEquipmentType());
		xfr.setSimulator(inf.getSimulator());
		xfr.setAttribute(FlightReport.ATTR_XACARS, true);
		xfr.setPassengers(inf.getPassengers());
		xfr.setLoadFactor(inf.getLoadFactor());
		xfr.setRoute(inf.getRoute());
		xfr.setNetwork(inf.getNetwork());
		xfr.setStatus(FlightReport.SUBMITTED);
		
		// ACARS-specific data
		xfr.setStartTime(inf.getStartTime());
		xfr.setTaxiTime(inf.getTaxiTime());
		xfr.setTaxiFuel(inf.getTaxiFuel());
		xfr.setTaxiWeight(inf.getTaxiWeight());
		
		xfr.setTakeoffTime(inf.getTakeoffTime());
		xfr.setTakeoffDistance(inf.getTakeoffDistance());
		xfr.setTakeoffHeading(inf.getTakeoffHeading());
		xfr.setTakeoffLocation(inf.getTakeoffLocation());
		xfr.setTakeoffN1(inf.getTakeoffN1());
		xfr.setTakeoffSpeed(inf.getTakeoffSpeed());
		xfr.setTakeoffFuel(inf.getTakeoffFuel());
		xfr.setTakeoffWeight(inf.getTakeoffWeight());
		
		xfr.setLandingTime(inf.getLandingTime());
		xfr.setLandingDistance(inf.getLandingDistance());
		xfr.setLandingHeading(inf.getLandingHeading());
		xfr.setLandingLocation(inf.getLandingLocation());
		xfr.setLandingN1(inf.getLandingN1());
		xfr.setLandingSpeed(inf.getLandingSpeed());
		xfr.setLandingFuel(inf.getLandingFuel());
		xfr.setLandingWeight(inf.getLandingWeight());
		
		xfr.setGateFuel(inf.getGateFuel());
		xfr.setGateWeight(inf.getGateWeight());
		xfr.setEndTime(inf.getEndTime());
		fi.setSimulator(xfr.getSimulator());
		
		// Calculate the flight time
		int duration = (int) ((xfr.getEndTime().toEpochMilli() - xfr.getStartTime().toEpochMilli()) / 1000);
		xfr.setLength(duration / 36);

		// Create the offline flight
		OfflineFlight<XACARSFlightReport, XARouteEntry> ofr = new OfflineFlight<XACARSFlightReport, XARouteEntry>();
		ofr.setInfo(fi);
		ofr.setFlightReport(xfr);
		return ofr;
	}
}