// Copyright 2006, 2009, 2016, 2017, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.*;
import java.util.*;
import java.time.*;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.common.*;

/**
 * A Web Site Command to save imported Flight Schedule data to the database.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class ScheduleFilterCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(ScheduleFilterCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Load schedule sources
		CommandResult result = ctx.getResult();
		try {
			GetRawSchedule rsdao = new GetRawSchedule(ctx.getConnection());
			ctx.setAttribute("sources", rsdao.getSources(), REQUEST);
			ctx.setAttribute("srcAirlines", rsdao.getSourceAirlines(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// If we're doing a get, then redirect to the JSP
		if (ctx.getParameters("src") == null) {
			ctx.setAttribute("today", LocalDate.now(), REQUEST);
			result.setURL("/jsp/schedule/flightFilter.jsp");
			result.setSuccess(true);
			return;
		}

		// Load import options
		LocalDate effectiveDate = LocalDate.ofInstant(StringUtils.parseInstant(ctx.getParameter("effDate"), "MM/dd/yyyy"), ZoneOffset.UTC);
		Collection<ScheduleSource> sources = ctx.getParameters("src", Collections.emptyList()).stream().map(src -> ScheduleSource.valueOf(src)).collect(Collectors.toCollection(TreeSet::new));
		
		// Load source/airline mappings
		Map<ScheduleSource, Collection<Airline>> srcAirlines = new HashMap<ScheduleSource, Collection<Airline>>();
		for (ScheduleSource src : sources) {
			Collection<String> srcCodes = ctx.getParameters("airline-" + src.name(), Collections.emptyList()); 
			srcAirlines.put(src, srcCodes.stream().map(ac -> SystemData.getAirline(ac)).filter(Objects::nonNull).collect(Collectors.toSet()));
		}

		// Save the entries
		Set<RawScheduleEntry> entries = new LinkedHashSet<RawScheduleEntry>();
		try {
			Connection con = ctx.getConnection();
			
			// Figure out what routePairs to import by source
			GetRawSchedule rawdao = new GetRawSchedule(con);
			Map<String, ImportRoute> srcPairs = new HashMap<String, ImportRoute>();
			for (ScheduleSource src : sources) {
				Collection<ImportRoute> rts = rawdao.getImportData(src, effectiveDate);
				for (ImportRoute ir : rts) {
					String k = ir.createKey();
					if (!srcPairs.containsKey(k))
						srcPairs.put(k, ir);
				}
			}

			// Start the transaction
			ctx.startTX();
			
			// Purge if needed
			SetSchedule dao = new SetSchedule(con);
			boolean doPurge = Boolean.valueOf(ctx.getParameter("purgeAll")).booleanValue();
			if (doPurge) {
				dao.purge();
				dao.purgeSourceAirlines();
			}
			
			// Load the entries, save source mappings
			for (ScheduleSource src : sources) {
				Collection<Airline> validAirlines = srcAirlines.getOrDefault(src, Collections.emptyList());
				
				// Get the raw data
				List<RawScheduleEntry> srcEntries = rawdao.load(src, effectiveDate).stream().filter(se -> validAirlines.contains(se.getAirline())).collect(Collectors.toList());
				for (RawScheduleEntry rse : srcEntries) {
					ImportRoute ir = srcPairs.get(rse.createKey());
					if ((ir == null) || (ir.getSource() != src))
						continue;
					
					boolean isAdded = entries.add(rse);
					if (!isAdded)
						log.info(rse.getShortCode() + " already exists");
				}
				
				log.info("Loaded " + srcEntries.size() + " " + src.getDescription() + " schedule entries for " + effectiveDate);
				dao.writeSourceAirlines(src, validAirlines);
			}
			
			// Save the schedule entries
			for (RawScheduleEntry rse : entries)
				dao.write(rse, false);
			
			// Save effective date
			SetMetadata mdwdao = new SetMetadata(con);
			String aCode = SystemData.get("airline.code").toLowerCase();
			mdwdao.write(aCode + ".schedule.effDate", effectiveDate.atStartOfDay().toInstant(ZoneOffset.UTC));

			// Change transaction isolocation
			con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			
			// Load the airport service map
			GetScheduleInfo sidao = new GetScheduleInfo(con);
			AirportServiceMap svcMap = sidao.getRoutePairs();
			
			// Determine unserviced airports
			boolean updateAirports = false;
			SetAirportAirline awdao = new SetAirportAirline(con);
			synchronized (SystemData.class) {
				Collection<Airport> allAirports = SystemData.getAirports().values();
				for (Airport ap : allAirports) {
					Collection<String> newAirlines = svcMap.getAirlineCodes(ap);
					if (CollectionUtils.hasDelta(ap.getAirlineCodes(), newAirlines)) {
						log.info("Updating " + ap.getName() + " new codes = " + newAirlines + ", was " + ap.getAirlineCodes());
						updateAirports = true;
						ap.setAirlines(svcMap.getAirlineCodes(ap));
						awdao.update(ap, ap.getIATA());
					}
				}
			}

			ctx.commitTX();
			
			// Update airport map
			if (updateAirports)
				EventDispatcher.send(new SystemEvent(SystemEvent.Type.AIRPORT_RELOAD));
		} catch (SQLException | DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save the status
		ctx.setAttribute("isFlights", Boolean.TRUE, REQUEST);
		ctx.setAttribute("entryCount", Integer.valueOf(entries.size()), REQUEST);
		ctx.setAttribute("doPurge", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/schedule/scheduleUpdate.jsp");
		result.setSuccess(true);
	}
}