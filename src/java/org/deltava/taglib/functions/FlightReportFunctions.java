// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import java.util.Iterator;

import org.deltava.beans.*;

/**
 * A JSP Function Library to define Flight Report-related functions.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class FlightReportFunctions {

	/**
	 * Returns the ACARS flight ID for this Flight Report.
	 * @param fr the Flight Report
	 * @return the ACARS database ID
	 */
    public static int ACARSID(FlightReport fr) {
        return (fr == null) ? 0 : fr.getDatabaseID(FlightReport.DBID_ACARS);
    }
    
    /**
     * Returns the Event ID for this Flight Report.
     * @param fr the Flight Report
     * @return the Event database ID
     */
    public static int eventID(FlightReport fr) {
        return (fr == null) ? 0 : fr.getDatabaseID(FlightReport.DBID_EVENT);
    }
    
    /**
     * Returns the Flight Assignment ID for this Flight Report.
     * @param fr the Flight Report
     * @return the Assignment database ID 
     */
    public static int assignID(FlightReport fr) {
        return (fr == null) ? 0 : fr.getDatabaseID(FlightReport.DBID_ASSIGN);
    }
    
    /**
     * Returns the Pilot ID for this Flight Report.
     * @param fr the Flight Report 
     * @return the Pilot database ID
     */
    public static int pilotID(FlightReport fr) {
    	return (fr == null) ? 0 : fr.getDatabaseID(FlightReport.DBID_PILOT);
    }
    
    /**
     * Returns if this Flight was logged using ACARS.
     * @param fr the Flight Report
     * @return TRUE if this flight used ACARS, otherwise FALSE
     */
    public static boolean isACARS(FlightReport fr) {
        return (fr instanceof ACARSFlightReport) || (fr.hasAttribute(FlightReport.ATTR_ACARS));
    }
    
    /**
     * Returns if this Flight was logged using Microsoft Flight Simulator.
     * @param fr the Flight Report
     * @return TRUE if the Flight was logged using a version of MSFS
     */
    public static boolean isMSFS(FlightReport fr) {
    	return (fr != null) && (fr.getFSVersion() != 100) && (fr.getFSVersion() != 0);
    }
    
    /**
     * Returns if the Flight was flown online.
     * @param fr the Flight Report
     * @return TRUE if the ATTR_ONLINE MASK attribute is present, otherwise FALSE
     * @see FlightReportFunctions#network(FlightReport)
     * @see FlightReport#ATTR_ONLINE_MASK
     */
    public static boolean isOnline(FlightReport fr) {
        return (fr != null) && fr.hasAttribute(FlightReport.ATTR_ONLINE_MASK);
    }
    
    /**
     * Returns if this Flight is a Check Ride.
     * @param fr the Flight Report
     * @return TRUE if the ATTR_CHECKRIDE attribute is present, otherwise FALSE
     * @see FlightReport#ATTR_CHECKRIDE
     */
    public static boolean isCheckRide(FlightReport fr) {
       return (fr != null) && fr.hasAttribute(FlightReport.ATTR_CHECKRIDE);
    }
    
    /**
     * Returns if this Flight was planned by a Dispatcher.
     * @param fr the Flight report
     * @return TRUE if the ATTR_DISPATCH attribute is present, otherwise FALSE
     * @see FlightReport#ATTR_DISPATCH
     */
    public static boolean isDispatch(FlightReport fr) {
    	return (fr != null) && fr.hasAttribute(FlightReport.ATTR_DISPATCH);
    }
    
    /**
     * Returns if this Flight Report is a Draft.
     * @param fr the Flight Report
     * @return TRUE if the Flight is in Draft status, otherwise FALSE
     * @see FlightReport#DRAFT 
     */
    public static boolean isDraft(FlightReport fr) {
    	return (fr != null) && (fr.getStatus() == FlightReport.DRAFT);
    }
    
    /**
     * Returns if this Flight is a Historic flight.
     * @param fr the Flight Report
     * @return TRUE if the ATTR_HISTORIC attribute is present, otherwise FALSE
     * @see FlightReport#ATTR_HISTORIC
     */
    public static boolean isHistoric(FlightReport fr) {
    	return (fr != null) && fr.hasAttribute(FlightReport.ATTR_HISTORIC);
    }
    
    /**
     * Returns if this Flight is a Charter operation.
     * @param fr the Flight Report
     * @return TRUE if the ATTR_CHARTER attribute is present, otherwise FALSE
     * @see FlightReport#ATTR_CHARTER
     */
    public static boolean isCharter(FlightReport fr) {
    	return (fr != null) && fr.hasAttribute(FlightReport.ATTR_CHARTER);
    }
    
    /**
     * Returns if this Flight is a Flight Academy Training Flight.
     * @param fr the Flight Report
     * @return TRUE if the ATTR_ACADEMY attribute is present, otherwise FALSE
     * @see FlightReport#ATTR_ACADEMY
     */
    public static boolean isAcademy(FlightReport fr) {
    	return (fr != null) && fr.hasAttribute(FlightReport.ATTR_ACADEMY);
    }
    
    /**
     * Returns if the Flight was flown using a rated equipment type.
     * @param fr the Flight Report
     * @return TRUE if the NOTRATED attribute is not present, otherwise FALSE
     * @see FlightReport#ATTR_NOTRATED
     */
    public static boolean isRated(FlightReport fr) {
        return (fr == null) || !fr.hasAttribute(FlightReport.ATTR_NOTRATED);
    }
    
    /**
     * Returns if the Flight's route does not exist in the Schedule database.
     * @param fr the Flight Report
     * @return TRUE if the ROUTEWARN atribute is present, otherwise FALSE
     * @see FlightReport#ATTR_ROUTEWARN
     */
    public static boolean routeWarn(FlightReport fr) {
        return (fr != null) && fr.hasAttribute(FlightReport.ATTR_ROUTEWARN);
    }
    
    /**
     * Returns if the Flight's distance exceeds the parameters in the Schedule database.
     * @param fr the Flight Report
     * @return TRUE if the RANGEWARN attribute is present, otherwise FALSE
     * @see FlightReport#ATTR_RANGEWARN
     */
    public static boolean rangeWarn(FlightReport fr) {
    	return (fr != null) && fr.hasAttribute(FlightReport.ATTR_RANGEWARN);
    }
    
    /**
     * Returns if the Flight's duration exceeds the parameters in the Schedule database.
     * @param fr the Flight Report
     * @return TRUE if the TIMEWARN attribute is present, otherwise FALSE
     * @see FlightReport#ATTR_TIMEWARN
     */
    public static boolean timeWarn(FlightReport fr) {
        return (fr != null) && fr.hasAttribute(FlightReport.ATTR_TIMEWARN);
    }
    
    /**
     * Returns if in-flight refueling was detected.
     * @param fr the Flight report
     * @return TRUE if the REFUELWARN attribute is present, otherwise FALSE
     * @see FlightReport#ATTR_REFUELWARN
     */
    public static boolean refuelWarn(FlightReport fr) {
    	return (fr != null) && fr.hasAttribute(FlightReport.ATTR_REFUELWARN);
    }
    
    /**
     * Returns if this Flight was operated using non-ETOPS-rated aircraft on an ETOPS route.
     * @param fr the Flight Report
     * @return TRUE if the ETOPSWARN attribute is present, otherwise FALSE
     * @see FlightReport#ATTR_ETOPSWARN
     */
    public static boolean etopsWarn(FlightReport fr) {
    	return ((fr != null) && fr.hasAttribute(FlightReport.ATTR_ETOPSWARN));
    }
    
    /**
     * Returns if this Flight counts towards promotion to Captain.
     * @param fr the Flight Report
     * @return TRUE if the Leg counts towards promotion, otherwise FALSE
     */
    public static boolean promoLeg(FlightReport fr) {
        return (fr != null) && (!fr.getCaptEQType().isEmpty());
    }
    
    /**
     * Returns the Equipment Programs this Flight counts towards promotion to Captain in. 
     * @param fr the Flight Report
     * @return a comma-delimited string of Equipment Program names
     */
    public static String promoTypes(FlightReport fr) {
    	StringBuilder buf = new StringBuilder();
    	if (fr != null) {
    		for (Iterator i = fr.getCaptEQType().iterator(); i.hasNext(); ) {
    			String eqType = (String) i.next();
    			buf.append(eqType);
    			if (i.hasNext())
    				buf.append(',');
    		}
    	}
    	
    	return buf.toString();
    }
    
    /**
     * Returns the name of the online network used on this Flight.
     * @param fr the Flight Report
     * @return the name of the network, or &quot;Offline&quot; if flown offline
     * @see FlightReportFunctions#isOnline(FlightReport)
     * @see FlightReport#ATTR_VATSIM
     * @see FlightReport#ATTR_IVAO
     * @see FlightReport#ATTR_FPI
     */
    public static String network(FlightReport fr) {
    	if (fr == null)
    		return "Offline";
    	else if (fr.hasAttribute(FlightReport.ATTR_VATSIM))
            return OnlineNetwork.VATSIM;
        else if (fr.hasAttribute(FlightReport.ATTR_IVAO))
            return OnlineNetwork.IVAO;
        else if (fr.hasAttribute(FlightReport.ATTR_FPI))
            return OnlineNetwork.FPI;
        
        return "Offline";
    }
}