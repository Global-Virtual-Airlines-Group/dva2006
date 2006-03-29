// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file.innovata;

import java.io.*;
import java.util.*;
import java.text.*;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.DAO;

import org.deltava.util.*;

/**
 * A Data Access Object to load CSV-format schedules from Innovata LLC.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetSchedule extends DAO {

	private static final Logger log = Logger.getLogger(GetSchedule.class);
	private static final DateFormat _df = new SimpleDateFormat("dd-MMM-yyyy");
	private static final DateFormat _tf = new SimpleDateFormat("HHmm");

	private Calendar _effDate;
	private Map<String, Airline> _airlines;
	private Map<String, Airport> _airports;
	private Collection<CSVTokens> _data = new TreeSet<CSVTokens>();

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
		_effDate = CalendarUtils.getInstance(null);
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
			a = new Airport(code, code, "Unknown - " + code);
		}

		return a;
	}

	public int getDaysOfWeek(String dayCodes) {
		int days = 0;
		for (int x = 0; x < dayCodes.length(); x++) {
			char c = dayCodes.charAt(x);
			if (!Character.isSpaceChar(c))
				days++;
		}

		return days;
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
		String year = "-" + String.valueOf(_effDate.get(Calendar.YEAR));

		// Parse the loaded data
		for (Iterator<CSVTokens> i = _data.iterator(); i.hasNext();) {
			CSVTokens tkns = i.next();
			String flightCode = tkns.get(7);
			UserID flightID = new UserID(flightCode);

			// List<String> info = tkns.getAll();
			pw.println(tkns.getAll());

			// Load the Airports
			Airport airportD = getAirport(tkns.get(3), tkns.getLineNumber());
			Airport airportA = getAirport(tkns.get(5), tkns.getLineNumber());

			// Get the effective dates
			boolean includeFlight = true;
			try {
				long eff = _effDate.getTimeInMillis();
				Date startDate = StringUtils.isEmpty(tkns.get(0)) ? new Date(eff) : _df.parse(tkns.get(0) + year);
				Date endDate = StringUtils.isEmpty(tkns.get(1)) ? new Date(eff + 1000) : _df.parse(tkns.get(1) + year);
				includeFlight = (startDate.getTime() <= eff) && (endDate.getTime() > eff);
			} catch (ParseException pe) {
				log.warn("Unknown start/end date - " + pe.getMessage());
				includeFlight = false;
			}

			// Load the airline
			Airline a = _airlines.get(flightID.getAirlineCode());
			if (a == null)
				log.warn("Unknown Airline " + flightCode);
			else if (flightID.getUserID() >= 9600)
				log.warn("Skipping charter " + flightID);
			else if (!includeFlight)
				log.warn("Skipping flight (NOT EFFECTIVE) " + flightID);
			else {
				// Build the leg information
				ScheduleEntry se = new ScheduleEntry(a, flightID.getUserID(), 1);
				se.setAirportD(airportD);
				se.setAirportA(airportA);
				se.setEquipmentType(tkns.get(8));
				try {
					se.setTimeD(_tf.parse(tkns.get(4)));
					String timeA = tkns.get(6);
					if (timeA.endsWith("+1"))
						timeA = timeA.substring(0, timeA.indexOf('+'));

					se.setTimeA(_tf.parse(timeA));
				} catch (ParseException pe) {
					log.warn("Error parsing time - " + pe.getMessage());
				}

				// Get the multi-leg info
				int stops = Integer.parseInt(tkns.get(10));
				boolean isExistML = legs.containsKey(flightCode);
				MultiLegInfo ml = isExistML ? legs.get(flightCode) : new MultiLegInfo(flightCode, flightID.getUserID());
				if (stops == 0) {
					log.info("Loading " + flightCode + " Leg " + (ml.getLegs() + 1));
					ml.addEntry(se);
					ml.addAirport(airportD.getIATA());
					ml.addAirport(airportA.getIATA());
				} else if (stops < (ml.getDepartsFrom().size() - 1)) {
					log.info("Intermediate stage in " + flightID);
				} else {
					log.info("Loading multi-stage info for " + flightID);
					ml.setAirports(airportD, airportA, tkns.get(11));
				}

				// add to the map
				if (!isExistML)
					legs.put(flightCode, ml);
			}
		}

		pw.close();

		// Go through the MultiLegInfo beans
		Collection<ScheduleEntry> results = new ArrayList<ScheduleEntry>();
		for (Iterator<MultiLegInfo> i = new TreeSet<MultiLegInfo>(legs.values()).iterator(); i.hasNext();) {
			MultiLegInfo inf = i.next();
			if (inf.getEntries().isEmpty())
				log.debug("empty");
			else if (inf.getLegs() != (inf.getDepartsFrom().size()))
				log.warn(inf.getFlightCode() + " stops=" + inf.getDepartsFrom() + ", entries=" + inf.getLegs());
			else
				results.addAll(inf.getEntries());
		}

		return results;
	}}