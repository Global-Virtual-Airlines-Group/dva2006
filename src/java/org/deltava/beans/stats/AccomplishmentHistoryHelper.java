 // Copyright 2010, 2011, 2014, 2015, 2016, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.AccomplishUnit.Data;

import org.deltava.comparators.FlightReportComparator;

import org.deltava.util.system.SystemData;

/**
 * A utility class to determine what Accomplishments a Pilot has achieved. 
 * @author Luke
 * @version 8.1
 * @since 3.2
 */

@Helper(Accomplishment.class)
public class AccomplishmentHistoryHelper {

	/**
	 * Enumeration to track whether a Pilot meets or exceeds the requirements for an Accomplishment.
	 */
	public enum Result {
		NOTYET, MEET, EXCEED;
	}
	
	private final Pilot _usr;
	private final Collection<FlightReport> _pireps = new TreeSet<FlightReport>(new FlightReportComparator(FlightReportComparator.DATE));
	private final Collection<DispatchConnectionEntry> _cons = new TreeSet<DispatchConnectionEntry>();
	
	private static class MutableInteger {
		private int _value;
		
		MutableInteger(int value) {
			super();
			_value = value;
		}
		
		public void inc() {
			_value++;
		}
		
		public Integer getValue() {
			return Integer.valueOf(_value);
		}
		
		@Override
		public String toString() {
			return String.valueOf(_value);
		}
		
		@Override
		public int hashCode() {
			return _value;
		}
	}
	
	private static class AccomplishmentCounter {

		private int _legs;
		private int _historicLegs;
		private final Collection<Integer> _events = new HashSet<Integer>();
		private int _onlineLegs;
		private long _miles;
		private long _pax;
		
		private int _dspFlights;
		private double _dspHours;

		private final Collection<Airport> _airports = new HashSet<Airport>();
		private final Collection<Airport> _airportD = new HashSet<Airport>();
		private final Collection<Airport> _airportA = new HashSet<Airport>();
		private final Collection<Airline> _airlines = new HashSet<Airline>();
		private final Collection<Country> _countries = new TreeSet<Country>();
		private final Collection<String> _conts = new TreeSet<String>();
		private final Collection<State> _states = new TreeSet<State>();
		private final Map<String, MutableInteger> _eqLegs = new TreeMap<String, MutableInteger>();
		private final Map<String, MutableInteger> _pLegs = new TreeMap<String, MutableInteger>();
		
		private final Map<String, MutableInteger> _daLegs = new TreeMap<String, MutableInteger>();
		private final Map<String, MutableInteger> _aaLegs = new TreeMap<String, MutableInteger>();

		AccomplishmentCounter() {
			super();
		}
		
		public void add(FlightReport fr) {
			_legs++;
			_miles += fr.getDistance();
			_pax += fr.getPassengers();
			_airlines.add(fr.getAirline());
			add(fr.getAirportD());
			_airportD.add(fr.getAirportD());
			add(fr.getAirportA());
			_airportA.add(fr.getAirportA());
			if (fr.hasAttribute(FlightReport.ATTR_HISTORIC)) _historicLegs++;
			if (fr.hasAttribute(FlightReport.ATTR_ONLINE_MASK)) _onlineLegs++;
			if (fr.getDatabaseID(DatabaseID.EVENT) != 0) _events.add(Integer.valueOf(fr.getDatabaseID(DatabaseID.EVENT)));
			incLeg(_eqLegs, fr.getEquipmentType());
			incLeg(_daLegs, fr.getAirportD().getIATA());
			incLeg(_aaLegs, fr.getAirportA().getIATA());
			fr.getCaptEQType().forEach(eq -> incLeg(_pLegs, eq));
		}
		
		static void incLeg(Map<String, MutableInteger> map, String key) {
			MutableInteger i = map.get(key);
			if (i == null)
				map.put(key, new MutableInteger(1));
			else
				i.inc();
		}
		
		void add(DispatchConnectionEntry dce) {
			_dspHours += (dce.getTime() / 3600.0d);
			_dspFlights += dce.getFlights().size();
		}
		
		private void add(Airport a) {
			_airports.add(a);
			_countries.add(a.getCountry());
			_conts.add(a.getCountry().getContinent());
			State s = a.getState();
			if (s != null)
				_states.add(s);
		}
		
		public Collection<Airport> getAirports() {
			return _airports;
		}
		
		public Collection<Airport> getDepartureAirports() {
			return _airportD;
		}
		
		public Collection<Airport> getArrivalAirports() {
			return _airportA;
		}
		
		public Collection<Country> getCountries() {
			return _countries;
		}
		
		public Collection<String> getContinents() {
			return _conts;
		}
		
		public Collection<State> getStates() {
			return _states;
		}
		
		public Collection<String> getEquipmentTypes() {
			return _eqLegs.keySet();
		}
		
		public Collection<Airline> getAirlines() {
			return _airlines;
		}
		
		public int getLegs() {
			return _legs;
		}
		
		public long getMiles() {
			return _miles;
		}
		
		public long getPassengers() {
			return _pax;
		}
		
		public int getHistoricLegs() {
			return _historicLegs;
		}
		
		public int getEvents() {
			return _events.size();
		}
		
		public int getOnlineLegs() {
			return _onlineLegs;
		}
		
		public int getDispatchedFlights() {
			return _dspFlights;
		}
		
		public double getDispatchHours() {
			return _dspHours;
		}
		
		public Integer getEquipmentLegs(String eqType) {
			return _eqLegs.containsKey(eqType) ? _eqLegs.get(eqType).getValue() : Integer.valueOf(0);
		}
		
		public Integer getPromotionLegs(String eqType) {
			return _pLegs.containsKey(eqType) ? _pLegs.get(eqType).getValue() : Integer.valueOf(0);
		}
		
		public Integer getDepartureLegs(String iata) {
			return _daLegs.containsKey(iata) ? _daLegs.get(iata).getValue() : Integer.valueOf(0);
		}
		
		public Integer getArrivalLegs(String iata) {
			return _aaLegs.containsKey(iata) ? _aaLegs.get(iata).getValue() : Integer.valueOf(0);
		}
	}
	
	// Cache counters so we don't need to iterate through PIREPs for everything.
	private final AccomplishmentCounter _totals = new AccomplishmentCounter();
	
	/**
	 * Creates the bean.
	 * @param p the Pilot
	 */
	public AccomplishmentHistoryHelper(Pilot p) {
		super();
		_usr = p;
	}
	
	/**
	 * Adds a FlightReport to the totals. 
	 * @param fr a FlightReport bean
	 */
	public void add(FlightReport fr) {
		if (fr.getStatus() == FlightStatus.OK) {
			_pireps.add(fr);
			_totals.add(fr);
		}
	}
	
	/**
	 * Adds a Dispatch connection to the totals.
	 * @param ce a ConnectionEntry bean
	 */
	public void add(ConnectionEntry ce) {
		if (ce.getDispatch() && (ce.getEndTime() != null)) {
			DispatchConnectionEntry dce = (DispatchConnectionEntry) ce;
			_totals.add(dce);
			_cons.add(dce);
		}
	}
	
	/**
	 * Determines whether a Pilot has achieved a particular Accomplishment. This returns an Enumeration
	 * that indicates whether the Pilot exactly matches the requirements, or exceeds them.
	 * @param a the Accomplishment bean
	 * @return a Result
	 */
	public Result has(Accomplishment a) {
		return has(a, _totals);
	}
	
	/**
	 * Returns how far a pilot is towards a particular Accomplishment.
	 * @param a the Accomplishment
	 * @return the value achieved thus far
	 */
	public long getProgress(Accomplishment a) {
		return progress(a, _totals);
	}

	/*
	 * Returns how far a pilot is towards a particular Accomplishment.
	 */
	private long progress(Accomplishment a, AccomplishmentCounter cnt) {
		switch (a.getUnit()) {
			case LEGS:
				return cnt.getLegs();
			case MILES:
				return cnt.getMiles();
			case PAX:
				return cnt.getPassengers();
			case OLEGS:
				return cnt.getOnlineLegs();
			case HLEGS:
				return cnt.getHistoricLegs();
			case EVENTS:
				return cnt.getEvents();
			case AIRPORTS:
				return AccomplishmentFilter.filter(cnt.getAirports(), a).size();
			case AIRPORTA:
				return AccomplishmentFilter.filter(cnt.getArrivalAirports(), a).size();
			case AIRPORTD:
				return AccomplishmentFilter.filter(cnt.getDepartureAirports(), a).size();
			case ADLEGS:
				return a.getChoices().stream().map(ap -> cnt.getDepartureLegs(ap)).mapToInt(Integer::intValue).sum();
			case AALEGS:
				return a.getChoices().stream().map(ap -> cnt.getArrivalLegs(ap)).mapToInt(Integer::intValue).sum();
			case AIRCRAFT:
				return AccomplishmentFilter.filter(cnt.getEquipmentTypes(), a).size();
			case COUNTRIES:
				return AccomplishmentFilter.filter(cnt.getCountries(), a).size();
			case CONTINENTS:
				return AccomplishmentFilter.filter(cnt.getContinents(), a).size();
			case STATES:
				return AccomplishmentFilter.filter(cnt.getStates(), a).size();
			case AIRLINES:
				return AccomplishmentFilter.filter(cnt.getAirlines(), a).size();
			case DFLIGHTS:
				return cnt.getDispatchedFlights();
			case DHOURS:
				return (long) cnt.getDispatchHours();
			case EQLEGS:
				return a.getChoices().stream().map(eqType -> cnt.getEquipmentLegs(eqType)).mapToInt(Integer::intValue).sum();
			case PROMOLEGS:
				return a.getChoices().stream().map(eqType -> cnt.getPromotionLegs(eqType)).mapToInt(Integer::intValue).sum();
			case MEMBERDAYS:
				return (System.currentTimeMillis() - _usr.getCreatedOn().toEpochMilli()) / 86400_000;
			default:
				return 0;
		}
	}
	
	/*
	 * Determines whether a Pilot has achieved a particular Accomplishment using a specific set of totals.
	 */
	private Result has(Accomplishment a, AccomplishmentCounter cnt) {
		if (!a.getActive())
			return Result.NOTYET;
		
		long value = progress(a, cnt);
		if (value < a.getValue())
			return Result.NOTYET;
		
		return (value == a.getValue()) ? Result.MEET : Result.EXCEED;
	}
	
	/**
	 * Returns missing elements from a particular Accomplishment.
	 * @param a the Accomplishment bean
	 * @return a Collection of missing objects
	 */
	public Collection<?> missing(Accomplishment a) {
		if (has(a) != Result.NOTYET)
			return Collections.emptySet();
		
		// Big switch based on types
		Collection<Object> results = new LinkedHashSet<Object>();
		Collection<String> codes = new TreeSet<String>();
		switch (a.getUnit()) {
			case AIRPORTS:
				codes.addAll(AccomplishmentFilter.missing(_totals.getAirports(), a));
				codes.stream().map(code -> SystemData.getAirport(code)).filter(Objects::nonNull).forEach(results::add);
				break;
				
			case AIRPORTD:
				codes.addAll(AccomplishmentFilter.missing(_totals.getDepartureAirports(), a));
				codes.stream().map(code -> SystemData.getAirport(code)).filter(Objects::nonNull).forEach(results::add);
				break;
				
			case AIRPORTA:
				codes.addAll(AccomplishmentFilter.missing(_totals.getArrivalAirports(), a));
				codes.stream().map(code -> SystemData.getAirport(code)).filter(Objects::nonNull).forEach(results::add);
				break;
			
			case CONTINENTS:
				results.addAll(AccomplishmentFilter.missing(_totals.getContinents(), a));
				break;
				
			case COUNTRIES:
				codes.addAll(AccomplishmentFilter.missing(_totals.getCountries(), a));
				codes.stream().map(code -> Country.get(code)).forEach(results::add);
				break;
			
			case STATES:
				codes.addAll(AccomplishmentFilter.missing(_totals.getStates(), a));
				codes.stream().map(code -> State.valueOf(code.toUpperCase())).filter(Objects::nonNull).forEach(results::add);
				break;
				
			case AIRCRAFT:
				results.addAll(AccomplishmentFilter.missing(_totals.getEquipmentTypes(), a));
				break;
				
			case AIRLINES:
				codes.addAll(AccomplishmentFilter.missing(_totals.getAirlines(), a));
				codes.stream().map(code -> SystemData.getAirline(code)).filter(Objects::nonNull).forEach(results::add);
				break;
		
			default:
				return Collections.emptySet();
		}
		
		return results;
	}
	
	/**
	 * Returns the date that a pilot achieved an Accomplishment.
	 * @param a the Accomplishment
	 * @return the date/time it was achieved, or null
	 */
	public Instant achieved(Accomplishment a) {
		if (has(a) == Result.NOTYET)
			return null;
		
		// Check join date
		if (a.getUnit() == AccomplishUnit.MEMBERDAYS) {
			ZonedDateTime zdt = ZonedDateTime.ofInstant(_usr.getCreatedOn(), ZoneOffset.UTC);
			return zdt.plusDays(a.getValue()).toInstant();
		}
			
		// Loop through the Flight Reports
		AccomplishmentCounter cnt = new AccomplishmentCounter();
		if (a.getUnit().getDataRequired() == Data.FLIGHTS) {
			for (FlightReport fr : _pireps) {
				Instant dt = (fr.getSubmittedOn() == null) ? fr.getDate() : fr.getSubmittedOn();
				cnt.add(fr);
				
				// If we meet the criteria, return the date
				if (has(a, cnt) != Result.NOTYET)
					return dt;
			}
		}
		
		// Loop through the connection entries
		if (a.getUnit().getDataRequired() == Data.DISPATCH) {
			for (DispatchConnectionEntry dce : _cons) {
				cnt.add(dce);
				if (has(a, cnt) != Result.NOTYET)
					return dce.getEndTime();
			}
		}
		
		return null;
	}
}