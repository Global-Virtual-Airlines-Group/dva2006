// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to extend the validity of a day's AviationStack schedule entries. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class AVStackExtendTask extends Task {

	/**
	 * Creates the Scheduled Task.
	 */
	public AVStackExtendTask() {
		super("AviationStack Extend", AVStackExtendTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		
		// Get download window size
		int daysFwd = SystemData.getInt("schedule.avstack.days", 14);
		LocalDate effDate = LocalDate.now().plusDays(daysFwd);
		
		try {
			Connection con = ctx.getConnection();
			
			// See if we have flights for that day
			GetRawSchedule rsdao = new GetRawSchedule(con);
			final Collection<RawScheduleEntry> entries = rsdao.load(ScheduleSource.AVSTACK, effDate);
			if (entries.isEmpty()) {
				log.warn("No AviationStack fligts found for {}", StringUtils.format(effDate, "MM/dd/yyyy"));
				
				// Determine what day to load
				DayOfWeek dw = effDate.getDayOfWeek();
				LocalDate copyDate = effDate.minusDays((dw == DayOfWeek.SATURDAY) || (dw == DayOfWeek.SUNDAY) || (dw == DayOfWeek.MONDAY) ? 7 : 1);
				entries.addAll(rsdao.load(ScheduleSource.AVSTACK, copyDate));
				if (entries.isEmpty()) {
					log.error("No AviationStack fligts found for {}, aborting", StringUtils.format(copyDate, "MM/dd/yyyy"));
					ctx.release();
					return;
				}
				
				// Clone into a new schedule entry object
				List<RawScheduleEntry> newEntries = entries.stream().map(rse -> clone(rse, copyDate)).toList();
				
				// Get max line
				ctx.startTX();
				GetRawScheduleInfo rsidao = new GetRawScheduleInfo(con);
				int srcLine = rsidao.getNextLine(ScheduleSource.AVSTACK);
				
				// Extend by a day
				SetSchedule wdao = new SetSchedule(con);
				for (RawScheduleEntry rse : newEntries) {
					rse.setLineNumber(srcLine++);
					wdao.writeRaw(rse, false);
				}
				
				ctx.commitTX();
				log.info("Wrote {} raw schedule entries for {}", Integer.valueOf(newEntries.size()), StringUtils.format(copyDate, "MM/dd/yyyy"));
			} else
				log.info("Found {} AviationStack flights for {}", Integer.valueOf(entries.size()), StringUtils.format(effDate, "MM/dd/yyyy"));
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}

		log.info("Complete");
	}
	
	private static RawScheduleEntry clone(RawScheduleEntry se, LocalDate toDate) {
		RawScheduleEntry rse = new RawScheduleEntry(se.getAirline(), se.getFlightNumber(), se.getLeg());
		rse.setAirportD(se.getAirportD());
		rse.setAirportA(se.getAirportA());
		rse.setEquipmentType(se.getEquipmentType());
		rse.setStartDate(toDate);
		rse.setEndDate(toDate.plusDays(1));
		rse.setIsUTC(se.getIsUTC());
		rse.setTimeD(se.getTimeD().toLocalDateTime());
		rse.setTimeA(se.getTimeA().toLocalDateTime());
		rse.setAcademy(se.getAcademy());
		rse.setHistoric(se.getHistoric());
		rse.setForceInclude(se.getForceInclude());
		rse.setSource(se.getSource());
		rse.setCodeShare(se.getCodeShare());
		rse.setComments(se.getComments());
		rse.setRemarks(se.getRemarks());
		rse.setDayMap(1 << toDate.getDayOfWeek().ordinal());
		return rse;
	}
}