// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Date;

import org.deltava.beans.Helper;
import org.deltava.beans.flight.*;

/**
 * A utility class to convert XACARS Flight Information records into Flight Reports.
 * @author Luke
 * @version 4.1
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
		
		// Create the connection entry
		ConnectionEntry ce = new ConnectionEntry(System.currentTimeMillis());
		ce.setAuthorID(inf.getAuthorID());
		ce.setStartTime(inf.getStartTime());
		ce.setEndTime(inf.getEndTime());
		
		// Create the flight entry
		FlightInfo fi = new FlightInfo(ce.getID());
		fi.setFlightCode(inf.getFlightCode());
		fi.setAirportD(inf.getAirportD());
		fi.setAirportA(inf.getAirportA());
		fi.setEquipmentType(inf.getEquipmentType());
		fi.setStartTime(inf.getStartTime());
		fi.setEndTime(inf.getEndTime());
		fi.setRoute(inf.getRoute());
		fi.setXACARS(true);
		
		// Basic PIREP fields
		XACARSFlightReport xfr = new XACARSFlightReport(inf.getAirline(), inf.getFlightNumber(), inf.getLeg());
		xfr.setAirportD(inf.getAirportD());
		xfr.setAirportA(inf.getAirportA());
		xfr.setAirportL(inf.getAirportL());
		xfr.setAuthorID(inf.getAuthorID());
		xfr.setDate(new Date());
		xfr.setSubmittedOn(new Date());
		xfr.setEquipmentType(inf.getEquipmentType());
		xfr.setFSVersion(inf.getFSVersion());
		xfr.setAttribute(FlightReport.ATTR_XACARS, true);
		xfr.setLength(inf.getLength());
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
		fi.setFSVersion(xfr.getFSVersion());
		
		// Calculate the flight time
		int duration = (int) ((xfr.getEndTime().getTime() - xfr.getStartTime().getTime()) / 1000);
		xfr.setLength(duration / 36);

		// Create the offline flight
		OfflineFlight<XACARSFlightReport, XARouteEntry> ofr = new OfflineFlight<XACARSFlightReport, XARouteEntry>();
		ofr.setConnection(ce);
		ofr.setInfo(fi);
		ofr.setFlightReport(xfr);
		return ofr;
	}
}