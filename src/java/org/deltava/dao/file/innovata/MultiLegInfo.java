// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file.innovata;

import java.util.*;

import org.deltava.beans.schedule.*;

class MultiLegInfo implements Comparable {

	private String _flightCode;
	private int _flightNumber;
	private List<String> _apCodes = new ArrayList<String>();
	private List<ScheduleEntry> _entries = new ArrayList<ScheduleEntry>();

	MultiLegInfo(String flightCode, int fNumber) {
		super();
		_flightCode = flightCode;
		_flightNumber = fNumber;
	}

	public String getFlightCode() {
		return _flightCode;
	}

	public int compareTo(Object o2) {
		MultiLegInfo i2 = (MultiLegInfo) o2;
		return new Integer(_flightNumber).compareTo(new Integer(i2._flightNumber));
	}

	public void addAirport(String apCode) {
		if (_apCodes.isEmpty())
			_apCodes.add(apCode);
		else {
			String lastCode = _apCodes.get(_apCodes.size() - 1);
			if (!lastCode.equals(apCode))
				_apCodes.add(apCode);
		}
	}

	public void addAirports(String apCodes) {
		StringTokenizer tkns = new StringTokenizer(apCodes, "!");
		while (tkns.hasMoreTokens())
			_apCodes.add(tkns.nextToken().toUpperCase());
	}

	public void setAirports(Airport aD, Airport aA, String stopCodes) {
		_apCodes.clear();
		addAirports(aD.getIATA());
		addAirports(stopCodes);
		addAirports(aA.getIATA());
	}

	public void addEntry(ScheduleEntry se) {
		_entries.add(se);
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

	public Collection<ScheduleEntry> getEntries() {

		// Do no processing if only one leg
		if (_entries.size() == 1)
			return new HashSet<ScheduleEntry>(_entries);
		else if (_entries.size() == 0)
			return Collections.emptySet();

		// Order the departure airports
		List<String> apCodes = getDepartsFrom();

		// Build the results
		List<ScheduleEntry> results = new ArrayList<ScheduleEntry>(_entries.size());
		for (int x = 0; x < _entries.size(); x++)
			results.add(null);

		// Order the legs
		for (Iterator<ScheduleEntry> i = _entries.iterator(); i.hasNext();) {
			ScheduleEntry e = i.next();
			int ofs = apCodes.indexOf(e.getAirportD().getIATA());
			if (ofs != -1) {
				e.setLeg(ofs + 1);
				results.set(ofs, e);
			}
		}

		return results;
	}
}