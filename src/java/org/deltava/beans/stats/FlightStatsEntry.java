// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved. 
package org.deltava.beans.stats;

import java.util.*;

/**
 * A bean to store Flight statistics entries.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class FlightStatsEntry implements Comparable<FlightStatsEntry> {

	private String _label;
	
	private int _legs;
	private int _acarsLegs;
	private int _onlineLegs;
	private int _historicLegs;
	private int _dispatchLegs;
	private double _hours;
	private int _miles;
	private int _pilotIDs;

	private final Map<Long, Integer> _verLegs = new TreeMap<Long, Integer>();
	
	/**
	 * Creates a new statistics entry.
	 * @param entryLabel the entry label, which can be a Date, Number or String
	 * @param legs the number of legs for this entry
	 * @param hours the number of hours for this entry <i>multiplied by 10</i> 
	 * @param miles the number of miles for this entry
	 */
	public FlightStatsEntry(String entryLabel, int legs, double hours, int miles) {
		super();
		_label = entryLabel;
		_legs = legs;
		_hours = hours;
		_miles = miles;
	}

	/**
	 * Returns the entry label.
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}
	
	/**
	 * Returns the number of legs linked to this entry.
	 * @return the number of legs
	 */
	public int getLegs() {
		return _legs;
	}

	/**
	 * Returns the number of ACARS legs linked to this entry.
	 * @return the number of legs using ACARS
	 * @see FlightStatsEntry#setACARSLegs(int)
	 */
	public int getACARSLegs() {
		return _acarsLegs;
	}
	
	/**
	 * Returns the number of Dispatch legs linked to this entry.
	 * @return the number of legs using ACARS Dispatch
	 * @see FlightStatsEntry#setDispatchLegs(int)
	 */
	public int getDispatchLegs() {
		return _dispatchLegs;
	}
	
	/**
	 * Returns the number of Historic legs linked to this entry.
	 * @return the number of legs using historic equipment
	 * @see FlightStatsEntry#setHistoricLegs(int)
	 */
	public int getHistoricLegs() {
		return _historicLegs;
	}
	
	/**
	 * Returns the number of distinct Pilots flying legs in this period.
	 * @return the number of pilots flying legs
	 * @see FlightStatsEntry#setPilotIDs(int)
	 */
	public int getPilotIDs() {
		return _pilotIDs;
	}
	
	/**
	 * Returns the number of Online legs linked to this entry. 
	 * @return the number of legs flown online
	 * @see FlightStatsEntry#setOnlineLegs(int)
	 */
	public int getOnlineLegs() {
		return _onlineLegs;
	}
	
	/**
	 * Returns the number of flight hours linked to this entry.
	 * @return the number of hours
	 * @see FlightStatsEntry#getAvgHours()
	 */
	public double getHours() {
		return _hours;
	}
	
	/**
	 * Returns the number of miles linked to this entry.
	 * @return the number of miles
	 * @see FlightStatsEntry#getAvgMiles()
	 */
	public int getMiles() {
		return _miles;
	}
	
	/**
	 * Returns the average number of flight hours per leg for this entry.
	 * @return the average hours per leg
	 * @see FlightStatsEntry#getHours()
	 */
	public double getAvgHours() {
		return (_legs == 0) ? 0 : _hours / _legs;
	}
	
	/**
	 * Returns the average number of miles per leg for this entry.
	 * @return the average miles per leg
	 * @see FlightStatsEntry#getMiles()
	 */
	public double getAvgMiles() {
		return (_legs == 0) ? 0 : _miles * 1.0 / _legs;
	}
	
	/**
	 * Returns the percentage of flights logged using ACARS.
	 * @return the percentage of ACARS flights
	 * @see FlightStatsEntry#setACARSLegs(int)
	 * @see FlightStatsEntry#getACARSLegs()
	 */
	public double getACARSPercent() {
		return (_legs == 0) ? 0 : _acarsLegs * 1.0 / _legs;
	}
	
	/**
	 * Returns the Map displaying flight legs by Flight Simulator verison.
	 * @return a Map of legs, keyed by version
	 */
	public Map<Long, Integer> getVersionLegs() {
		return _verLegs;
	}
	
	/**
	 * Sets the legs for a specific Flight Simulator version. Version should be set to
	 * 7 through 10 for FS2000 through FSX, and 0 for other.
	 * @param version the version code
	 * @param legs the number of legs
	 */
	public void setFSVersionLegs(int version, int legs) {
		_verLegs.put(Long.valueOf(version), Integer.valueOf(legs));
	}
	
	public void setVersionLegs(Map<Long, Integer> legs) {
		_verLegs.putAll(legs);
	}
	
	/**
	 * Updates the number of ACARS legs linked to this entry.
	 * @param legs the number of legs logged using ACARS
	 * @see FlightStatsEntry#getACARSLegs()
	 */
	public void setACARSLegs(int legs) {
		_acarsLegs = legs;
	}
	
	/**
	 * Updates the number of Dispatch legs linked to this entry.
	 * @param legs the number of legs logged using ACARS Dispatch
	 * @see FlightStatsEntry#getDispatchLegs()
	 */
	public void setDispatchLegs(int legs) {
		_dispatchLegs = legs;
	}
	
	/**
	 * Updates the number of Historic legs linked to this entry.
	 * @param legs the number of legs flown using Historic equipment
	 * @see FlightStatsEntry#getHistoricLegs()
	 */
	public void setHistoricLegs(int legs) {
		_historicLegs = legs;
	}
	
	/**
	 * Updates the number of Online legs linked to this entry.
	 * @param legs the number of legs flown online
	 * @see FlightStatsEntry#getOnlineLegs()
	 */
	public void setOnlineLegs(int legs) {
		_onlineLegs = legs;
	}
	
	/**
	 * Updates the number of Pilots flying legs during this period.
	 * @param ids the number of pilots
	 * @see FlightStatsEntry#getPilotIDs()
	 */
	public void setPilotIDs(int ids) {
		_pilotIDs = ids;
	}
	
	/**
	 * Returns the entry label.
	 */
	public String toString() {
		return _label;
	}
	
	/**
	 * Returns the label's hash code.
	 */
	public int hashCode() {
		return _label.hashCode();
	}
	
	/**
	 * Compares the entries by using the natural sort order of the labels.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(FlightStatsEntry e2) {
		return (_label.compareTo(e2._label));
	}
}