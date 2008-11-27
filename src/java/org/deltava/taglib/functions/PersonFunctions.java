// Copyright 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;

/**
 * A JSP Function Library for Pilot-related functions.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class PersonFunctions {

	/**
	 * Returns whether the supplied e-mail address is valid.
	 * @param addr the e-mail address
	 * @return TRUE if the address is valid, otherwise FALSE
	 */
	public static boolean isEMailValid(String addr) {
		return (!StringUtils.isEmpty(addr)) && (!EMailAddress.INVALID_ADDR.equals(addr));
	}
	
	/**
	 * Returns whether the Person is an Applicant.
	 * @param usr the Person
	 * @return TRUE if an Applicant, otherwise FALSE 
	 */
	public static boolean isApplicant(Person usr) {
		return ((usr != null) && (usr instanceof Applicant));
	}
	
	/**
	 * Returns whether the Person is a Pilot.
	 * @param usr the Person
	 * @return TRUE if a Pilot, otherwise FALSE 
	 */
	public static boolean isPilot(Person usr) {
		return ((usr != null) && (usr instanceof Pilot));
	}
	
	/**
	 * Returns whether the Person is a Suspended Pilot.
	 * @param usr the Person
	 * @return TRUE if a Supended Pilot, otherwise FALSE
	 */
	public static boolean isSuspended(Person usr) {
		return isPilot(usr) && (usr.getStatus() == Pilot.SUSPENDED);
	}

	/**
	 * Returns whether the Person is an Active or On Leave Pilot.
	 * @param usr the Person
	 * @return TRUE if an Active or On Leave Pilot, otherwise FALSE
	 */
	public static boolean isActive(Person usr) {
		return isPilot(usr) && ((usr.getStatus() == Pilot.ACTIVE) || (usr.getStatus() == Pilot.ON_LEAVE));
	}
	
	/**
	 * Returns a Person's online network ID.
	 * @param usr the Person
	 * @param name the network name
	 * @return the network ID, or null 
	 */
	public static String getNetworkID(Person usr, String name) {
		try {
			OnlineNetwork net = OnlineNetwork.valueOf(name.toUpperCase());
			return (usr == null) ? null : usr.getNetworkID(net);
		} catch (Exception e) {
			// empty
		}
		
		return null;
	}
}