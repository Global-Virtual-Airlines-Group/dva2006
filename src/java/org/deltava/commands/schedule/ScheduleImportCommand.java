// Copyright 2005, 2006, 2007, 2010, 2012, 2015, 2019, 2020, 2021, 2022, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.sql.Connection;

import org.apache.logging.log4j.*;

import org.deltava.beans.Compression;
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
 * @version 11.5
 * @since 1.0
 */

public class ScheduleImportCommand extends AbstractCommand {

	protected static final Logger log = LogManager.getLogger(ScheduleImportCommand.class);
	
	private static class RawDupeChecker implements Comparator<RawScheduleEntry> {
		
		protected RawDupeChecker() {
			super();
		}

		@Override
		public int compare(RawScheduleEntry rse1, RawScheduleEntry rse2) {
			
			int tmpResult = rse1.getAirportD().compareTo(rse2.getAirportD());
			if (tmpResult == 0)
				tmpResult = rse1.getAirportA().compareTo(rse2.getAirportA());
			if (tmpResult == 0)
				tmpResult = rse1.compareTo(rse2);
			if (tmpResult == 0)
				tmpResult = rse1.getStartDate().compareTo(rse2.getStartDate());
			if (tmpResult == 0)
				tmpResult = rse1.getEndDate().compareTo(rse2.getEndDate());
			
			return (tmpResult == 0) ?  Integer.compare(rse1.getDayMap(), rse2.getDayMap()) : tmpResult;
		}
	}

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

		ImportStatus st = null; boolean doPurge = Boolean.parseBoolean(ctx.getParameter("doPurge"));
		File f = (ss == ScheduleSource.INNOVATA) ? new File(SystemData.get("schedule.innovata.file")) : new File(SystemData.get("path.upload"), ctx.getParameter("id"));
		try {
			// Get the DAOs
			Connection con = ctx.getConnection();
			GetAirline adao = new GetAirline(con);
			GetAircraft acdao = new GetAircraft(con);

			Collection<RawScheduleEntry> entries = new ArrayList<RawScheduleEntry>();
			Compression c = Compression.detect(f);
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
					try (InputStream is = c.getStream(fis)) {
						GetFullSchedule dao = new GetFullSchedule(is);
						dao.setAircraft(acdao.getAircraftTypes());
						dao.setAirlines(adao.getActive().values());
						dao.setMainlineCodes((List<String>) SystemData.getObject("schedule.innovata.primary_codes"));
						dao.setCodeshareCodes((List<String>) SystemData.getObject("schedule.innovata.codeshare_codes"));
						entries.addAll(dao.process());
						st = dao.getStatus();
					}

					break;
					
				case VASYS:
					try (InputStream is = c.getStream(fis)) {
						GetPHPVMSSchedule dao = new GetPHPVMSSchedule(is);
						dao.setAircraft(acdao.getAircraftTypes());
						dao.setAirlines(adao.getActive().values());
						entries.addAll(dao.process());
						st = dao.getStatus();
					}
					
					break;
					
				case LEGACY:
				case MANUAL:
					boolean isUTC = Boolean.parseBoolean(ctx.getParameter("isUTC"));
					GetRawSchedule rsdao = new GetRawSchedule(con);
					Collection<ScheduleSourceInfo> srcs = rsdao.getSources(false, ctx.getDB());
					try (InputStream is = new FileInputStream(f)) {
						GetSchedule dao = new GetSchedule(ss, is, isUTC);
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
				eqCodes.stream().map(code -> allAC.get(code)).filter(Objects::nonNull).forEach(aircraft::add);
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
					log.debug("Variable equipment for {}, reusing {}", rse.getShortCode(), rse.getEquipmentType());
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
						log.debug("Variable equipment for {}, using {}", rse.getShortCode(), rse.getEquipmentType());
						pastChoices.put(rse.getShortCode(), ac);
					} else
						log.warn("Variable equipment for {} ({}-{}), no available aircraft", rse.getShortCode(), rse.getAirportD().getIATA(), rse.getAirportA().getIATA());
				}
				else
					rse.setEquipmentType(eqTypes.get(0).getName());
			}
			
			// Eliminate dupes
			int rawEntryCount = entries.size();
			Collection<RawScheduleEntry> dupeFilter = new TreeSet<RawScheduleEntry>(new RawDupeChecker());
			entries.removeIf(se -> !dupeFilter.add(se)); int dupeCount = rawEntryCount - entries.size(); 
			log.info("Removed {} duplicate schedule entries", Integer.valueOf(dupeCount));

			// Save the data
			ctx.startTX();
			SetSchedule swdao = new SetSchedule(con);
			if (doPurge && !entries.isEmpty()) {
				int purgeCount = swdao.purgeRaw(ss);
				log.info("Purged {} raw schedule entries from {}", Integer.valueOf(purgeCount), ss.getDescription());
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
			ctx.setAttribute("dupeCount", Integer.valueOf(dupeCount), REQUEST);
			ctx.setAttribute("importCount", Integer.valueOf(entryCount), REQUEST);
			ctx.setAttribute("today", LocalDate.now(), REQUEST);
		} catch (DAOException | IOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setURL("/jsp/schedule/schedFilter.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
	
	private static InputStream parsePDF(File f) throws IOException, DAOException {
		
		if (f.getName().toLowerCase().endsWith(".gz"))
			return new GZIPInputStream(new FileInputStream(f), 65536);
		if (f.getName().toLowerCase().endsWith(".bz2"))
			return new BZip2MultiInputStream(new BufferedInputStream(new FileInputStream(f), 65536));

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