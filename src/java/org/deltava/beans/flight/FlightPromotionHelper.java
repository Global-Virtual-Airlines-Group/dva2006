// Copyright 2009, 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.*;

/**
 * A utility class to determine whether a Flight counts for promotion to Captain in a particular Equipment Type program.
 * @author Luke
 * @version 7.0
 * @since 2.7
 */

@Helper(FlightReport.class)
public final class FlightPromotionHelper {

	private final FlightReport _fr;
	private String _comment;

	/**
	 * Initializes the helper.
	 * @param fr the FlightReport bean
	 */
	public FlightPromotionHelper(FlightReport fr) {
		super();
		_fr = fr;
	}
	
	/**
	 * Returns the last reason why a Flight does not count for promotion to Captain.
	 * @return the last reason, or null if none
	 */
	public String getLastComment() {
		return _comment;
	}

	/**
	 * Checks whether this Flight Report counts for promotion to Captain in a particular equipment program.
	 * @param eq the EquipmentType bean
	 * @return TRUE if the flight counts for promotion, otherwise FALSE
	 * @see FlightPromotionHelper#getLastComment()
	 */
	public boolean canPromote(EquipmentType eq) {
		if ((_fr == null) || (eq == null)) {
			_comment = "No flight/equipment type";
			return false;
		}

		FDRFlightReport ffr = (_fr instanceof FDRFlightReport) ? (FDRFlightReport) _fr : null;
		ACARSFlightReport afr = (_fr instanceof ACARSFlightReport) ? (ACARSFlightReport) _fr : null;
		if ((ffr == null) && eq.getACARSPromotionLegs()) {
			_comment = eq.getName() + " requires flights using ACARS/XACARS/simFDR for promotion";
			return false;
		} else if (_fr.getDistance() < eq.getPromotionMinLength()) {
			_comment = "Minimum flight length for promotion to Captain in " + eq.getName() + " is "  +  eq.getPromotionMinLength() + " statute miles";
			return false;
		} else if (_fr.hasAttribute(FlightReport.ATTR_CHARTER)) {
			_comment = "Charter flights not eligible for promotion";
			return false;
		} else if ((afr != null) && (_fr.getDistance() < eq.getPromotionSwitchLength()) && ((afr.getTime(2) + afr.getTime(4)) > eq.getMaximumAccelTime())) {
			_comment = "Time at 1X = " + afr.getTime(1) + "sec, time at 2X/4X = " + (afr.getTime(2) + afr.getTime(4)) + "sec";
			return false;
		} else if ((afr != null) && (_fr.getDistance() >= eq.getPromotionSwitchLength()) && (afr.getTime(1) < eq.getMinimum1XTime())) {
			_comment = "Time at 1X = " + afr.getTime(1) + "sec, time at 2X/4X = " + (afr.getTime(2) + afr.getTime(4)) + "sec";
			return false;
		}

		_comment = null;
		return true;
	}
}