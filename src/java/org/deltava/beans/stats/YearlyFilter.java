// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.Helper;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.FlightReportComparator;

/**
 * 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

@Helper(YearlySummary.class)
public class YearlyFilter {
	
	private final int _pilotID;
	private final List<FlightReport> _flights = new ArrayList<FlightReport>();

	/**
	 * Creates the bean.
	 * @param pilotID the Pilot's database ID
	 * @param flights a Pilot's complete logbook 
	 */
	public YearlyFilter(int pilotID, Collection<FlightReport> flights) {
		super();
		_pilotID = pilotID;
		flights.stream().filter(fr -> (fr.getStatus() == FlightStatus.OK)).forEach(_flights::add);
		_flights.sort(new FlightReportComparator(FlightReportComparator.DATE));
	}

	public YearlySummary getSummary(int year) {
		
		final Instant ed = LocalDate.of(year + 1, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
		final Instant sd = LocalDate.of(year, 1, 1).atStartOfDay().minusSeconds(1).toInstant(ZoneOffset.UTC);
		
		YearlySummary cy = new YearlySummary(_pilotID, year);
		_flights.stream().filter(fr -> fr.getDate().isAfter(sd) && fr.getDate().isBefore(ed)).forEach(cy::add);
		
		// Get previous year data
		Collection<FlightReport> pyFlights = _flights.stream().filter(fr -> !cy.contains(fr)).toList();
		Collection<Airport> pyAP = pyFlights.stream().map(RoutePair::getAirports).flatMap(Collection::stream).collect(Collectors.toSet());
		Collection<String> pyEQ = pyFlights.stream().map(FlightReport::getEquipmentType).collect(Collectors.toSet());
		
		// Get new Airports/equipment
		cy.getAirports().stream().filter(ap -> !pyAP.contains(ap)).forEach(cy::addNewAirport);
		cy.getEquipmentTypes().stream().filter(eq -> !pyEQ.contains(eq)).forEach(cy::addNewEquipment);
		return cy;
	}
}