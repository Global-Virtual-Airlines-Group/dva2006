// Copyright 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.ComboAlias;

/**
 * An enumeration to store Flight Statistics sorting options.
 * @author Luke
 * @version 7.5
 * @since 6.3
 */

public enum FlightStatsSort implements ComboAlias {

	LEGS("Flight Legs", "SL DESC"), MILES ("Miles Flown", "SM DESC"), HOURS("Flight Hours", "SH DESC"), AVGHOURS("Average Hours", "AVGHOURS DESC"),
	AVGMILES("Average Miles", "AVGMILES DESC"), DATE("Flight Date", "F.DATE DESC"), ALEGS("ACARS Flights", "SAL DESC"), OLEGS("Online Flights", "OLEGS DESC"),
	OVLEGS("VATSIM Flights", "OVL DESC"), OILEGS("IVAO Flights", "OIL DESC"), HLEGS("Historic Flights", "SHL DESC"), DSPLEGS("Dispatched Flights", "SDL DESC"),
	PIDS("Distinct Pilots", "PIDS DESC"), PAX("Passengers", "SP DESC"), LF("Load Factor", "LF DESC");
	
	FlightStatsSort(String label, String sql) {
		_label = label;
		_sql = sql;
	}
	
	private final String _label;
	private final String _sql;
	
	/**
	 * Returns the sort option label.
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}
	
	/**
	 * Returns the sort option SQL.
	 * @return the SQL
	 */
	public String getSQL() {
		return _sql;
	}

	@Override
	public String getComboName() {
		return _label;
	}
	
	@Override
	public String getComboAlias() {
		return name();
	}

	/**
	 * Retrieves an enumeration value from a string, with a default value 
	 * @param v the string
	 * @param defValue the default value if unknown
	 * @return a FlightStatsSort enum
	 */
	public static FlightStatsSort from(String v, FlightStatsSort defValue) {
		try {
			return FlightStatsSort.valueOf(v.toUpperCase());
		} catch (Exception e) {
			return defValue;
		}
	}
}