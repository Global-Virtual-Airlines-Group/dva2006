// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.List;

import org.deltava.commands.AbstractViewCommand;

import org.deltava.util.ComboUtils;

/**
 * An Abstract Command used to store options for Flight Report statistics.
 * @author Luke
 * @version 2.3
 * @since 2.1
 */

public abstract class AbstractStatsCommand extends AbstractViewCommand {
	
	/**
	 * Sort option SQL.
	 */
	public static final String[] SORT_CODE = {"LEGS", "MILES", "HOURS", "AVGHOURS", "AVGMILES", "F.DATE", "ACARSLEGS",
		"OLEGS", "HISTLEGS", "PIDS"};
	
	/**
	 * Sort option labels.
	 */
	public static final List SORT_OPTIONS = ComboUtils.fromArray(new String[] {"Flight Legs", "Miles Flown", "Flight Hours", 
			"Avg. Hours", "Avg. Miles", "Flight Date", "ACARS Legs", "Online Legs", "Historic Legs", "Distinct Pilots"}, SORT_CODE);

	/**
	 * Group option SQL.
	 */
	public static final String[] GROUP_CODE = {"CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME)", "F.DATE", "F.EQTYPE",
			"AL.NAME", "AP.AIRPORT_D", "AP.AIRPORT_A", "$MONTH", "DATE_SUB(F.DATE, INTERVAL WEEKDAY(F.DATE) DAY)" };
	
	/**
	 * Group option labels.
	 */
	public static final List GROUP_OPTIONS = ComboUtils.fromArray(new String[] {"Pilot Name", "Flight Date", "Equipment Type", 
			"Airline", "Departed from", "Arrived at", "Month", "Week" }, GROUP_CODE);
	
	/**
	 * SQL used to display friendly month/year.
	 */
	public static final String MONTH_SQL = "DATE_FORMAT(F.DATE, '%M %Y')";
}