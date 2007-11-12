// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file.innovata;

import java.io.*;
import java.util.*;
import java.text.*;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.ScheduleLoadDAO;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load CSV-format flight schedules from Innovata LLC.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetFullSchedule extends ScheduleLoadDAO {

	private static final Logger log = Logger.getLogger(GetFullSchedule.class);
	private static final DateFormat _df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	private static final DateFormat _tf = new SimpleDateFormat("HH:mm");

	private static final List<String> GROUND_EQ = Arrays.asList("TRN", "BUS", "LMO", "RFS");

	private long _effDate;

	private final Collection<CSVTokens> _data = new ArrayList<CSVTokens>();
	private final Collection<String> _aCodes = new HashSet<String>();
	private final Collection<String> _mlCodes = new HashSet<String>();

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
			long sd = _df.parse(entries.get(5) + " 00:00").getTime();
			long ed = _df.parse(entries.get(6) + " 23:59").getTime();
			if ((_effDate < sd) || (_effDate >= ed))
				return false;
		} catch (ParseException pe) {
			log.warn("Invalid start/end date - " + pe.getMessage());
			return false;
		}

		// Check codeshare operation
		boolean isCS = "1".equals(entries.get(48));
		if (isCS)
			return false;

		// Check for Train/Bus
		if (GROUND_EQ.contains(entries.get(27)))
			return false;
		
		// Check for multiple legs
		if (!"0".equals(entries.get(36)))
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
	 * Loads the schedule entries from the Input stream.
	 * @throws DAOException if an I/O error occurs
	 * @return a Collection of CSVTokens beans
	 */
	public Collection<CSVTokens> load() throws DAOException {
		LineNumberReader br = null;
		try {
			br = new LineNumberReader(getReader());
			br.readLine(); // Skip first line

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
			Airport airportD = SystemData.getAirport(entries.get(14));
			Airport airportA = SystemData.getAirport(entries.get(22));

			// Look up the equipment type
			String eqType = getEquipmentType(entries.get(27));

			// Validate the data
			boolean isOK = true;
			Airline a = SystemData.getAirline(entries.get(0));
			String flightCode = entries.get(0) + entries.get(1);
			if (eqType == null) {
				isOK = false;
				_invalidEQ.add(entries.get(27));
				log.warn("Unknown equipment code at Line " + entries.getLineNumber() + " - " + entries.get(27) + " (" + flightCode + ")");
				_errors.add("Unknown equipment code at Line " + entries.getLineNumber() + " - " + entries.get(27));
			} else if (airportD == null) {
				isOK = false;
				_invalidAP.add(entries.get(14));
				log.warn("Unknown Airport at Line " + entries.getLineNumber() + " - " + entries.get(14) + " (" + flightCode + ")");
				_errors.add("Unknown Airport at Line " + entries.getLineNumber() + " - " + entries.get(14));
			} else if (airportA == null) {
				isOK = false;
				_invalidAP.add(entries.get(22));
				log.warn("Unknown Airport at Line " + entries.getLineNumber() + " - " + entries.get(22) + " (" + flightCode + ")");
				_errors.add("Unknown Airport at Line " + entries.getLineNumber() + " - " + entries.get(22));
			} else if (a == null) {
				isOK = false;
				log.warn("Unknown airline at Line " + entries.getLineNumber() + " - " + entries.get(0) + " (" + flightCode + ")");
				_errors.add("Unknown airline at Line " + entries.getLineNumber() + " - " + entries.get(0));
			} else if (!a.getApplications().contains(SystemData.get("airline.code"))) {
				isOK = false;
				log.info("Disabled airline at Line " + entries.getLineNumber() + " - " + entries.get(0) + " (" + flightCode + ")");
			} else if (airportD.getPosition().distanceTo(airportA) < 5) {
				isOK = false;
				log.info("Dummy flight from " + airportD.getIATA() + " to " + airportA.getIATA());
			}

			// Count the number of days this leg operates
			StringBuilder dayBuf = new StringBuilder();
			for (int x = 7; x < 14; x++) {
				if ("1".equals(entries.get(x)))
					dayBuf.append(String.valueOf(x - 6));
			}

			// Build the Schedule Entry
			if (isOK) {
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
				if ((e2 == null) || (e2.getDays() < entry.getDays())) {
					validateAirports(entry);
					results.put(entry.toString(), entry);
				}
			}
		}

		return new TreeSet<ScheduleEntry>(results.values());
	}
}