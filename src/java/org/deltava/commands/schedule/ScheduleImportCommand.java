// Copyright 2005, 2006, 2007, 2010, 2012, 2015, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.file.*;
import org.deltava.dao.file.GetSchedule;
import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to import raw Flight Schedule data.
 * @author Luke
 * @version 10.0
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

		ImportStatus st = null; boolean doPurge = Boolean.valueOf(ctx.getParameter("doPurge")).booleanValue();
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
					
				case LEGACY:
				case MANUAL:
					GetRawSchedule rsdao = new GetRawSchedule(con);
					Collection<ScheduleSourceInfo> srcs = rsdao.getSources(false, ctx.getDB());
					try (InputStream is = new FileInputStream(f)) {
						GetSchedule dao = new GetSchedule(ss, is);
						dao.setAircraft(acdao.getAircraftTypes());
						dao.setAirlines(adao.getActive().values());
						srcs.forEach(ssi -> dao.setMaxLine(ssi.getSource(), ssi.getMaxLineNumber()));
						dao.process().stream().filter(se -> (se.getSource() == ss)).forEach(entries::add);
						st = dao.getStatus();
					}
					
					break;

				default:
					throw new CommandException("Unknown Schedule Source - " + ss);
				}
			}
			
			// Find variable entries
			Collection<RawScheduleEntry> variedEQ = entries.stream().filter(ScheduleEntry::isVariable).collect(Collectors.toList());
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
			entries.stream().filter(se -> !ScheduleEntry.isVariable(se)).forEach(se -> {
				Collection<Aircraft> ac = airlineEQ.get(se.getAirline());
				if (ac != null)
					ac.add(allAC.get(se.getEquipmentType()));
			});
			
			String appCode = SystemData.get("airline.code"); Map<String, Aircraft> pastChoices = new HashMap<String, Aircraft>();
			for (RawScheduleEntry rse : variedEQ) {
				Aircraft a = pastChoices.get(rse.getShortCode());
				if (a != null) {
					rse.setEquipmentType(a.getName());
					log.debug("Variable equipment for " + rse.getShortCode() + ", reusing " + rse.getEquipmentType());
					continue;
				}
				
				List<Aircraft> eqTypes = sedao.getEquipmentTypes(rse, rse.getAirline()).stream().map(acType -> allAC.get(acType)).filter(ac -> (ac != null) && (ac.getHistoric() == rse.getHistoric())).collect(Collectors.toList());
				if (eqTypes.isEmpty())
					sedao.getEquipmentTypes(rse, null).stream().map(acType -> allAC.get(acType)).filter(ac -> (ac != null) && (ac.getHistoric() == rse.getHistoric())).forEach(eqTypes::add);
				
				// Determine variable equipment
				if (eqTypes.isEmpty()) {
					List<Aircraft> possibleEQ = airlineEQ.get(rse.getAirline()).stream().filter(ac -> (ac.getOptions(appCode).getRange() > rse.getDistance())).collect(Collectors.toList());
					boolean hasHistoric = possibleEQ.stream().anyMatch(ac -> ac.getHistoric() == rse.getHistoric());
					if (hasHistoric)
						possibleEQ.removeIf(ac -> ac.getHistoric() != rse.getHistoric());
					
					if (!possibleEQ.isEmpty()) {
						Collections.shuffle(possibleEQ); Aircraft ac = possibleEQ.get(0); 
						rse.setEquipmentType(ac.getName());
						log.debug("Variable equipment for " + rse.getShortCode() + ", using " + rse.getEquipmentType());
						pastChoices.put(rse.getShortCode(), ac);
					} else
						log.warn("Variable equipment for " + rse.getShortCode() + " (" + rse.getAirportD().getIATA() + "-" + rse.getAirportA().getIATA() + "), no available aircraft!");
				}
				else
					rse.setEquipmentType(eqTypes.get(0).getName());
			}

			// Save the data
			ctx.startTX();
			SetSchedule swdao = new SetSchedule(con);
			if (doPurge && !entries.isEmpty()) {
				int purgeCount = swdao.purgeRaw(ss);
				log.info("Purged " + purgeCount + " raw schedule entries from " + ss.getDescription());
				ctx.setAttribute("purgeCount", Integer.valueOf(purgeCount), REQUEST);
			}
			
			int entryCount = entries.size();
			for (Iterator<RawScheduleEntry> i = entries.iterator(); i.hasNext(); ) {
				swdao.writeRaw(i.next(), false);
				i.remove();
			}
			
			// Load schedule sources
			CacheManager.invalidate("ScheduleSource");
			GetRawSchedule rsdao = new GetRawSchedule(con);
			Collection<ScheduleSourceInfo> stats = rsdao.getSources(false, ctx.getDB());
			ctx.setAttribute("srcAirlines", rsdao.getSourceAirlines(), REQUEST);
			
			// Save the status
			SetImportStatus sswdao = new SetImportStatus(SystemData.get("schedule.cache"), ss.name() + ".import.status.txt");	
			sswdao.write(st);
			ctx.commitTX();

			// Set status attributes
			ctx.setAttribute("rawEntryStats", stats, REQUEST);
			ctx.setAttribute("sources", stats, REQUEST);
			ctx.setAttribute("status", st, REQUEST);
			ctx.setAttribute("importCount", Integer.valueOf(entryCount), REQUEST);
			
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
	
	private static InputStream parsePDF(File f) throws IOException, DAOException {
		
		if (f.getName().toLowerCase().endsWith(".gz"))
			return new GZIPInputStream(new FileInputStream(f), 65536);

		boolean isPDF = false;
		try (InputStream his = new BufferedInputStream(new FileInputStream(f))) {
			byte[] b = new byte[64]; his.read(b);
			isPDF = PDFUtils.isPDF(b);
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