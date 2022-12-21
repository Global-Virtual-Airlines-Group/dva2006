// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.OnlineNetwork;

/**
 * A bean to store online flight statistics entries.
 * @author Luke
 * @version 10.3
 * @since 10.2
 */

public class OnlineStatsEntry extends LegHoursStatsEntry<OnlineNetwork> {

	private final String _label;
	private int _legs;
	
	/**
	 * Creates the bean.
	 * @param label the date/time
	 */
	public OnlineStatsEntry(String label) {
		super();
		_label = label;
	}
	
	/**
	 * Returns the total number of flight legs flown across all networks during this period.
	 * @return the number of legs
	 */
	public int getOnlineLegs() {
		return getKeys().stream().mapToInt(n -> getLegs(n)).sum();
	}
	
	/**
	 * Returns the total number of flight hours flown across all networks during this period.
	 * @return the number of hours
	 */
	public double getOnlineHours() {
		return getKeys().stream().mapToDouble(n -> getHours(n)).sum();
	}
	
	/**
	 * Returns the total number of miles flown across all networks during this period.
	 * @return the number of miles
	 */
	public int getOnlineDistance() {
		return getKeys().stream().mapToInt(n -> getDistance(n)).sum();
	}
	
	/**
	 * Returns the total number of legs flown during this period.
	 * @return the number of legs
	 */
	public int getTotalLegs() {
		return _legs;
	}
	
	/**
	 * Returns the statistics label.
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}
	
	/**
	 * Sets online network statistics.
	 * @param net the OnlineNetwork
	 * @param legs the number of legs
	 * @param distance the flight distance in miles
	 * @param hours the number of hours
	 */
	public void setNetwork(OnlineNetwork net, int legs, int distance, double hours) {
		set(net, legs, distance, hours);
	}
	
	/**
	 * Updates the total number of legs flown during this period.
	 * @param legs the number of legs
	 */
	public void setTotalLegs(int legs) {
		_legs = legs;
	}
	
	@Override
	public int hashCode() {
		return _label.hashCode();
	}
}