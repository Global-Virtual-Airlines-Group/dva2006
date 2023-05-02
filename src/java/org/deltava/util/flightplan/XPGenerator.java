// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.flightplan;

import org.deltava.beans.navdata.Navaid;

/**
 * A Flight Plan Generator for X-Plane. 
 * @author Luke
 * @version 10.6
 * @since 10.6
 */

abstract class XPGenerator extends FlightPlanGenerator {

	/**
	 * Maps Navaid types to X-Plane FMS types.
	 * @param nt a Navaid
	 * @return the X-Plane type code
	 */
	protected static int getNavaidType(Navaid nt) {
		return switch (nt) {
			case NDB -> 2;
			case VOR -> 3;
			case AIRPORT -> 1;
			default -> 11;
		};
	}
}