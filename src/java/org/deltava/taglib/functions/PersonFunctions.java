// Copyright 2006, 2008, 2010, 2014, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import org.deltava.beans.*;

/**
 * A JSP Function Library for Pilot-related functions.
 * @author Luke
 * @version 9.1
 * @since 1.0
 */

public class PersonFunctions {
	
	// static class
	private PersonFunctions() {
		super();
	}

	/**
	 * Returns whether the user's supplied e-mail address is valid.
	 * @param usr the e-mail address
	 * @return TRUE if the address is valid, otherwise FALSE
	 */
	public static boolean isEMailValid(Person usr) {
		return EMailAddress.isValid(usr);
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
		PilotStatus ps = isPilot(usr) ? ((Pilot) usr).getStatus() : null;
		return (ps == PilotStatus.SUSPENDED);
	}

	/**
	 * Returns whether the Person is an Active or On Leave Pilot.
	 * @param usr the Person
	 * @return TRUE if an Active or On Leave Pilot, otherwise FALSE
	 */
	public static boolean isActive(Person usr) {
		PilotStatus ps = isPilot(usr) ? ((Pilot) usr).getStatus() : null;
		return (ps == PilotStatus.ACTIVE) || (ps == PilotStatus.ONLEAVE);
	}
	
	/**
	 * Returns whether the Person is a member of a security role.
	 * @param roleName the role name
	 * @param usr the Person
	 * @return TRUE if the Person is a member of the Role, otherwise FALSE
	 */
	public static boolean hasRole(String roleName, Person usr) {
		return isPilot(usr) && usr.isInRole(roleName);
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
	
	/**
	 * Returns whether a Person has a particular IM address.
	 * @param usr the Person
	 * @param imType the Instant Message type
	 * @return TRUE if the Person has an address, otherwise FALSE
	 */
	public static boolean hasIM(Person usr, String imType) {
		try {
			IMAddress addr = IMAddress.valueOf(imType.toUpperCase());
			return (usr == null) ? false : (usr.getIMHandle(addr) != null);
		} catch (Exception e) {
			// empty
		}
		
		return false;
	}
}