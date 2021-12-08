// Copyright 2015, 2017, 2018, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

/**
 * An enumeration to store Flight Statistics grouping options.
 * @author Luke
 * @version 10.2
 * @since 6.3
 */

public enum FlightStatsGroup implements org.deltava.beans.ComboAlias {

	PILOT("Pilot Name", "CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME)"), DATE("Flight Date", "F.DATE"), EQ("Equipment Type", "F.EQTYPE"),
	AP("Airport", "AP.NAME"), AD("Departed from", "F.AIRPORT_D"), AA("Arrived at", "F.AIRPORT_A"), MONTH("Month", "DATE_FORMAT(F.DATE, '%M %Y')"),
	WEEK("Week", "DATE_SUB(F.DATE, INTERVAL WEEKDAY(F.DATE) DAY)"), YEAR("Year", "YEAR(F.DATE)");
	
	private final String _label;
	private final String _sql;
	
	FlightStatsGroup(String label, String sql) {
		_label = label;
		_sql = sql;
	}
	
	/**
	 * Returns the group option label.
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}
	
	/**
	 * Returns the group option SQL.
	 * @return the SQL
	 */
	public String getSQL() {
		return _sql;
	}
	
	/**
	 * Returns whether this groups Pilot.
	 * @return TRUE if using P.*, otherwise FALSE
	 */
	public boolean isPilotGroup() {
		return _sql.contains(" P.");
	}

	/**
	 * Returns whether this groups by the flight date or its derivative.
	 * @return TRUE if using F.DATE, otherwise FALSE
	 */
	public boolean isDateGroup() {
		return _sql.contains("F.DATE");
	}
	
	/**
	 * Returns whether this groups by an airport.
	 * @return TRUE if using AP.*, otherwise FALSE
	 */
	public boolean isAirportGroup() {
		return _sql.contains("AP.") || _sql.contains("AIRPORT");
	}
	
	@Override
	public String getComboName() {
		return _label;
	}
	
	@Override
	public String getComboAlias() {
		return name();
	}
}