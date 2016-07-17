// Copyright 2006, 2008, 2011, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to store Online Network constants.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public enum OnlineNetwork {
	VATSIM, IVAO, FPI, INTVAS, ACARS, PILOTEDGE;

	/**
	 * Parses an online network name.
	 * @param name the name
	 * @return an OnlineNetwork, or null
	 */
	public static OnlineNetwork fromName(String name) {
		try {
			return OnlineNetwork.valueOf(name.toUpperCase());
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return (this != PILOTEDGE) ? name() : "PilotEdge";
	}
}