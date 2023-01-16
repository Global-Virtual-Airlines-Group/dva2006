// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;

import org.deltava.beans.Helper;
import org.deltava.beans.schedule.RoutePair;

/**
 * 
 * @author Luke
 * @version 10.4
 * @since 10.4
 */

@Helper(FlightReport.class)
public class LogbookHistoryHelper {
	
	private final List<FlightReport> _flights = new ArrayList<FlightReport>();

	/**
	 * Creates the helper.
	 * @param flights the logbook entries
	 */
	public LogbookHistoryHelper(Collection<FlightReport> flights) {
		super();
		flights.stream().filter(fr -> ((fr.getStatus() != FlightStatus.DRAFT) && (fr.getStatus() != FlightStatus.REJECTED))).forEach(_flights::add);
	}

	/**
	 * Returns if the logbook has been continuous for the past number of flights.
	 * @param flights the number of flights
	 * @return TRUE if the logbook is continuous, or FALSE if discontinuous or less than 2 entries in logbook 
	 */
	public boolean isContinuous(int flights) {
		if (_flights.size() < 2) return false;
		RoutePair rp = _flights.get(0);
		for (int x = 1; x <= Math.min(flights, _flights.size() - 1); x++) {
			RoutePair rp2 = _flights.get(x);
			if (!rp.getAirportD().equals(rp2.getAirportA()))
				return false;
			
			rp = rp2;
		}
		
		return true;
	}
	
	/**
	 * Returns the head of the list, trimming if cnt &lt; the list size.
	 */
	private List<FlightReport> head(int cnt) {
		return _flights.subList(0, Math.min(_flights.size(), cnt));
	}
	
	/**
	 * Returns if the last number of flights have all been flown with non-historic Airlines.
	 * @param flights the number of flights
	 * @return TRUE if the flights have all been flown with non-historic Airlines
	 */
	public boolean isCurent(int flights) {
		return head(flights).stream().allMatch(fr -> !fr.getAirline().getHistoric());
	}

	/**
	 * Returns if the last number of flights have all been flown with historic Airlines.
	 * @param flights the number of flights
	 * @return TRUE if the flights have all been flown with historic Airlines
	 */
	public boolean isHistoric(int flights) {
		return head(flights).stream().allMatch(fr -> fr.getAirline().getHistoric());
	}
	
	/**
	 * Returns if the last number of flights have all been flown with historic equipment.
	 * @param flights the number of flights
	 * @return TRUE if the flights have all been flown with historic Aircraft
	 */
	public boolean isHistoricEQ(int flights) {
		return head(flights).stream().allMatch(fr -> fr.hasAttribute(FlightReport.ATTR_HISTORIC));
	}
	
	/**
	 * Returns the last Flight Report.
	 * @return a FlightReport, or null if no flights in logbook
	 */
	public FlightReport getLastFlight() {
		return _flights.isEmpty() ? null : _flights.get(0);
	}
}