// Copyright 2005, 2006, 2007, 2010, 2012, 2015, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.file.*;

import org.deltava.security.command.ScheduleAccessControl;

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
						GetRawPDFSchedule dao = new GetRawPDFSchedule(is);
						dao.setAircraft(acdao.getAircraftTypes());
						entries.addAll(dao.process());
						st = dao.getStatus();
					}

					break;

				case SKYTEAM:
					try (InputStream is = parsePDF(f)) {
						GetSkyTeamSchedule dao = new GetSkyTeamSchedule(is);
						dao.setAircraft(acdao.getAircraftTypes());
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
						dao.load();
						entries.addAll(dao.process());
						st = dao.getStatus();
					}

					break;

				default:
					throw new CommandException("Unknown Schedule Source - " + ss);
				}
			}
			
			// Find "EQV" entries
			Collection<RawScheduleEntry> variedEQ = entries.stream().filter(se -> "EQV".equals(se.getEquipmentType())).collect(Collectors.toList());
			Collection<Airline> airlines = variedEQ.stream().map(ScheduleEntry::getAirline).collect(Collectors.toSet());
			
			// Load equipment types
			GetScheduleEquipment sedao = new GetScheduleEquipment(con);
			Map<Airline, List<Aircraft>> airlineEQ = new HashMap<Airline, List<Aircraft>>();
			for (Airline a : airlines) {
				List<Aircraft> aircraft = new ArrayList<Aircraft>();
				airlineEQ.put(a, aircraft);
				Collection<String> eqCodes = sedao.getEquipmentTypes(a);
				for (String eq : eqCodes)
					aircraft.add(acdao.get(eq));
			}
			
			String appCode = SystemData.get("airline.code");
			for (Iterator<RawScheduleEntry> i = variedEQ.iterator(); i.hasNext(); ) {
				RawScheduleEntry rse = i.next();
				Collection<String> eqTypes = sedao.getEquipmentTypes(rse, rse.getAirline());
				if (eqTypes.isEmpty())
					eqTypes = sedao.getEquipmentTypes(rse, null);
				
				if (eqTypes.isEmpty()) {
					List<Aircraft> possibleEQ = airlineEQ.get(rse.getAirline()).stream().filter(ac -> (ac.getOptions(appCode).getRange() > rse.getDistance())).collect(Collectors.toList());
					if (!possibleEQ.isEmpty()) {
						Collections.shuffle(possibleEQ);
						rse.setEquipmentType(possibleEQ.get(0).getName());
						log.info("Unknown variable equipment for " + rse.getShortCode() + ", setting to " + rse.getEquipmentType());
					} else {
						log.warn("Unknown variable equipment for " + rse.getShortCode() + ", no available aircraft!");
						i.remove();
					}
				}
				else
					rse.setEquipmentType(eqTypes.iterator().next());
			}

			// Save the data
			ctx.startTX();
			SetSchedule swdao = new SetSchedule(con);
			swdao.purgeRaw(ss);
			for (RawScheduleEntry rse : entries)
				swdao.writeRaw(rse);
			
			// Load schedule sources
			GetRawSchedule rsdao = new GetRawSchedule(con);
			Collection<ScheduleSourceInfo> stats = rsdao.getSources();
			
			// Save the status
			SetImportStatus sswdao = new SetImportStatus(SystemData.get("schedule.cache"), ss.name() + ".import.status.txt");	
			sswdao.write(st);
			ctx.commitTX();

			// Set status attributes
			ctx.setAttribute("rawEntryStats", stats, REQUEST);
			ctx.setAttribute("sources", stats.stream().map(ScheduleSourceInfo::getSource).collect(Collectors.toList()), REQUEST);
			ctx.setAttribute("status", st, REQUEST);
			ctx.setAttribute("importCount", Integer.valueOf(entries.size()), REQUEST);
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