// Copyright 2006, 2007, 2008, 2009, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file.innovata;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.ScheduleLoadDAO;

import org.deltava.util.CollectionUtils;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load CSV-format flight schedules from Innovata LLC.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GetFullSchedule extends ScheduleLoadDAO {

	private static final Logger log = Logger.getLogger(GetFullSchedule.class);
	
	private final DateTimeFormatter _df = new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").parseDefaulting(ChronoField.SECOND_OF_DAY, 0).toFormatter();
	private DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("H:mm:ss").toFormatter();

	private static final List<String> GROUND_EQ = Arrays.asList("TRN", "BUS", "LMO", "RFS");

	private LocalDateTime _effDate = LocalDateTime.now();

	private final Collection<CSVTokens> _data = new ArrayList<CSVTokens>();
	private final Collection<String> _aCodes = new HashSet<String>();
	private final Collection<String> _mlCodes = new HashSet<String>();
	private final Collection<String> _csCodes = new HashSet<String>();

	/**
	 * Initializes the Data Access Object.
	 * @param is the input stream to read
	 */
	public GetFullSchedule(InputStream is) {
		super(is);
	}

	/**
	 * Initializes the list of airlines.
	 * @param airlines a Collection of Airline beans
	 */
	@Override
	public void setAirlines(Collection<Airline> airlines) {
		super.setAirlines(airlines);
		for (Iterator<Airline> i = airlines.iterator(); i.hasNext();) {
			Airline a = i.next();
			_aCodes.addAll(a.getCodes());
		}
	}

	/**
	 * Sets main line airline codes. These will automatically be included, and by default code shares
	 * of flights with these codes will be included.
	 * @param codes the main line airline codes
	 * @see GetFullSchedule#setCodeshareCodes(Collection)
	 */
	public void setMainlineCodes(Collection<String> codes) {
		_mlCodes.clear();
		_mlCodes.addAll(codes);
		_csCodes.clear();
		_csCodes.addAll(codes);
	}
	
	/**
	 * Sets code share airline codes. Flights with a code share code including one or more of these
	 * airlines will be included.
	 * @param codes the code share airline codes
	 * @see GetFullSchedule#setMainlineCodes(Collection)
	 */
	public void setCodeshareCodes(Collection<String> codes) {
		if (codes != null) {
			_csCodes.clear();
			_csCodes.addAll(CollectionUtils.union(_mlCodes, codes));
		}
	}

	private boolean include(CSVTokens entries) {

		// Check the date
		try {
			LocalDateTime sd = LocalDateTime.parse(entries.get(5), _df);
			LocalDateTime ed = LocalDateTime.parse(entries.get(6), _df).plusDays(1).minusSeconds(1);
			if (_effDate.isBefore(sd) || _effDate.isAfter(ed))
				return false;
		} catch (Exception e) {
			log.warn("Invalid start/end date - " + e.getMessage());
			return false;
		}

		// Check codeshare operation
		if ("1".equals(entries.get(48)))
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
		if (!isMainLine && !_mlCodes.contains(entries.get(3))) {
			String csInfo = entries.get(50);
			for (Iterator<String> i = _csCodes.iterator(); i.hasNext() && !isCodeShare;) {
				String mlCode = i.next();
				isCodeShare |= (csInfo.indexOf(mlCode) != -1);
			}
		}

		return isMainLine || isCodeShare;
	}

	/**
	 * Sets the effective date of the schedule import. Flights starting after this date (or ending before this date) will not be loaded.
	 * @param dt the effective date/time
	 */
	public void setEffectiveDate(LocalDateTime dt) {
		if (dt != null) {
			_effDate = dt.truncatedTo(ChronoUnit.DAYS);
			DateTimeFormatterBuilder tfb = new DateTimeFormatterBuilder().appendPattern("H:mm:ss");
			tfb.parseDefaulting(ChronoField.YEAR, _effDate.getYear()).parseDefaulting(ChronoField.DAY_OF_YEAR, _effDate.getDayOfYear());
			_tf = tfb.toFormatter();
		}
	}

	/**
	 * Loads the schedule entries from the Input stream.
	 * @throws DAOException if an I/O error occurs
	 * @return a Collection of CSVTokens beans
	 */
	public Collection<CSVTokens> load() throws DAOException {
		LineNumberReader lr = getReader();
		try (LineNumberReader br = lr) {
			br.readLine(); // Skip first line
			while (br.ready()) {
				String data = br.readLine();
				CSVTokens tkns = new CSVTokens(data, br.getLineNumber());
				if (data.startsWith("//")) {
					if (log.isDebugEnabled())
						log.debug("Skipping line " + br.getLineNumber() + " - comment");
				} else if (tkns.size() < 53)
					log.warn("Skipping line " + br.getLineNumber() + " - size = " + tkns.size());
				else if (include(tkns))
					_data.add(tkns);
			}
		} catch (Exception e) {
			log.error("Error at line " + lr.getLineNumber() + " - " + e.getMessage(), e);
			throw new DAOException(e);
		}

		return _data;
	}

	/**
	 * Loads the Schedule Entries.
	 * @return a Collection of ScheduleEntry beans
	 * @throws DAOException if an I/O error occurs
	 */
	@Override
	public Collection<ScheduleEntry> process() throws DAOException {

		Map<String, DailyScheduleEntry> results = new HashMap<String, DailyScheduleEntry>();
		for (Iterator<CSVTokens> i = _data.iterator(); i.hasNext();) {
			CSVTokens entries = i.next();

			// Load the Airports
			Airport airportD = SystemData.getAirport(entries.get(14));
			Airport airportA = SystemData.getAirport(entries.get(22));
			
			// Ensure all BSL/MLH comes from a common airport.
			if ((airportD == null) && ("MLH".equals(entries.get(14))))
				airportD = SystemData.getAirport("BSL");
			if ((airportA == null) && ("MLH".equals(entries.get(22))))
				airportA = SystemData.getAirport("BSL");

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
				_invalidAL.add(entries.get(0));
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
				DailyScheduleEntry entry = new DailyScheduleEntry(a, Integer.parseInt(entries.get(1)), Integer.parseInt(entries.get(46)));
				entry.setAirportD(airportD);
				entry.setAirportA(airportA);
				entry.setEquipmentType(eqType);
				entry.setLength(Integer.parseInt(entries.get(42)) / 6);
				entry.setDays(dayBuf.toString());
				try {
					entry.setTimeD(LocalDateTime.parse(entries.get(18), _tf));
					entry.setTimeA(LocalDateTime.parse(entries.get(23), _tf));
				} catch (Exception pe) {
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