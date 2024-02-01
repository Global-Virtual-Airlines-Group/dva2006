// Copyright 2006, 2008, 2010, 2014, 2019, 2020, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import org.deltava.beans.*;

import org.deltava.util.EnumUtils;

/**
 * A JSP Function Library for Pilot-related functions.
 * @author Luke
 * @version 11.2
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
		return (usr instanceof Applicant);
	}
	
	/**
	 * Returns whether the Person is a Pilot.
	 * @param usr the Person
	 * @return TRUE if a Pilot, otherwise FALSE 
	 */
	public static boolean isPilot(Person usr) {
		return (usr instanceof Pilot);
	}
	
	/**
	 * Returns whether the Person is a Suspended Pilot.
	 * @param usr the Person
	 * @return TRUE if a Supended Pilot, otherwise FALSE
	 */
	public static boolean isSuspended(Person usr) {
		return (usr instanceof Pilot p) && (p.getStatus() == PilotStatus.SUSPENDED);
	}

	/**
	 * Returns whether the Person is an Active or On Leave Pilot.
	 * @param usr the Person
	 * @return TRUE if an Active or On Leave Pilot, otherwise FALSE
	 */
	public static boolean isActive(Person usr) {
		return (usr instanceof Pilot p) && p.getStatus().isActive();
	}
	
	/**
	 * Returns whether the Person is a member of a security role.
	 * @param roleName the role name
	 * @param usr the Person
	 * @return TRUE if the Person is a member of the Role, otherwise FALSE
	 */
	public static boolean hasRole(String roleName, Person usr) {
		return (usr instanceof Pilot) && usr.isInRole(roleName);
	}
	
	/**
	 * Returns a Person's external ID.
	 * @param usr the Person
	 * @param idType the external ID type name
	 * @return the external ID, or null 
	 */
	public static String getExternalID(Person usr, String idType) {
		ExternalID extID = EnumUtils.parse(ExternalID.class, idType, null);
		return ((usr == null) || (extID == null)) ? null : usr.getExternalID(extID);
	}
	
	/**
	 * Returns a Person's external ID.
	 * @param usr the Person
	 * @param netName the OnlineNetwork
	 * @return the network ID, or null 
	 */
	public static String getNetworkID(Person usr, String netName) {
		OnlineNetwork net = EnumUtils.parse(OnlineNetwork.class, netName, null);
		return ((usr == null) || (net == null)) ? null : usr.getNetworkID(net);
	}
}