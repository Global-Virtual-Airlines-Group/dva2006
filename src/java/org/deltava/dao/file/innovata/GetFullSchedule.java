// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file.innovata;

import java.io.*;
import java.util.*;
import java.text.*;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.ScheduleLoadDAO;

import org.deltava.util.ConfigLoader;

/**
 * A Data Access Object to load CSV-format flight schedules from Innovata LLC.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetFullSchedule extends ScheduleLoadDAO {

	private static final Logger log = Logger.getLogger(GetFullSchedule.class);
	private static final DateFormat _df = new SimpleDateFormat("dd/MM/yyyy");
	private static final DateFormat _tf = new SimpleDateFormat("HH:mm");

	private long _effDate;

	private Collection<CSVTokens> _data = new ArrayList<CSVTokens>();
	private Collection<String> _aCodes = new HashSet<String>();
	private Collection<String> _mlCodes = new HashSet<String>();
	private Properties _acTypes;

	/**
	 * Initializes the Data Access Object.
	 * @param is the input stream to read
	 */
	public GetFullSchedule(InputStream is) {
		super(is);
		_effDate = System.currentTimeMillis();
	}

	/**
	 * Initializes the Data Access Object with a preloaded set of tokens.
	 * @param tokens the tokens
	 */
	public GetFullSchedule(Collection<CSVTokens> tokens) {
		this((InputStream) null);
		for (Iterator<CSVTokens> i = tokens.iterator(); i.hasNext();) {
			CSVTokens tkn = i.next();
			if (include(tkn))
				_data.add(tkn);
		}
	}

	/**
	 * Initializes the list of airlines.
	 * @param airlines a Collection of Airline beans
	 * @see ScheduleLoadDAO#setAirports(Collection)
	 */
	public void setAirlines(Collection<Airline> airlines) {
		super.setAirlines(airlines);
		for (Iterator<Airline> i = airlines.iterator(); i.hasNext();) {
			Airline a = i.next();
			_aCodes.addAll(a.getCodes());
		}
	}

	/**
	 * Sets primary airline codes.
	 * @param codes the primary airline codes
	 */
	public void setPrimaryCodes(Collection<String> codes) {
		_mlCodes.clear();
		_mlCodes.addAll(codes);
	}

	private boolean include(CSVTokens entries) {

		// Check the date
		try {
			long sd = _df.parse(entries.get(5)).getTime();
			long ed = _df.parse(entries.get(6)).getTime();
			if ((_effDate < sd) || (_effDate >= ed))
				return false;
		} catch (ParseException pe) {
			log.warn("Invalid start/end date - " + pe.getMessage());
			return false;
		}

		// Check codeshare operation
		boolean isCS = "1".equals(entries.get(48));
		if (!isCS)
			return false;

		// Check the airline
		String code = entries.get(0).toUpperCase();
		boolean isMainLine = _mlCodes.contains(code);
		boolean isCodeShare = false;
		if (_aCodes.contains(code) && !isMainLine && !_mlCodes.contains(code) && !_mlCodes.contains(entries.get(3))) {
			String csInfo = entries.get(50);
			for (Iterator<String> i = _mlCodes.iterator(); i.hasNext() && !isCodeShare;) {
				String mlCode = i.next();
				isCodeShare |= (csInfo.indexOf(mlCode) != -1);
			}
		}

		return isMainLine || isCodeShare;
	}

	/**
	 * Sets the effective date of the schedule import. Flights starting after this date (or ending before this date)
	 * will not be loaded.
	 * @param dt the effective date/time
	 */
	public void setEffectiveDate(Date dt) {
		if (dt != null)
			_effDate = dt.getTime();
	}

	/**
	 * Initializes the aircraft codes.
	 * @throws DAOException if an I/O error occurs
	 */
	public void initAircraft() throws DAOException {

		// Load aircraft types from disk
		_acTypes = new Properties();
		try {
			_acTypes.load(ConfigLoader.getStream("/etc/iata_aircraft.properties"));
		} catch (IOException ie) {
			DAOException de = new DAOException("Cannot load IATA aircraft codes!");
			de.setLogStackDump(false);
			throw de;
		}
	}

	/**
	 * Loads the schedule entries from the Input stream.
	 * @throws DAOException if an I/O error occurs
	 * @return a Collection of CSVTokens beans
	 */
	public Collection<CSVTokens> load() throws DAOException {
		initAircraft();
		LineNumberReader br = null;
		try {
			br = new LineNumberReader(getReader());

			while (br.ready()) {
				String data = br.readLine();
				CSVTokens tkns = new CSVTokens(data, br.getLineNumber());
				if (data.startsWith("//"))
					log.debug("Skipping line " + br.getLineNumber() + " - comment");
				else if (tkns.size() < 53)
					log.warn("Skipping line " + br.getLineNumber() + " - size = " + tkns.size());
				else if (include(tkns))
					_data.add(tkns);
			}

			br.close();
		} catch (IOException ie) {
			log.error("Error at line " + br.getLineNumber() + " - " + ie.getMessage(), ie);
			throw new DAOException(ie);
		}

		return _data;
	}

	/**
	 * Loads the Schedule Entries.
	 * @return a Collection of ScheduleEntry beans
	 * @throws DAOException if an I/O error occurs
	 */
	public Collection<ScheduleEntry> process() throws DAOException {

		Map<String, DailyScheduleEntry> results = new HashMap<String, DailyScheduleEntry>();
		for (Iterator<CSVTokens> i = _data.iterator(); i.hasNext();) {
			CSVTokens entries = i.next();

			// Load the Airports
			Airport airportD = _airports.get(entries.get(14).toUpperCase());
			Airport airportA = _airports.get(entries.get(22).toUpperCase());

			// Look up the equipment type
			String eqType = _acTypes.getProperty(entries.get(27));

			// Validate the data
			Airline a = _airlines.get(entries.get(0));
			if (eqType == null) {
				log.warn("Unknown equipment code at Line " + entries.getLineNumber() + " - " + entries.get(27));
				_errors.add("Unknown equipment code at Line " + entries.getLineNumber() + " - " + entries.get(27));
			} else if (airportD == null) {
				log.warn("Unknown Airport at Line " + entries.getLineNumber() + " - " + entries.get(14));
				_errors.add("Unknown Airport at Line " + entries.getLineNumber() + " - " + entries.get(14));
			} else if (airportA == null) {
				log.warn("Unknown Airport at Line " + entries.getLineNumber() + " - " + entries.get(22));
				_errors.add("Unknown Airport at Line " + entries.getLineNumber() + " - " + entries.get(22));
			}

			// Count the number of days this leg operates
			StringBuilder dayBuf = new StringBuilder();
			for (int x = 7; x < 14; x++) {
				if ("1".equals(entries.get(x)))
					dayBuf.append(String.valueOf(x - 6));
			}

			// Build the Schedule Entry
			DailyScheduleEntry entry = new DailyScheduleEntry(a, Integer.parseInt(entries.get(1)), Integer
					.parseInt(entries.get(46)));
			entry.setAirportD(airportD);
			entry.setAirportA(airportA);
			entry.setEquipmentType(eqType);
			entry.setLength(Integer.parseInt(entries.get(42)) / 6);
			entry.setDays(dayBuf.toString());
			try {
				entry.setTimeD(_tf.parse(entries.get(18)));
				entry.setTimeA(_tf.parse(entries.get(23)));
			} catch (ParseException pe) {
				log.warn("Error parsing time - " + pe.getMessage());
				_errors.add("Error parsing time - " + pe.getMessage());
			}
			
			// Check if we have an entry
			DailyScheduleEntry e2 = results.get(entry.toString());
			if ((e2 == null) || (e2.getDays() < entry.getDays()))
				results.put(entry.toString(), entry);
		}

		return new TreeSet<ScheduleEntry>(results.values());
	}
}