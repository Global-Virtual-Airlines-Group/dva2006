 // Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.flight.DatabaseID;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.schedule.Airport;

/**
 * A utility class to determine what Accomplishments a Pilot has achieved. 
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class AccomplishmentHistoryHelper {

	/**
	 * Enumeration to track whether a Pilot meets or exceeds the requirements for an Accomplishment.
	 */
	public enum Result {
		NOTYET, MEET, EXCEED;
	}
	
	private Pilot _usr;
	private final Collection<FlightReport> _pireps = new TreeSet<FlightReport>();
	
	private static class AccomplishmentCounter {

		private int _legs;
		private int _historicLegs;
		private int _eventLegs;
		private int _onlineLegs;
		private long _miles;

		private final Collection<String> _airports = new HashSet<String>();
		private final Collection<String> _eqTypes = new HashSet<String>();
		private final Collection<String> _countries = new HashSet<String>();
		private final Collection<String> _states = new HashSet<String>();

		AccomplishmentCounter() {
			super();
		}
		
		public void add(FlightReport fr) {
			_legs++;
			_miles += fr.getDistance();
			_eqTypes.add(fr.getEquipmentType());
			add(fr.getAirportD());
			add(fr.getAirportA());
			if (fr.hasAttribute(FlightReport.ATTR_HISTORIC)) _historicLegs++;
			if (fr.hasAttribute(FlightReport.ATTR_ONLINE_MASK)) _onlineLegs++;
			if (fr.getDatabaseID(DatabaseID.EVENT) != 0) _eventLegs++;
		}
		
		private void add(Airport a) {
			_airports.add(a.getIATA());
			_countries.add(a.getCountry().getCode());
			if ("US".equals(a.getCountry().getCode())) {
				String state = a.getName().substring(a.getName().lastIndexOf(' ') + 1);
				if (state.length() == 2)
					_states.add(state);
			}
		}
		
		public int getAirportCount() {
			return _airports.size();
		}
		
		public int getCountryCount() {
			return _countries.size();
		}
		
		public int getStateCount() {
			return _states.size();
		}
		
		public int getEquipmentCount() {
			return _eqTypes.size();
		}
		
		public int getLegs() {
			return _legs;
		}
		
		public long getMiles() {
			return _miles;
		}
		
		public int getHistoricLegs() {
			return _historicLegs;
		}
		
		public int getEventLegs() {
			return _eventLegs;
		}
		
		public int getOnlineLegs() {
			return _onlineLegs;
		}
	}
	
	// Cache counters so we don't need to iterate through PIREPs for everything.
	private final AccomplishmentCounter _totals = new AccomplishmentCounter();
	
	/**
	 * Creates the bean.
	 * @param p the Pilot
	 * @param flights the Pilot's Flight Reports
	 */
	public AccomplishmentHistoryHelper(Pilot p, Collection<FlightReport> flights) {
		super();
		_usr = p;
		
		// Only use approved PIREPs
		for (FlightReport fr : flights) {
			if (fr.getStatus() != FlightReport.OK)
				continue;
			
			_pireps.add(fr);
			_totals.add(fr);
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
	 * Determines whether a Pilot has achieved a particular Accomplishment using a specific set of totals.
	 */
	private Result has(Accomplishment a, AccomplishmentCounter cnt) {
		if (!a.getActive())
			return Result.NOTYET;
		
		// Big switch based on types
		switch (a.getUnit()) {
			case LEGS:
				return calc(cnt.getLegs(), a.getValue());
			case MILES:
				return calc(cnt.getMiles(), a.getValue());
			case OLEGS:
				return calc(cnt.getOnlineLegs(), a.getValue());
			case HLEGS:
				return calc(cnt.getHistoricLegs(), a.getValue());
			case ELEGS:
				return calc(cnt.getEventLegs(), a.getValue());
			case AIRPORTS:
				return calc(cnt.getAirportCount(), a.getValue());
			case AIRCRAFT:
				return calc(cnt.getEquipmentCount(), a.getValue());
			case COUNTRIES:
				return calc(cnt.getCountryCount(), a.getValue());
			case STATES:
				return calc(cnt.getStateCount(), a.getValue());
			case MEMBERDAYS:
				long days = (System.currentTimeMillis() - _usr.getCreatedOn().getTime()) / 86400000;
				return calc(days, a.getValue());
				
			default:
				return Result.NOTYET;
		}
	}
	
	/**
	 * Returns the date that a pilot achieved an Accomplishment.
	 * @param a the Accomplishment
	 * @return the date/time it was achieved, or null
	 */
	public Date achieved(Accomplishment a) {
		if (has(a) == Result.NOTYET)
			return null;
		
		// Loop through the Flight Reports
		AccomplishmentCounter cnt = new AccomplishmentCounter();
		for (FlightReport fr : _pireps) {
			Date dt = (fr.getSubmittedOn() == null) ? fr.getDate() : fr.getSubmittedOn();
			cnt.add(fr);
			
			// If we meet the criteria, return the date
			if (has(a, cnt) == Result.MEET)
				return dt;
		}
		
		return null;
	}
	
	/**
	 * Helper method to convert comparison into three-value Result enum.
	 */
	private Result calc(long v1, long v2) {
		if (v1 < v2)
			return Result.NOTYET;
		
		return (v1 == v2) ? Result.MEET : Result.EXCEED;
	}
}