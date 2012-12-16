// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.flightplan;

import org.deltava.beans.navdata.Gate;

/**
 * A Flight Plan Generator for MSFS and successors that supports gates.
 * @author Luke
 * @version 5.1
 * @since 5.1
 */

public abstract class MSFSGenerator extends FlightPlanGenerator {

	protected Gate _gateD;

	/**
	 * Sets the departure Gate.
	 * @param g the departure Gate
	 */
	public void setGateD(Gate g) {
		_gateD = g;
	}
}