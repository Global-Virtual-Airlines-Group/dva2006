// Copyright 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file.innovata;

import java.util.*;

import org.deltava.beans.schedule.*;

/**
 * A class to track multiple-leg flights during an Innovata schedule import.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

class MultiLegInfo implements Comparable<MultiLegInfo> {
	
	private DailyScheduleEntry _startEnd; 
	
	private String _flightCode;
	private int _flightNumber;
	private List<String> _apCodes = new ArrayList<String>();
	private List<DailyScheduleEntry> _entries = new ArrayList<DailyScheduleEntry>();
	private boolean _authAirportList;
	
	/**
	 * Creates a new Multiple Leg information bean.
	 * @param flightCode the flight code
	 * @param fNumber the flight number
	 */
	MultiLegInfo(String flightCode, int fNumber) {
		super();
		_flightCode = flightCode;
		_flightNumber = fNumber;
	}

	/**
	 * Returns the Flight Code.
	 * @return the flight code
	 */
	public String getFlightCode() {
		return _flightCode;
	}
	
	public DailyScheduleEntry getMainEntry() {
		return _startEnd;
	}

	/**
	 * Compares two multi-leg beans by comparing their flight numbers.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(MultiLegInfo i2) {
		return Integer.valueOf(_flightNumber).compareTo(Integer.valueOf(i2._flightNumber));
	}

	/**
	 * Adds an Airport to the list of airports served by this flight.
	 * @param apCode the airport code
	 */
	public void addAirport(String apCode) {
		if (_apCodes.isEmpty())
			_apCodes.add(apCode);
		else {
			String lastCode = _apCodes.get(_apCodes.size() - 1);
			if (!lastCode.equals(apCode))
				_apCodes.add(apCode);
		}
	}
	
	public void setMainEntry(DailyScheduleEntry se) {
		_startEnd = se;
	}

	/**
	 * Adds a number of Airports to the list of airports served by this flight.
	 */
	private void addAirports(String apCodes, String sep) {
		StringTokenizer tkns = new StringTokenizer(apCodes, sep);
		while (tkns.hasMoreTokens())
			_apCodes.add(tkns.nextToken().toUpperCase());
	}

	public void setAirports(Airport aD, Airport aA, String stopCodes) {
		_apCodes.clear();
		addAirport(aD.getIATA());
		addAirports(stopCodes, "!");
		addAirport(aA.getIATA());
		_authAirportList = true;
	}

	/**
	 * Adds a Schedule Entry.
	 * @param se the Schedule Entry bean
	 */
	public void addEntry(DailyScheduleEntry se) {
		_entries.add(se);
	}
	
	/**
	 * Adds a Schedule Entry, replacing an existing Entry covering this route pair.
	 * @param se the Schedule Entry bean
	 */
	public void replaceEntry(DailyScheduleEntry se) {
		ScheduleEntry ee = getEntry(se.getAirportD(), se.getAirportA());
		if (ee != null)
			_entries.remove(ee);
		
		_entries.add(se);
	}
	
	/**
	 * Returns a leg entry for a given airport pair.
	 * @param ad the departure Airport bean
	 * @param aa the arrival Airport bean
	 * @return the first ScheduleEntry bean matching these airports, or null if not found
	 */
	public DailyScheduleEntry getEntry(Airport ad, Airport aa) {
		for (Iterator<DailyScheduleEntry> i = _entries.iterator(); i.hasNext(); ) {
			DailyScheduleEntry se = i.next();
			if (se.getAirportA().equals(aa) && se.getAirportD().equals(ad))
				return se;
		}
	
		return null;
	}
	
	public List<String> getDepartsFrom() {
		if (_apCodes.isEmpty())
			return Collections.emptyList();

		List<String> apCodes = new ArrayList<String>(_apCodes);
		apCodes.remove(apCodes.size() - 1);
		return apCodes;
	}

	public int getLegs() {
		return _entries.size();
	}
	
	/**
	 * Returns wether an authoritative airport list has been loaded.
	 * @return TRUE if all airports are loaded, otherwise FALSE
	 */
	public boolean isAirportListLoaded() {
		return _authAirportList;
	}

	/**
	 * Returns the Schedule entries for this flight, setting the leg numbers.
	 * @return a Collection of ScheduleEntry beans
	 */
	public Collection<ScheduleEntry> getEntries() {

		// Do no processing if only one leg
		if (_entries.size() == 1)
			return new HashSet<ScheduleEntry>(_entries);
		else if (_entries.size() == 0) {
			Collection<ScheduleEntry> results = new HashSet<ScheduleEntry>();
			results.add(_startEnd);
			return results;
		}

		// Order the departure airports
		List<String> apCodes = getDepartsFrom();

		// Build the results
		List<ScheduleEntry> results = new ArrayList<ScheduleEntry>(_entries.size());
		for (int x = 0; x < _entries.size(); x++)
			results.add(null);

		// Order the legs
		for (Iterator<DailyScheduleEntry> i = _entries.iterator(); i.hasNext();) {
			DailyScheduleEntry e = i.next();
			int ofs = apCodes.indexOf(e.getAirportD().getIATA());
			if (ofs >= results.size()) {
				//System.out.println(_flightCode);
			} else if (ofs != -1) {
				e.setLeg(ofs + 1);
				results.set(ofs, e);
			}
		}
		
		return results;
	}
}