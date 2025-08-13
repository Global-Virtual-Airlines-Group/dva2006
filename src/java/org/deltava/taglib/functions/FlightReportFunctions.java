// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2016, 2017, 2018, 2019, 2022, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import org.deltava.beans.Simulator;
import org.deltava.beans.acars.Capabilities;
import org.deltava.beans.flight.*;

import org.deltava.util.StringUtils;

/**
 * A JSP Function Library to define Flight Report-related functions.
 * @author Luke
 * @version 12.2
 * @since 1.0
 */

public class FlightReportFunctions {

	// static class
	private FlightReportFunctions() {
		super();
	}

	/**
	 * Returns the ACARS flight ID for this Flight Report.
	 * @param fr the Flight Report
	 * @return the ACARS database ID
	 */
	public static int ACARSID(FlightReport fr) {
		return (fr == null) ? 0 : fr.getDatabaseID(DatabaseID.ACARS);
	}

	/**
	 * Returns the Event ID for this Flight Report.
	 * @param fr the Flight Report
	 * @return the Event database ID
	 */
	public static int eventID(FlightReport fr) {
		return (fr == null) ? 0 : fr.getDatabaseID(DatabaseID.EVENT);
	}

	/**
	 * Returns the Flight Assignment ID for this Flight Report.
	 * @param fr the Flight Report
	 * @return the Assignment database ID
	 */
	public static int assignID(FlightReport fr) {
		return (fr == null) ? 0 : fr.getDatabaseID(DatabaseID.ASSIGN);
	}

	/**
	 * Returns the Disposal ID for this Flight Report.
	 * @param fr the FlightReport
	 * @return the Disposal database ID
	 */
	public static int disposalID(FlightReport fr) {
		return (fr == null) ? 0 : fr.getDatabaseID(DatabaseID.DISPOSAL);
	}

	/**
	 * Returns the Pilot ID for this Flight Report.
	 * @param fr the Flight Report
	 * @return the Pilot database ID
	 */
	public static int pilotID(FlightReport fr) {
		return (fr == null) ? 0 : fr.getDatabaseID(DatabaseID.PILOT);
	}
	
	/**
	 * Returns the Tour ID for this Flight Report.
	 * @param fr the Flight Report
	 * @return the Tour database ID
	 */
	public static int tourID(FlightReport fr) {
		return (fr == null) ? 0 : fr.getDatabaseID(DatabaseID.TOUR);
	}

	/**
	 * Returns if this Flight was logged using ACARS.
	 * @param fr the Flight Report
	 * @return TRUE if this flight used ACARS, otherwise FALSE
	 */
	public static boolean isACARS(FlightReport fr) {
		return (fr instanceof ACARSFlightReport) || fr.hasAttribute(Attribute.ACARS);
	}

	/**
	 * Returns if this Flight was logged using ACARS.
	 * @param fr the Flight Report
	 * @return TRUE if this flight used XACARS, otherwise FALSE
	 */
	public static boolean isXACARS(FlightReport fr) {
		return (fr instanceof XACARSFlightReport) || fr.hasAttribute(Attribute.XACARS);
	}
	
	/**
	 * Returns if this Flight was logged using Microsoft Flight Simulator 2020 or above.
	 * @param fr the Flight Report
	 * @return TRUE if this flight used MSFS2020 or 2024, otherwise FALSE
	 */
	public static boolean isMSFS(FlightReport fr) {
		return (fr != null) && ((fr.getSimulator() == Simulator.FS2020) || (fr.getSimulator() == Simulator.FS2024));
	}

	/**
	 * Returns if the Flight was flown online.
	 * @param fr the Flight Report
	 * @return TRUE if the ATTR_ONLINE MASK attribute is present, otherwise FALSE
	 * @see FlightReportFunctions#network(FlightReport)
	 * @see Attribute#isOnline(int)
	 */
	public static boolean isOnline(FlightReport fr) {
		return (fr != null) && Attribute.isOnline(fr.getAttributes()); 
	}

	/**
	 * Returns if this Flight is a Check Ride.
	 * @param fr the Flight Report
	 * @return TRUE if the ATTR_CHECKRIDE attribute is present, otherwise FALSE
	 * @see Attribute#CHECKRIDE
	 */
	public static boolean isCheckRide(FlightReport fr) {
		return (fr != null) && fr.hasAttribute(Attribute.CHECKRIDE);
	}

	/**
	 * Returns if this Flight was planned by a Dispatcher.
	 * @param fr the Flight report
	 * @return TRUE if the ATTR_DISPATCH attribute is present, otherwise FALSE
	 * @see Attribute#DISPATCH
	 */
	public static boolean isDispatch(FlightReport fr) {
		return (fr != null) && fr.hasAttribute(Attribute.DISPATCH);
	}

	/**
	 * Returns if this Flight Report is a Draft.
	 * @param fr the Flight Report
	 * @return TRUE if the Flight is in Draft status, otherwise FALSE
	 * @see FlightStatus#DRAFT
	 */
	public static boolean isDraft(FlightReport fr) {
		return (fr != null) && (fr.getStatus() == FlightStatus.DRAFT);
	}

	/**
	 * Returns if this Flight is a Historic flight.
	 * @param fr the Flight Report
	 * @return TRUE if the ATTR_HISTORIC attribute is present, otherwise FALSE
	 * @see Attribute#HISTORIC
	 */
	public static boolean isHistoric(FlightReport fr) {
		return (fr != null) && fr.hasAttribute(Attribute.HISTORIC);
	}

	/**
	 * Returns if this Flight is a Charter operation.
	 * @param fr the Flight Report
	 * @return TRUE if the ATTR_CHARTER attribute is present, otherwise FALSE
	 * @see Attribute#CHARTER
	 */
	public static boolean isCharter(FlightReport fr) {
		return (fr != null) && fr.hasAttribute(Attribute.CHARTER);
	}

	/**
	 * Returns if this Flight is a Flight Academy Training Flight.
	 * @param fr the Flight Report
	 * @return TRUE if the ATTR_ACADEMY attribute is present, otherwise FALSE
	 * @see Attribute#ACADEMY
	 */
	public static boolean isAcademy(FlightReport fr) {
		return (fr != null) && fr.hasAttribute(Attribute.ACADEMY);
	}

	/**
	 * Returns if the Flight was flown using a rated equipment type.
	 * @param fr the Flight Report
	 * @return TRUE if the NOTRATED attribute is not present, otherwise FALSE
	 * @see Attribute#NOTRATED
	 */
	public static boolean isRated(FlightReport fr) {
		return (fr == null) || !fr.hasAttribute(Attribute.NOTRATED);
	}
	
	/**
	 * Returns if this Flight was a diversion to another airport.
	 * @param fr the FlightReport
	 * @return TRUE if the DIVERT attribute is present, otherwsie FALSE
	 * @see Attribute#DIVERT
	 */
	public static boolean isDivert(FlightReport fr) {
		return (fr != null) && fr.hasAttribute(Attribute.DIVERT);
	}
	
	/**
	 * Returns if this Flight has On-Time data.
	 * @param fr the FlightReport
	 * @return TRUE if OnTime data is present, otherwise FALSE
	 */
	public static boolean hasOnTime(FlightReport fr) {
		return (fr != null) && (fr instanceof ACARSFlightReport afr) && (afr.getOnTime() != OnTime.UNKNOWN);
	}
	
	/**
	 * Returns if a Flight Report has a valid FDE file name.
	 * @param fr the FlightReport
	 * @return TRUE if a valid FDE file name is present, otherwise FALSE
	 */
	public static boolean hasFDE(FlightReport fr) {
		return (fr != null) && (fr instanceof ACARSFlightReport afr) && (fr.getSimulator() != Simulator.FS2020) && !StringUtils.isEmpty(afr.getFDE());
	}

	/**
	 * Returns if the Flight's route does not exist in the Schedule database.
	 * @param fr the Flight Report
	 * @return TRUE if the ROUTEWARN atribute is present, otherwise FALSE
	 * @see Attribute#ROUTEWARN
	 */
	public static boolean routeWarn(FlightReport fr) {
		return (fr != null) && fr.hasAttribute(Attribute.ROUTEWARN);
	}

	/**
	 * Returns if the Flight's distance exceeds the parameters in the Schedule database.
	 * @param fr the Flight Report
	 * @return TRUE if the RANGEWARN attribute is present, otherwise FALSE
	 * @see Attribute#RANGEWARN
	 */
	public static boolean rangeWarn(FlightReport fr) {
		return (fr != null) && fr.hasAttribute(Attribute.RANGEWARN);
	}

	/**
	 * Returns if the takeoff or landing runway exceeds the Aircraft's minimums.
	 * @param fr the Flight Report
	 * @return TRUE if the RWYWARN attribute is present, otherwise FALSE
	 * @see Attribute#RWYWARN
	 */
	public static boolean runwayWarn(FlightReport fr) {
		return (fr != null) && fr.hasAttribute(Attribute.RWYWARN);
	}

	/**
	 * Returns if the Flight's duration exceeds the parameters in the Schedule database.
	 * @param fr the Flight Report
	 * @return TRUE if the TIMEWARN attribute is present, otherwise FALSE
	 * @see Attribute#TIMEWARN
	 */
	public static boolean timeWarn(FlightReport fr) {
		return (fr != null) && fr.hasAttribute(Attribute.TIMEWARN);
	}

	/**
	 * Returns if in-flight refueling was detected.
	 * @param fr the Flight report
	 * @return TRUE if the REFUELWARN attribute is present, otherwise FALSE
	 * @see Attribute#REFUELWARN
	 */
	public static boolean refuelWarn(FlightReport fr) {
		return (fr != null) && fr.hasAttribute(Attribute.REFUELWARN);
	}

	/**
	 * Returns if this Flight was operated using non-ETOPS-rated aircraft on an ETOPS route.
	 * @param fr the Flight Report
	 * @return TRUE if the ETOPSWARN attribute is present, otherwise FALSE
	 * @see Attribute#ETOPSWARN
	 */
	public static boolean etopsWarn(FlightReport fr) {
		return ((fr != null) && fr.hasAttribute(Attribute.ETOPSWARN));
	}
	
	/**
	 * Returns if this Flight entered Prohibited or Restricted airspace.
	 * @param fr the FlightReport
	 * @return TRUE if the AIRSPACEWARN attribute is present, otherwise FALSE
	 * @see Attribute#AIRSPACEWARN
	 */
	public static boolean airspaceWarn(FlightReport fr) {
		return ((fr != null) && fr.hasAttribute(Attribute.AIRSPACEWARN));
	}

	/**
	 * Returns if excessive takeoff or landing weight was detected.
	 * @param fr the FlightReport
	 * @return TRUE if the WEIGHTWARN attribute is present, otherwise FALSE
	 * @see Attribute#WEIGHTWARN
	 */
	public static boolean weightWarn(FlightReport fr) {
		return ((fr != null) && fr.hasAttribute(Attribute.WEIGHTWARN));
	}
	
	/**
	 * Returns if the flight was planned using SimBrief.
	 * @param fr the FlightReport
	 * @return TRUE if the SIMBRIEF attribute is present, otherwise FALSE
	 * @see Attribute#SIMBRIEF
	 */
	public static boolean isSimBrief(FlightReport fr) {
		return ((fr != null) && fr.hasAttribute(Attribute.SIMBRIEF));
	}

	/**
	 * Returns if any warnings have been set.
	 * @param fr the FlightReport
	 * @return TRUE if any warnings are set, otherwise FALSE
	 * @see Attribute#hasWarning(int)
	 */
	public static boolean hasWarn(FlightReport fr) {
		return ((fr != null) && Attribute.hasWarning(fr.getAttributes()));
	}

	/**
	 * Returns if this Flight counts towards promotion to Captain.
	 * @param fr the FlightReport
	 * @return TRUE if the Leg counts towards promotion, otherwise FALSE
	 */
	public static boolean promoLeg(FlightReport fr) {
		return ((fr != null) && !fr.getCaptEQType().isEmpty());
	}
	
	/**
	 * Returns if this Flight has visible aircraft capabilities.
	 * @param fr the FlightReport
	 * @return TRUE if it has visible Capabilities flags, otherwise FALSE
	 */
	public static boolean hasVisibleCapabilities(FlightReport fr) {
		if (!(fr instanceof ACARSFlightReport)) return false;
		long flags = ((ACARSFlightReport) fr).getCapabilities();
		for (Capabilities c : Capabilities.values()) {
			if (c.isVisible() && c.has(flags))
				return true;
		}
		
		return false;
	}

	/**
	 * Returns the Equipment Programs this Flight counts towards promotion to Captain in.
	 * @param fr the FlightReport
	 * @return a comma-delimited string of Equipment Program names
	 */
	public static String promoTypes(FlightReport fr) {
		return StringUtils.listConcat(fr.getCaptEQType(), ",");
	}

	/**
	 * Returns the name of the online network used on this Flight.
	 * @param fr the FlightReport
	 * @return the name of the network, or &quot;Offline&quot; if flown offline
	 * @see FlightReportFunctions#isOnline(FlightReport)
	 */
	public static String network(FlightReport fr) {
		return ((fr == null) || (fr.getNetwork() == null)) ? "Offline" : fr.getNetwork().toString();
	}

	/**
	 * Returns whether the Flight was logged using an aircraft-specific SDK.
	 * @param fr the FlightReport
	 * @return TRUE if an SDK was detected, otherwise FALSE
	 */
	public static boolean hasSDK(FlightReport fr) {
		if (!(fr instanceof ACARSFlightReport afr)) return false;
		String sdk = afr.getSDK();
		return !StringUtils.isEmpty(sdk) && !"Generic".equalsIgnoreCase(sdk);
	}
}