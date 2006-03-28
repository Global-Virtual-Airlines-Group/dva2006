// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.text.*;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.*;

/**
 * A Data Access Object to load CSV-format schedules.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetSchedule extends DAO {

	private static final Logger log = Logger.getLogger(GetSchedule.class);
	private static final DateFormat _df = new SimpleDateFormat("HHmm");

	private Map<String, Airline> _airlines;
	private Map<String, Airport> _airports;
	private Collection<CSVTokens> _data = new TreeSet<CSVTokens>(); 

	private class CSVTokens implements Comparable {

		private int _lineNumber;
		private List<String> _tkns = new ArrayList<String>();

		CSVTokens(String entry, int line) {
			super();
			_lineNumber = line;
			
			// Loop through the string
			int x = 0;
			while (x < entry.length()) {
				// Parse numeric or string
				if (entry.charAt(x) == '\"') {
					int ofs = entry.indexOf('\"', x + 1);
					if (ofs == -1) {
						_tkns.add(entry.substring(x + 1));
						x = entry.length();
					} else {
						_tkns.add(entry.substring(x + 1, ofs));
						x = ofs + 2;
					}
				} else {
					int ofs = entry.indexOf(',', x + 1);
					if (ofs == -1) {
						_tkns.add(entry.substring(x + 1));
						x = entry.length();
					} else {
						_tkns.add(entry.substring(x, ofs));
						x = ofs + 1;
					}
				}
			}
		}
		
		public int getLineNumber() {
			return _lineNumber;
		}

		public String get(int ofs) {
			return _tkns.get(ofs);
		}

		public List<String> getAll() {
			return _tkns;
		}

		public int size() {
			return _tkns.size();
		}
		
		public int compareTo(Object o2) {
			CSVTokens t2 = (CSVTokens) o2;
			int tmpResult = _tkns.get(7).compareTo(t2.get(7));
			if (tmpResult == 0)
				tmpResult = (_tkns.get(10).compareTo(t2.get(10)) * -1);
			
			return (tmpResult == 0) ? _tkns.get(4).compareTo(t2.get(4)) : tmpResult; 
		}
	}

	private class MultiLegInfo implements Comparable {

		private String _flightCode;
		private int _flightNumber;
		private Collection<String> _apCodes = new LinkedHashSet<String>();
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

		public void addAirports(String apCodes) {
			if (apCodes.indexOf('!') != -1) {
				StringTokenizer tkns = new StringTokenizer(apCodes, "!");
				while (tkns.hasMoreTokens())
					_apCodes.add(tkns.nextToken().toUpperCase());
			} else {
				_apCodes.add(apCodes.toUpperCase());
			}
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

	/**
	 * Initializes the Data Access Object.
	 * @param is the input stream to read
	 * @param airlines a Collection of Airline beans
	 * @param airports a Collection of Airport beans
	 */
	public GetSchedule(InputStream is, Collection<Airline> airlines, Collection<Airport> airports) {
		super(is);
		_airlines = CollectionUtils.createMap(airlines, "code");
		_airports = CollectionUtils.createMap(airports, "IATA");
	}
	
	/**
	 * Helper method to load an airport bean.
	 */
	private Airport getAirport(String code, int line) {
		Airport a = _airports.get(code.toUpperCase());
		if (a == null) {
			log.warn("Unknown Airport at Line " + line + " - " + code);
			a = new Airport(code, code, "Unknown - " + code);
		}
		
		return a;
	}
	
	/**
	 * Loads the schedule entries from the Input stream.
	 * @throws DAOException if an I/O error occurs
	 */
	public void load() throws DAOException {
		LineNumberReader br = null;
		try {
			br = new LineNumberReader(getReader());
			while (br.ready()) {
				String data = br.readLine();
				CSVTokens tkns = new CSVTokens(data, br.getLineNumber());
				if (data.startsWith("//")) {
					log.debug("Skipping line " + br.getLineNumber() + " - comment");
				} else if (tkns.size() != 14) {
					if (tkns.size() > 2)
						log.warn("Skipping line " + br.getLineNumber() + " - size = " + tkns.size());
				} else {
					_data.add(tkns);
				}
			}
			
			br.close();
		} catch (IOException ie) {
			log.error("Error at line " + br.getLineNumber() + " - " + ie.getMessage(), ie);
			throw new DAOException(ie);
		}
	}

	/**
	 * Processes the Schedule entries from the collection.
	 * @return a Collection of ScheduleEntry beans
	 */
	public Collection<ScheduleEntry> process() throws IOException {
		Map<String, MultiLegInfo> legs = new LinkedHashMap<String, MultiLegInfo>();
		PrintWriter pw = new PrintWriter("c:\\temp\\schedule.csv");

		// Parse the loaded data
		for (Iterator<CSVTokens> i = _data.iterator(); i.hasNext(); ) {
			CSVTokens tkns = i.next();
			String flightCode = tkns.get(7);
			UserID flightID = new UserID(flightCode);
			
			List<String> info = tkns.getAll();
			pw.println(info.subList(3, info.size()));
			
			// Load the Airports
			Airport airportD = getAirport(tkns.get(3), tkns.getLineNumber());
			Airport airportA = getAirport(tkns.get(5), tkns.getLineNumber());

			// Load the airline
			Airline a = _airlines.get(flightID.getAirlineCode());
			if (a == null)
				log.warn("Unknown Airline " + flightCode);
			else if (flightID.getUserID() >= 9600)
				log.warn("Skipping charter " + flightID);
			else {
				// Build the leg information
				ScheduleEntry se = new ScheduleEntry(a, flightID.getUserID(), 1);
				se.setAirportD(airportD);
				se.setAirportA(airportA);
				se.setEquipmentType(tkns.get(8));
				try {
					se.setTimeD(_df.parse(tkns.get(4)));
					String timeA = tkns.get(6);
					if (timeA.endsWith("+1"))
						timeA = timeA.substring(0, timeA.indexOf('+'));
					
					se.setTimeA(_df.parse(timeA));
				} catch (ParseException pe) {
					log.warn("Error parsing time - " + pe.getMessage());
				}

				// Get the multi-leg info
				int stops = Integer.parseInt(tkns.get(10));
				MultiLegInfo ml = legs.containsKey(flightCode) ? legs.get(flightCode) : 
					new MultiLegInfo(flightCode, flightID.getUserID());
				if (stops == 0) {
					log.info("Loading " + flightCode + " Leg " + (ml.getLegs() + 1));
					ml.addAirports(airportD.getIATA());
					ml.addAirports(airportA.getIATA());
					ml.addEntry(se);
				} else if (stops < (ml.getDepartsFrom().size() - 1)) {
					log.info("Intermediate stage in " + flightID);
				} else {
					log.info("Loading multi-stage info for " + flightID);
					ml.setAirports(airportD, airportA, tkns.get(11));
				}
				
				// add to the map
				if (!legs.containsKey(flightCode))
					legs.put(flightCode, ml);
			}
		}
		
		pw.close();

		// Go through the MultiLegInfo beans
		Collection<ScheduleEntry> results = new ArrayList<ScheduleEntry>();
		for (Iterator<MultiLegInfo> i = new TreeSet<MultiLegInfo>(legs.values()).iterator(); i.hasNext();) {
			MultiLegInfo inf = i.next();
			if (inf.getLegs() != (inf.getDepartsFrom().size()))
				log.warn(inf.getFlightCode() + " stops=" + inf.getDepartsFrom() + ", entries=" + inf.getLegs());
			else
				results.addAll(inf.getEntries());
		}

		return results;
	}
}