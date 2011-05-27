// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

import org.gvagroup.acars.ACARSFlags;

/**
 * A bean to track fuel usage and in-flight refueling on ACARS flights.
 * @author Luke
 * @version 3.7
 * @since 3.7
 */

public class FuelUse {
	
	/**
	 * Maximum delta between positions before trigerring in-flight refueling flag.
	 */
	public static final int MAX_DELTA = 1250;
	
	private int _totalFuel;
	private boolean _hasRefuel;

	/**
	 * Returns whether refueling occured in-flight.
	 * @return TRUE if refueling occured, otherwise FALSE
	 */
	public boolean getRefuel() {
		return _hasRefuel;
	}
	
	/**
	 * Returns the total amount of fuel used.
	 * @return the amount of fuel in pounds
	 */
	public int getTotalFuel() {
		return _totalFuel;
	}

	/**
	 * Adds an amount of fuel to the total fuel used.
	 * @param fuel the amount of fuel in pounds
	 */
	public void addFuelUse(int fuel) {
		_totalFuel += fuel;
	}
	
	/**
	 * Sets whether refueling occured in-flight.
	 * @paraam inflightRefuel TRUE if refueling occured, otherwise FALSE
	 */
	public void setRefuel(boolean inflightRefuel) {
		_hasRefuel = inflightRefuel;
	}
	
	/**
	 * Calculates fuel use from flight data.
	 * @param positions a Collection of RouteEntry beans
	 * @return a FuelUse bean
	 */
	public static FuelUse validate(Collection<RouteEntry> positions) {
		FuelUse fu = new FuelUse(); int lastFuel = 0;
		for (RouteEntry re : positions) {
			int fuel = re.getFuelRemaining();
			if (lastFuel != 0) {
				int fuelDelta = (lastFuel - fuel);
				if (fuelDelta < -MAX_DELTA) {
					if (!re.isFlagSet(ACARSFlags.FLAG_ONGROUND))
						fu.setRefuel(true);
				} else if (fuelDelta > 0)
					fu.addFuelUse(fuelDelta);
			}
			
			lastFuel = fuel;
		}
		
		return fu;
	}
}