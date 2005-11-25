package org.deltava.beans.stats;

import java.io.Serializable;

/**
 * A bean to store airline statistics entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightStatsEntry implements Serializable, Comparable {

	private Comparable _label;
	
	private int _legs;
	private double _hours;
	private int _miles;
	
	/**
	 * Creates a new statistics entry.
	 * @param entryLabel the entry label, which can be a Date, Number or String
	 * @param legs the number of legs for this entry
	 * @param hours the number of hours for this entry <i>multiplied by 10</i> 
	 * @param miles the number of miles for this entry
	 */
	public FlightStatsEntry(Comparable entryLabel, int legs, double hours, int miles) {
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
	public Object getLabel() {
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
		return (_legs == 0) ? 0 : _miles / _legs;
	}
	
	/**
	 * Compares the entries by using the natural sort order of the labels.
	 * @see Comparable#compareTo(Object)
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(Object o2) {
		FlightStatsEntry e2 = (FlightStatsEntry) o2;
		return (_label.compareTo(e2._label));
	}
}