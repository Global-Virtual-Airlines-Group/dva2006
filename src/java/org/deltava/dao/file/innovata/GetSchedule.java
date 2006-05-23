// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file.innovata;

import java.io.*;
import java.util.*;
import java.text.*;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.*;

import org.deltava.util.*;

/**
 * A Data Access Object to load CSV-format schedules from Innovata LLC.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetSchedule extends ScheduleLoadDAO {

	private static final Logger log = Logger.getLogger(GetSchedule.class);
	private static final DateFormat _df = new SimpleDateFormat("dd-MMM-yyyy");
	private static final DateFormat _tf = new SimpleDateFormat("HHmm");
	private static final DateFormat _ftf = new SimpleDateFormat("HH:mm");

	private Calendar _effDate;
	private Collection<CSVTokens> _data = new TreeSet<CSVTokens>();
	
	// Innovata Equipment Types
	private static final String[] IV_EQTYPES = {"310", "319", "320", "321", "332", "333", "340", "343", "346",
		"732", "733", "734", "735", "737", "738", "739", "744", "747", "752",
		"753", "757", "762", "763", "764", "767", "772", "777", "77W", "ARJ",
		"AT5", "AT7", "CR2", "CR7", "CRJ", "D93", "D95", "E70", "EM2", "ER3", "ER4",
		"ERJ", "M80", "M87", "M88", "M90", "SF3" };
	
	// Our equipment types
	private static final String[] EQTYPES = {"A310", "A319", "A320", "A321", "A330-200", "A330-300", "A340-300", "A340-300", "A346-600",
		"B737-200", "B737-300", "B737-400", "B737-500", "B737-700", "B737-800", "B737-900", "B747-400", "B747-200", "B757-200",
		"B757-300", "B757-200", "B767-200", "B767-300", "B767-400", "B767-400", "B777-200", "B777-200", "B777-300", "BAE-146",
		"ATR-72", "ATR-72", "CRJ-200", "CRJ-700", "CRJ-200", "DC-9-32", "DC-9-50", "ERJ-170", "EMB-120", "ERJ-135", "ERJ-145",
		"ERJ-145", "MD-88", "MD-88", "MD-88", "MD-90", "SF340"};

	/**
	 * Initializes the Data Access Object.
	 * @param is the input stream to read
	 */
	public GetSchedule(InputStream is) {
		super(is);
		_effDate = CalendarUtils.getInstance(null);
	}

	/**
	 * Initializes the Data Access Object with a preloaded set of tokens.
	 * @param tokens the tokens
	 */
	public GetSchedule(Collection<CSVTokens> tokens) {
		super(null);
		_data.addAll(tokens);
	}

	/**
	 * Sets the effective date of the schedule import. Flights starting after this date (or ending before this date)
	 * will not be loaded.
	 * @param dt the effective date/time
	 */
	public void setEffectiveDate(Date dt) {
		_effDate.setTime(dt);
	}

	/**
	 * Helper method to load an airport bean.
	 */
	private Airport getAirport(String code, int line) {
		Airport a = _airports.get(code.toUpperCase());
		if (a == null) {
			log.warn("Unknown Airport at Line " + line + " - " + code);
			_errors.add("Unknown Airport at Line " + line + " - " + code);
		}

		return a;
	}
	
	/**
	 * Loads the schedule entries from the Input stream.
	 * @throws DAOException if an I/O error occurs
	 * @return a Collection of CSVTokens beans
	 */
	public Collection<CSVTokens> load() throws DAOException {
		LineNumberReader br = null;
		try {
			br = new LineNumberReader(getReader());
			
			// Skip the first line
			if (br.ready())
				br.readLine();
				
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
		
		return _data;
	}

	/**
	 * Processes the Schedule entries from the collection.
	 * @return a Collection of ScheduleEntry beans
	 */
	public Collection<ScheduleEntry> process() {
		Map<String, MultiLegInfo> legs = new LinkedHashMap<String, MultiLegInfo>();
		String year = "-" + String.valueOf(_effDate.get(Calendar.YEAR));

		// Parse the loaded data
		for (Iterator<CSVTokens> i = _data.iterator(); i.hasNext();) {
			CSVTokens tkns = i.next();
			String flightCode = tkns.get(7);
			UserID flightID = new UserID(flightCode);
			int eqOfs = StringUtils.arrayIndexOf(IV_EQTYPES, tkns.get(8));

			// Load the Airports
			Airport airportD = getAirport(tkns.get(3), tkns.getLineNumber());
			Airport airportA = getAirport(tkns.get(5), tkns.getLineNumber());

			// Get the effective dates
			boolean includeFlight = true;
			try {
				long eff = _effDate.getTimeInMillis();
				Date startDate = StringUtils.isEmpty(tkns.get(0)) ? new Date(eff - 1) : _df.parse(tkns.get(0) + year);
				Date endDate = StringUtils.isEmpty(tkns.get(1)) ? new Date(eff + 1) : _df.parse(tkns.get(1) + year);
				includeFlight = ((startDate.getTime() <= eff) && (endDate.getTime() >= eff));
			} catch (ParseException pe) {
				log.warn("Unknown start/end date - " + pe.getMessage());
				_errors.add("Unknown start/end date - " + pe.getMessage());
				includeFlight = false;
			}

			// Load the airline
			Airline a = _airlines.get(flightID.getAirlineCode());
			if (a == null) {
				log.warn("Unknown Airline " + flightID.getAirlineCode());
				_errors.add("Unknown Airline " + flightID.getAirlineCode());
			} else if (eqOfs == -1) {
				log.warn("Unknown equipment code at Line " + tkns.getLineNumber() + " - " + tkns.get(8));
				_errors.add("Unknown equipment code at Line " + tkns.getLineNumber() + " - " + tkns.get(8));
			} else if (flightID.getUserID() >= 9000)
				log.debug("Skipping charter " + flightID);
			else if ((airportD == null) || (airportA == null))
				log.debug("Invalid Airport(s)");
			else if (!includeFlight)
				log.debug("Skipping flight (NOT EFFECTIVE) " + flightID);
			else {
				// Build the leg information
				DailyScheduleEntry se = new DailyScheduleEntry(a, flightID.getUserID(), 1);
				se.setAirportD(airportD);
				se.setAirportA(airportA);
				se.setEquipmentType(EQTYPES[eqOfs]);
				se.setDays(tkns.get(2));
				try {
					se.setTimeD(_tf.parse(tkns.get(4)));
					String timeA = tkns.get(6);
					if (timeA.endsWith("+1"))
						timeA = timeA.substring(0, timeA.indexOf('+'));

					se.setTimeA(_tf.parse(timeA));
					Calendar ft = CalendarUtils.getInstance(_ftf.parse(tkns.get(12)));
					se.setLength((ft.get(Calendar.HOUR) * 10) + (ft.get(Calendar.MINUTE) / 6));
				} catch (ParseException pe) {
					log.warn("Error parsing time - " + pe.getMessage());
					_errors.add("Error parsing time - " + pe.getMessage());
				}
				
				// Get the multi-leg info
				int stops = Integer.parseInt(tkns.get(10));
				boolean isExistML = legs.containsKey(flightCode);
				MultiLegInfo ml = isExistML ? legs.get(flightCode) : new MultiLegInfo(flightCode, flightID.getUserID());
				if (stops == 0) {
					// Check to see if we already have a leg
					DailyScheduleEntry ee = ml.getEntry(airportD, airportA);
					if ((ee != null) && (ee.getDays() < se.getDays())) {
						log.debug("Replacing " + flightCode + " Leg " + (ml.getLegs() + 1));
						ml.replaceEntry(se);
					} else if (ee == null) {
						log.debug("Loading " + flightCode + " Leg " + (ml.getLegs() + 1));
						ml.addEntry(se);
						
						// Load airports only if we haven't had a multi-leg info entry yet
						if (!ml.isAirportListLoaded()) {
							ml.addAirport(airportD.getIATA());
							ml.addAirport(airportA.getIATA());
						}
					}
				} else if (stops < (ml.getDepartsFrom().size() - 1)) {
					log.debug("Intermediate stage in " + flightID);
				} else {
					if ((!isExistML) || (ml.getMainEntry().getDays() < se.getDays())) {
						log.debug("Loading multi-stage info for " + flightID);
						if (se.getLength() > 12)
							se.setLength(se.getLength() - 5);
						
						ml.setAirports(airportD, airportA, tkns.get(11));
						ml.setMainEntry(se);
					}
				}

				// add to the map
				if (!isExistML)
					legs.put(flightCode, ml);
			}
		}

		// Go through the MultiLegInfo beans
		int invalidFlights = 0;
		Collection<ScheduleEntry> results = new ArrayList<ScheduleEntry>();
		for (Iterator<MultiLegInfo> i = new TreeSet<MultiLegInfo>(legs.values()).iterator(); i.hasNext();) {
			MultiLegInfo inf = i.next();
			if (inf.getEntries().isEmpty()) {
				log.warn(inf.getFlightCode() + " empty");
				invalidFlights++;
			} else if (inf.getEntries().size() > (inf.getDepartsFrom().size())) {
				log.warn(inf.getFlightCode() + " stops=" + inf.getDepartsFrom() + ", entries=" + inf.getEntries().size());
				invalidFlights++;
			} else {
				// Trim out nulls
				Collection<ScheduleEntry> entries = inf.getEntries();
				for (Iterator<ScheduleEntry> ei = entries.iterator(); ei.hasNext(); ) {
					ScheduleEntry se = ei.next();
					if (se == null)
						ei.remove();
				}
				
				results.addAll(entries);
			}
		}
		
		// Update the codeshare airlines
		updateCodeshares(results);
		return results;
	}
}