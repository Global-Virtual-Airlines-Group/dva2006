// Copyright 2005, 2006, 2007, 2010, 2012, 2015, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.sql.Connection;
import java.time.LocalDate;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.file.*;

import org.deltava.security.command.ScheduleAccessControl;
import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to import raw Flight Schedule data.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class ScheduleImportCommand extends AbstractCommand {

	protected static final Logger log = Logger.getLogger(ScheduleImportCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command results
		CommandResult result = ctx.getResult();

		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanImport())
			throw securityException("Cannot import Flight Schedule data");

		// If we are not uploading a CSV file, then redirect to the JSP
		ScheduleSource ss = StringUtils.isEmpty(ctx.getParameter("schedType")) ? null : ScheduleSource.valueOf(ctx.getParameter("schedType"));
		if (ss == null) {
			result.setURL("/jsp/schedule/flightImport.jsp");
			result.setSuccess(true);
			return;
		}

		ImportStatus st = null;
		File f = (ss == ScheduleSource.INNOVATA) ? new File(SystemData.get("schedule.innovata.file")) : new File(SystemData.get("path.upload"), ctx.getParameter("id"));
		try {
			// Get the DAOs
			Connection con = ctx.getConnection();
			GetAirline adao = new GetAirline(con);
			GetAircraft acdao = new GetAircraft(con);

			Collection<RawScheduleEntry> entries = new ArrayList<RawScheduleEntry>();
			try (InputStream fis = new FileInputStream(f)) {
				switch (ss) {
				case DELTA:
					try (InputStream is = parsePDF(f)) {
						GetDeltaSchedule dao = new GetDeltaSchedule(is);
						dao.setAircraft(acdao.getAircraftTypes());
						dao.setAirlines(adao.getActive().values());
						entries.addAll(dao.process());
						st = dao.getStatus();
					}

					break;

				case SKYTEAM:
					try (InputStream is = parsePDF(f)) {
						GetSkyTeamSchedule dao = new GetSkyTeamSchedule(is);
						dao.setAircraft(acdao.getAircraftTypes());
						dao.setAirlines(adao.getActive().values());
						entries.addAll(dao.process());
						st = dao.getStatus();
					}

					break;

				case INNOVATA:
					try (GZIPInputStream zis = new GZIPInputStream(fis, 131072)) {
						GetFullSchedule dao = new GetFullSchedule(zis);
						dao.setAircraft(acdao.getAircraftTypes());
						dao.setAirlines(adao.getActive().values());
						dao.setMainlineCodes((List<String>) SystemData.getObject("schedule.innovata.primary_codes"));
						dao.setCodeshareCodes((List<String>) SystemData.getObject("schedule.innovata.codeshare_codes"));
						entries.addAll(dao.process());
						st = dao.getStatus();
					}

					break;

				default:
					throw new CommandException("Unknown Schedule Source - " + ss);
				}
			}
			
			// Find "EQV" entries
			Collection<RawScheduleEntry> variedEQ = entries.stream().filter(ScheduleImportCommand::isVariable).collect(Collectors.toList());
			Collection<Airline> airlines = variedEQ.stream().map(ScheduleEntry::getAirline).collect(Collectors.toSet());
			Map<String, Aircraft> allAC = CollectionUtils.createMap(acdao.getAircraftTypes(), Aircraft::getName);
			
			// Load equipment types
			GetScheduleEquipment sedao = new GetScheduleEquipment(con);
			Map<Airline, Collection<Aircraft>> airlineEQ = new HashMap<Airline, Collection<Aircraft>>();
			for (Airline a : airlines) {
				Collection<Aircraft> aircraft = new LinkedHashSet<Aircraft>();
				airlineEQ.put(a, aircraft);
				Collection<String> eqCodes = sedao.getEquipmentTypes(a);
				eqCodes.forEach(eq -> aircraft.add(allAC.get(eq)));
			}
			
			// Get unvaried
			entries.stream().filter(se -> !isVariable(se)).forEach(se -> {
				Collection<Aircraft> ac = airlineEQ.get(se.getAirline());
				if (ac != null)
					ac.add(allAC.get(se.getEquipmentType()));
			});
			
			String appCode = SystemData.get("airline.code"); Map<String, Aircraft> pastChoices = new HashMap<String, Aircraft>();
			for (RawScheduleEntry rse : variedEQ) {
				Aircraft a = pastChoices.get(rse.getShortCode());
				if (a != null) {
					rse.setEquipmentType(a.getName());
					log.info("Variable equipment for " + rse.getShortCode() + ", reusing " + rse.getEquipmentType());
					continue;
				}
				
				Collection<String> eqTypes = sedao.getEquipmentTypes(rse, rse.getAirline());
				if (eqTypes.isEmpty())
					eqTypes = sedao.getEquipmentTypes(rse, null);
				
				if (eqTypes.isEmpty()) {
					List<Aircraft> possibleEQ = airlineEQ.get(rse.getAirline()).stream().filter(ac -> (ac.getOptions(appCode).getRange() > rse.getDistance())).collect(Collectors.toList());
					if (!possibleEQ.isEmpty()) {
						Collections.shuffle(possibleEQ); Aircraft ac = possibleEQ.get(0); 
						rse.setEquipmentType(ac.getName());
						log.info("Variable equipment for " + rse.getShortCode() + ", using " + rse.getEquipmentType());
						pastChoices.put(rse.getShortCode(), ac);
					} else
						log.warn("Variable equipment for " + rse.getShortCode() + " (" + rse.getAirportD().getIATA() + "-" + rse.getAirportA().getIATA() + "), no available aircraft!");
				}
				else
					rse.setEquipmentType(eqTypes.iterator().next());
			}

			// Save the data
			ctx.startTX();
			SetSchedule swdao = new SetSchedule(con);
			int purgeCount = swdao.purgeRaw(ss); int entryCount = entries.size();
			for (Iterator<RawScheduleEntry> i = entries.iterator(); i.hasNext(); ) {
				swdao.writeRaw(i.next());
				i.remove();
			}
			
			// Load schedule sources
			GetRawSchedule rsdao = new GetRawSchedule(con);
			Collection<ScheduleSourceInfo> stats = rsdao.getSources(true);
			
			// Save the status
			SetImportStatus sswdao = new SetImportStatus(SystemData.get("schedule.cache"), ss.name() + ".import.status.txt");	
			sswdao.write(st);
			ctx.commitTX();

			// Set status attributes
			ctx.setAttribute("rawEntryStats", stats, REQUEST);
			ctx.setAttribute("sources", stats, REQUEST);
			ctx.setAttribute("status", st, REQUEST);
			ctx.setAttribute("importCount", Integer.valueOf(entryCount), REQUEST);
			ctx.setAttribute("purgeCount", Integer.valueOf(purgeCount), REQUEST);
			ctx.setAttribute("today", LocalDate.now(), REQUEST);
		} catch (DAOException | IOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setURL("/jsp/schedule/flightFilter.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
	
	private static boolean isVariable(ScheduleEntry se) {
		return "EQV".equals(se.getEquipmentType());
	}

	private static InputStream parsePDF(File f) throws IOException, DAOException {

		boolean isPDF = false;
		try (InputStream his = new FileInputStream(f)) {
			byte[] b = new byte[64]; int bytesRead = his.read(b);
			isPDF = (bytesRead > Chart.PDF_MAGIC.length());
			for (int x = 0; isPDF && (x < Chart.PDF_MAGIC.length()); x++)
				isPDF &= (b[x] == Chart.PDF_MAGIC.getBytes()[x]);
		}

		if (isPDF) {
			try (InputStream in = new FileInputStream(f)) {
				GetPDFText pdfdao = new GetPDFText(in);
				String txt = pdfdao.getText();
				return new ByteArrayInputStream(txt.getBytes());
			}
		}

		return new FileInputStream(f);
	}
}