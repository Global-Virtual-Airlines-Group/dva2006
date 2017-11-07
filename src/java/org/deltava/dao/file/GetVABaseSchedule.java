// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load VABase-formatted airline schedules.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class GetVABaseSchedule extends ScheduleLoadDAO {
	
	private static final String[] DAYS = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
	private final DateTimeFormatter _tf = new DateTimeFormatterBuilder().appendPattern("H:mm:ss.n").toFormatter();
	
	private static final Logger log = Logger.getLogger(GetVABaseSchedule.class);

	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to read
	 */
	public GetVABaseSchedule(InputStream is) {
		super(is);
	}

	/**
	 * Loads aircraft tail codes from a VABase feed.
	 * @return a Collection of TailCode beans
	 * @throws DAOException if an I/O error occurs
	 */
	public Collection<TailCode> getTailCodes() throws DAOException {
		try (LineNumberReader lr = getReader()) {
			Collection<TailCode> results = new TreeSet<TailCode>();
			lr.readLine(); String data = lr.readLine();
			while (data != null) {
				List<String> csv = StringUtils.split(data, ",");
				String eqType = getEquipmentType(csv.get(4));
				if (eqType == null) {
					_invalidEQ.add(csv.get(4));
					_errors.add("Unknown equipment code at Line " + lr.getLineNumber() + " - " + csv.get(4));
				} else
					results.add(new TailCode(csv.get(3), eqType));
				
				data = lr.readLine();
			}
			
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	@Override
	public Collection<ScheduleEntry> process() throws DAOException {
		
		LocalDate today = LocalDateTime.now().toLocalDate();
		try (LineNumberReader lr = getReader()) {
			Collection<ScheduleEntry> results = new ArrayList<ScheduleEntry>();
			lr.readLine(); String data = lr.readLine();
			while (data != null) {
				List<String> csv = StringUtils.split(data, ","); boolean isOK = true;
				RawScheduleEntry rse = new RawScheduleEntry(FlightCodeParser.parse(csv.get(0)));
				rse.setAirportD(SystemData.getAirport(csv.get(1)));
				rse.setAirportA(SystemData.getAirport(csv.get(3)));
				rse.setTimeD(LocalDateTime.of(today, LocalTime.parse(csv.get(5), _tf)));
				rse.setTimeA(LocalDateTime.of(today, LocalTime.parse(csv.get(6), _tf)));
				rse.setEquipmentType(getEquipmentType(csv.get(9)));
				rse.setTailCode(csv.get(10));
				List<String> days = StringUtils.split(csv.get(8), " ");
				for (String d : days) {
					int ofs = StringUtils.arrayIndexOf(DAYS, d);
					if (ofs != -1)
						rse.addDay(DayOfWeek.of(ofs + 1));
				}
				
				if (rse.getEquipmentType() == null) {
					isOK = false;
					_invalidEQ.add(csv.get(9));
					log.warn("Unknown equipment code at Line " + lr.getLineNumber() + " - " + csv.get(9) + " (" + csv.get(0) + ")");
					_errors.add("Unknown equipment code at Line " + lr.getLineNumber() + " - " + csv.get(9));
				} else if (rse.getAirportD() == null) {
					isOK = false;
					_invalidAP.add(csv.get(1));
					log.warn("Unknown Airport at Line " + lr.getLineNumber() + " - " + csv.get(1) + " (" + csv.get(0) + ")");
					_errors.add("Unknown Airport at Line " + lr.getLineNumber() + " - " + csv.get(1));
				} else if (rse.getAirportA() == null) {
					isOK = false;
					_invalidAP.add(csv.get(3));
					log.warn("Unknown Airport at Line " + lr.getLineNumber() + " - " + csv.get(3) + " (" + csv.get(0) + ")");
					_errors.add("Unknown Airport at Line " + lr.getLineNumber() + " - " + csv.get(3));
				} else if (rse.getAirline() == null) {
					isOK = false;
					StringBuilder ac = new StringBuilder();
					for (char c : csv.get(0).toCharArray())
						if (Character.isLetter(c))
							ac.append(c);
					
					_invalidAL.add(ac.toString());
					log.warn("Unknown airline at Line " + lr.getLineNumber() + " - " + ac + " (" + csv.get(0) + ")");
					_errors.add("Unknown airline at Line " + lr.getLineNumber() + " - " + ac);
				} else if (!rse.getAirline().getApplications().contains(SystemData.get("airline.code"))) {
					isOK = false;
					log.info("Disabled airline at Line " + lr.getLineNumber() + " - " + rse.getAirline().getCode() + " (" + csv.get(0) + ")");
				}
				
				data = lr.readLine();
				if (isOK)
					results.add(rse);
			}
			
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}		
	}
}