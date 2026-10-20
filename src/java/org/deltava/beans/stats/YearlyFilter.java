// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.FlightReportComparator;

/**
 * A helper bean to filter FlightReports and other dated objects by year. 
 * @author Luke
 * @version 13.0
 * @since 12.5
 */

@Helper(YearlySummary.class)
public class YearlyFilter {
	
	private final int _pilotID;
	private final List<FlightReport> _flights = new ArrayList<FlightReport>();
	
	private static class DateFilter implements Predicate<CalendarEntry> {
		private final int _year;
		
		DateFilter(int year) {
			_year = year;
		}
		
		@Override
		public boolean test(CalendarEntry t) {
			ZonedDateTime zdt = ZonedDateTime.ofInstant(t.getDate(), ZoneOffset.UTC);
			return (zdt.getYear() == _year);
		}
	}

	/**
	 * Creates a Predicate used to filter objects with a date, by year.
	 * @param year the year to filter for
	 * @return a Predicate
	 */
	public static Predicate<CalendarEntry> filter(int year) {
		return new DateFilter(year);
	}
	
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

	/**
	 * Generates a yearly flight summary for a particular year.
	 * @param year the year
	 * @return a YearlySummary
	 */
	public YearlySummary getSummary(int year) {

		// Filter the flights by year
		YearlySummary cy = new YearlySummary(_pilotID, year);
		_flights.stream().filter(new DateFilter(year)).forEach(cy::add);
		
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