// Copyright 2006, 2007, 2008, 2009, 2012, 2016, 2019, 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load CSV-format flight schedules from Innovata LLC.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class GetFullSchedule extends ScheduleLoadDAO {

	private static final Logger log = LogManager.getLogger(GetFullSchedule.class);
	
	private final DateTimeFormatter _df = new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").parseDefaulting(ChronoField.SECOND_OF_DAY, 0).toFormatter();
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("HH:mm:ss").toFormatter();

	private final Collection<String> _aCodes = new HashSet<String>();
	private final Collection<String> _mlCodes = new HashSet<String>();
	private final Collection<String> _csCodes = new HashSet<String>();

	/**
	 * Initializes the Data Access Object.
	 * @param is the input stream to read
	 */
	public GetFullSchedule(InputStream is) {
		super(ScheduleSource.INNOVATA, is);
	}

	/**
	 * Initializes the list of airlines.
	 * @param airlines a Collection of Airline beans
	 */
	@Override
	public void setAirlines(Collection<Airline> airlines) {
		super.setAirlines(airlines);
		airlines.forEach(a -> _aCodes.addAll(a.getCodes()));
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
		if (_mlCodes.contains(code))
			return true;
		
		boolean isCodeShare = false;
		if (!_mlCodes.contains(entries.get(3))) {
			String csInfo = entries.get(50);
			for (Iterator<String> i = _csCodes.iterator(); i.hasNext() && !isCodeShare;) {
				String mlCode = i.next();
				isCodeShare |= (csInfo.indexOf(mlCode) != -1);
			}
		}

		return isCodeShare;
	}

	/**
	 * Loads the Schedule Entries.
	 * @return a Collection of RawScheduleEntry beans
	 * @throws DAOException if an I/O error occurs
	 */
	@Override
	public Collection<RawScheduleEntry> process() throws DAOException {
		Collection<RawScheduleEntry> results = new ArrayList<RawScheduleEntry>();
		LineNumberReader lr = getReader();
		try (LineNumberReader br = lr) {
			br.readLine(); // Skip first line
			while (br.ready()) {
				String data = br.readLine();
				CSVTokens tkns = StringUtils.parseCSV(data);
				if (data.startsWith("//") && log.isDebugEnabled())
					log.debug("Skipping line " + br.getLineNumber() + " - comment");
				else if (tkns.size() < 53)
					log.warn("Skipping line " + br.getLineNumber() + " - size = " + tkns.size());
				else if (include(tkns)) {
					RawScheduleEntry se = parse(tkns);
					if (se != null) {
						se.setLineNumber(br.getLineNumber());
						results.add(se);
					}
				}
			}
		} catch (Exception e) {
			log.error("Error at line " + lr.getLineNumber() + " - " + e.getMessage(), e);
			throw new DAOException(e);
		}

		return results;
	}

	public RawScheduleEntry parse(CSVTokens entries) {

		Airport airportD = SystemData.getAirport(entries.get(14));
		Airport airportA = SystemData.getAirport(entries.get(22));
			
		// Ensure all BSL/MLH comes from a common airport.
		if ((airportD == null) && ("MLH".equals(entries.get(14))))
			airportD = SystemData.getAirport("BSL");
		if ((airportA == null) && ("MLH".equals(entries.get(22))))
			airportA = SystemData.getAirport("BSL");

		// Look up the equipment type
		String eqType = getEquipmentType(entries.get(27));
		String ln = entries.get(entries.size() - 1);

		// Validate the data
		boolean isOK = true;
		Airline a = SystemData.getAirline(entries.get(0));
		String flightCode = entries.get(0) + entries.get(1);
		if (eqType == null) {
			isOK = false;
			_status.addInvalidEquipment(entries.get(27));
			log.warn("Unknown equipment code at Line " + ln + " - " + entries.get(27) + " (" + flightCode + ")");
			_status.addMessage("Unknown equipment code at Line " + ln + " - " + entries.get(27));
		} else if (airportD == null) {
			isOK = false;
			_status.addInvalidAirport(entries.get(14));
			log.warn("Unknown Airport at Line " + ln + " - " + entries.get(14) + " (" + flightCode + ")");
			_status.addMessage("Unknown Airport at Line " + ln + " - " + entries.get(14));
		} else if (airportA == null) {
			isOK = false;
			_status.addInvalidAirport(entries.get(22));
			log.warn("Unknown Airport at Line " + ln + " - " + entries.get(22) + " (" + flightCode + ")");
			_status.addMessage("Unknown Airport at Line " + ln + " - " + entries.get(22));
		} else if (a == null) {
			isOK = false;
			_status.addInvalidAirline(entries.get(0));
			log.warn("Unknown airline at Line " + ln + " - " + entries.get(0) + " (" + flightCode + ")");
			_status.addMessage("Unknown airline at Line " + ln + " - " + entries.get(0));
		} else if (!a.getApplications().contains(SystemData.get("airline.code"))) {
			isOK = false;
			log.info("Disabled airline at Line " + ln + " - " + entries.get(0) + " (" + flightCode + ")");
		} else if (airportD.getPosition().distanceTo(airportA) < 5) {
			isOK = false;
			log.info("Dummy flight from " + airportD.getIATA() + " to " + airportA.getIATA());
		}

		if (!isOK) return null;

		// Build the Schedule Entry
		RawScheduleEntry entry = new RawScheduleEntry(a, Integer.parseInt(entries.get(1)), Integer.parseInt(entries.get(46)));
		entry.setSource(ScheduleSource.INNOVATA);
		entry.setAirportD(airportD);
		entry.setAirportA(airportA);
		entry.setEquipmentType(eqType);
		entry.setLength(Integer.parseInt(entries.get(42)) / 6);
		for (int x = 7; x < 14; x++) {
			if ("1".equals(entries.get(x)))
				entry.addDayOfWeek(DayOfWeek.of(x - 6));
		}
		
		try {
			entry.setStartDate(LocalDate.parse(entries.get(5), _df));
			entry.setEndDate(LocalDate.parse(entries.get(6), _df));
		} catch (Exception pe) {
			log.warn("Error parsing date - " + pe.getMessage());
			_status.addMessage("Error parsing date - " + pe.getMessage());
		}
			
		try {
			entry.setTimeD(LocalDateTime.of(entry.getStartDate(), LocalTime.parse(entries.get(18), _tf)));
			entry.setTimeA(LocalDateTime.of(entry.getStartDate(), LocalTime.parse(entries.get(23), _tf)));
		} catch (Exception pe) {
			log.warn("Error parsing time - " + pe.getMessage());
			_status.addMessage("Error parsing time - " + pe.getMessage());
		}

		return entry;
	}
}