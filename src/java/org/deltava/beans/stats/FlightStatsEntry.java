// Copyright 2005, 2006, 2007, 2008, 2010, 2015, 2020 Global Virtual Airlines Group. All Rights Reserved. 
package org.deltava.beans.stats;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.Simulator;

/**
 * A bean to store Flight statistics entries.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class FlightStatsEntry implements java.io.Serializable, Comparable<FlightStatsEntry> {

	private final String _label;
	
	private final int _legs;
	private int _acarsLegs;
	private int _vatsimLegs;
	private int _ivaoLegs;
	private int _historicLegs;
	private int _dispatchLegs;
	private final double _hours;
	private final int _miles;
	private int _pilotIDs;
	private double _loadFactor;
	private int _pax;

	private final Map<Simulator, Integer> _verLegs = new TreeMap<Simulator, Integer>();
	
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
	 */
	public int getOnlineLegs() {
		return _vatsimLegs + _ivaoLegs;
	}
	
	/**
	 * Returns the number of IVAO legs linked to this entry. 
	 * @return the number of legs flown on IVAO
	 * @see FlightStatsEntry#setIVAOLegs(int)
	 */
	public int getIVAOLegs() {
		return _ivaoLegs;
	}
	
	/**
	 * Returns the number of VATSIM legs linked to this entry. 
	 * @return the number of legs flown on VATSIM
	 * @see FlightStatsEntry#setVATSIMLegs(int)
	 */
	public int getVATSIMLegs() {
		return _vatsimLegs;
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
	 * Returns the distance linked to this entry.
	 * @return the distance in statute miles
	 * @see FlightStatsEntry#getAvgDistance()
	 */
	public int getDistance() {
		return _miles;
	}
	
	/**
	 * Returns the number of passengers linked to this entry.
	 * @return the number of passengers
	 */
	public int getPax() {
		return _pax;
	}
	
	/**
	 * Returns the average load factor linked to this entry.
	 * @return the average load factor
	 */
	public double getLoadFactor() {
		return _loadFactor;
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
	 * Returns the average distance for this entry.
	 * @return the average distance in statute miles per leg
	 * @see FlightStatsEntry#getDistance()
	 */
	public double getAvgDistance() {
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
	public Map<String, Integer> getVersionLegs() {
		Map<String, Integer> results = new HashMap<String, Integer>();
		_verLegs.entrySet().forEach(me -> results.put(me.getKey().name(), me.getValue()));
		return results;
	}
	
	/**
	 * Returns the simulators used in this stats period.
	 * @return a Collection of Simulators
	 */
	public Collection<Simulator> getSimulators() {
		return _verLegs.entrySet().stream().filter(me -> me.getValue().intValue() > 0).map(Map.Entry::getKey).collect(Collectors.toSet());
	}
	
	/**
	 * Sets the legs for a specific Simulator version.
	 * @param s the Simulator value
	 * @param legs the number of legs
	 */
	public void setFSVersionLegs(Simulator s, int legs) {
		_verLegs.put(s, Integer.valueOf(legs));
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
	 * Updates the number of VATSIM legs linked to this entry.
	 * @param legs the number of legs flown on VATSIM
	 * @see FlightStatsEntry#getVATSIMLegs()
	 */
	public void setVATSIMLegs(int legs) {
		_vatsimLegs = legs;
	}
	
	/**
	 * Updates the number of IVAO legs linked to this entry.
	 * @param legs the number of legs flown on IVAO
	 * @see FlightStatsEntry#getIVAOLegs()
	 */
	public void setIVAOLegs(int legs) {
		_ivaoLegs = legs;
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
	 * Updates the number of passengers linked to this entry.
	 * @param pax the number of passeengers
	 * @see FlightStatsEntry#getPax()
	 */
	public void setPax(int pax) {
		_pax = Math.max(0, pax);
	}
	
	/**
	 * Updates the average load factor linked to this entry.
	 * @param lf the average load factor
	 * @see FlightStatsEntry#getLoadFactor()
	 */
	public void setLoadFactor(double lf) {
		_loadFactor = Math.max(0, Math.min(1, lf));
	}
	
	@Override
	public String toString() {
		return _label;
	}
	
	@Override
	public int hashCode() {
		return _label.hashCode();
	}
	
	/**
	 * Compares the entries by using the natural sort order of the labels.
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(FlightStatsEntry e2) {
		return (_label.compareTo(e2._label));
	}
}