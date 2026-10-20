// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.FlightReportComparator;

/**
 * A bean to store a year's Flights.
 * @author Luke
 * @version 13.0
 * @since 12.5
 */

public class YearlySummary implements AuthoredBean {
	
	private final int _year;
	private final int _pilotID;
	
	private final Collection<FlightReport> _flights = new TreeSet<FlightReport>(new FlightReportComparator(FlightReportComparator.DATE));
	private final Collection<Airport> _newAirports = new TreeSet<Airport>();
	private final Collection<String> _newEQ = new TreeSet<String>();

	/**
	 * Creates the bean.
	 * @param pilotID the Pilot database ID
	 * @param year the year 
	 */
	public YearlySummary(int pilotID, int year) {
		super();
		_year = year;
		_pilotID = pilotID;
	}
	
	/**
	 * Returns the year.
	 * @return the year
	 */
	public int getYear() {
		return _year;
	}

	@Override
	public int getAuthorID() {
		return _pilotID;
	}
	
	/**
	 * Returns the Airports visited during the year.
	 * @return a Collection of Airports
	 */
	public Collection<Airport> getAirports() {
		return _flights.stream().map(RoutePair::getAirports).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
	}
	
	/**
	 * Returns the equipment types used during the year.
	 * @return a sorted Collection of equipment types
	 */
	public Collection<String> getEquipmentTypes() {
		return _flights.stream().map(FlightReport::getEquipmentType).collect(Collectors.toCollection(TreeSet::new));
	}
	
	/**
	 * Returns the total distance flown in the year.
	 * @return the distance in miles
	 */
	public int getDistance() {
		return _flights.stream().mapToInt(FlightReport::getDistance).sum();
	}
	
	/**
	 * Returns the flights flown in the year.
	 * @return a Collection of FlightReports
	 */
	public Collection<FlightReport> getFlights() {
		return _flights;
	}
	
	/**
	 * Returns new Airports visited in this year.
	 * @return a Collection of Airports
	 */
	public Collection<Airport> getNewAirports() {
		return _newAirports;
	}
	
	/**
	 * Returns new equipment types used in this year.
	 * @return a Collection of equipment types
	 */
	public Collection<String> getNewEquipment() {
		return _newEQ;
	}
	
	/**
	 * Returns whether a Flight report is included in this Summary.
	 * @param fr the FlightReport
	 * @return TRUE if included, otherwise FALSE
	 */
	public boolean contains(FlightReport fr) {
		return _flights.contains(fr);
	}
	
	public int size() {
		return _flights.size();
	}

	/**
	 * Adds a Flight to the year.
	 * @param fr a FlightReport
	 */
	public void add(FlightReport fr) {
		_flights.add(fr);
	}
	
	/**
	 * Adds an Airport first visited in this year.
	 * @param a an Airport
	 */
	public void addNewAirport(Airport a) {
		_newAirports.add(a);
	}
	
	/**
	 * Adds an aircraft type first visited in this year.
	 * @param eqType the equipment type
	 */
	public void addNewEquipment(String eqType) {
		_newEQ.add(eqType);
	}
	
	@Override
	public void setAuthorID(int id) {
		DatabaseBean.validateID(_pilotID, id);
	}
}