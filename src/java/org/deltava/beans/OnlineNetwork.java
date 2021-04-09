// Copyright 2006, 2008, 2011, 2014, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to store Online Network constants.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public enum OnlineNetwork {
	VATSIM, IVAO, FPI, INTVAS, ACARS, PILOTEDGE, POSCON;

	@Override
	public String toString() {
		return (this != PILOTEDGE) ? name() : "PilotEdge";
	}
}