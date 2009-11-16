// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.apache.log4j.Logger;

import org.deltava.beans.EquipmentType;

/**
 * A utility class to determine whether a Flight counts for promotion to Captain in a particular Equipment Type program.
 * @author Luke
 * @version 2.7
 * @since 2.7
 */

public class FlightPromotionHelper {

	private static final Logger log = Logger.getLogger(FlightPromotionHelper.class);

	private FlightReport _fr;
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
		_comment = null;
		if ((_fr == null) || (eq == null))
			return false;

		ACARSFlightReport afr = (_fr instanceof ACARSFlightReport) ? (ACARSFlightReport) _fr : null;
		if ((afr == null) && eq.getACARSPromotionLegs()) {
			log.info(eq.getName() + " requires flights using ACARS");
			_comment = eq.getName() + " requires flights using ACARS";
			return false;
		} else if (_fr.getDistance() < eq.getPromotionMinLength()) {
			log.info("Minimum " + eq.getName() + " flight length is "  +  eq.getPromotionMinLength() + ", distance=" + _fr.getDistance());
			_comment = "Minimum flight length for promotion to Captain in " + eq.getName() + " is "  +  eq.getPromotionMinLength() + " miles";
			return false;
		} else if ((afr != null) && (_fr.getDistance() < eq.getPromotionSwitchLength()) && ((afr.getTime(2) + afr.getTime(4)) > eq.getMaximumAccelTime())) {
			log.info("Time at 1X = " + afr.getTime(1) + " time at 2X/4X = " + (afr.getTime(2) + afr.getTime(4)));
			_comment = "Time at 1X = " + afr.getTime(1) + " time at 2X/4X = " + (afr.getTime(2) + afr.getTime(4));
			return false;
		} else if ((afr != null) && (_fr.getDistance() >= eq.getPromotionSwitchLength()) && (afr.getTime(1) < eq.getMinimum1XTime())) {
			log.info("Time at 1X = " + afr.getTime(1) + " time at 2X/4X = " + (afr.getTime(2) + afr.getTime(4)));
			_comment = "Time at 1X = " + afr.getTime(1) + " time at 2X/4X = " + (afr.getTime(2) + afr.getTime(4));
			return false;
		}

		return true;
	}
}