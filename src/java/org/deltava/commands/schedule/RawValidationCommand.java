// Copyright 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.DistanceUnit;
import org.deltava.beans.FlightNumber;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.comparators.ScheduleEntryComparator;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.EnumUtils;

/**
 * A Web Site Command to display potentially questionable Raw Scedule entires.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class RawValidationCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<ScheduleEntry> vctxt = initView(ctx, ScheduleEntry.class);
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/rawValidate.jsp");
		try {
			Connection con = ctx.getConnection();
			
			// Load aircraft
			GetAircraft acdao = new GetAircraft(con);
			Map<String, Aircraft> allAC = CollectionUtils.createMap(acdao.getAircraftTypes(), Aircraft::getName);
			
			// Load schedule sources
			GetRawSchedule rsdao = new GetRawSchedule(con);
			Collection<ScheduleSourceInfo> srcs = rsdao.getSources(false, ctx.getDB());
			ctx.setAttribute("srcs", srcs, REQUEST);
			
			// Get the source
			ScheduleSource src = EnumUtils.parse(ScheduleSource.class, (String) ctx.getCmdParameter(ID, null), null);
			if (src == null) {
				result.setSuccess(true);
				return;
			}
			
			// Get the source info
			ScheduleSourceInfo srcInfo = srcs.stream().filter(ssi -> ssi.getSource().equals(src)).findAny().orElse(null);
			
			// Load and sort
			Map<ScheduleEntry, ValidationResult> vrs = new HashMap<ScheduleEntry, ValidationResult>();
			List<RawScheduleEntry> entries = rsdao.load(src, srcInfo.getEffectiveDate()); Collection<ScheduleEntry> results = new LinkedHashSet<ScheduleEntry>();
			Collections.sort(entries, new ScheduleEntryComparator(ScheduleEntryComparator.ROUTE_FLIGHT));
			ScheduleEntry lse = null; final String aCode = ctx.getDB().toUpperCase();
			for (ScheduleEntry se : entries) {
				ValidationResult vr = new ValidationResult();
				Aircraft a = allAC.get(se.getEquipmentType()); AircraftPolicyOptions ao = a.getOptions(aCode);
				if (ao == null)
					vr.setPolicy(true);
				else if (se.getDistance() > ao.getRange()) {
					vr.setAircraft(true);
					vr.setRange(0, ao.getRange());
				} else if (se.getAirportD().getMaximumRunwayLength() < ao.getTakeoffRunwayLength()) {
					vr.setAircraft(true);
					vr.setRange(0, ao.getTakeoffRunwayLength());
				} else if (se.getAirportA().getMaximumRunwayLength() < ao.getLandingRunwayLength()) {
					vr.setAircraft(true);
					vr.setRange(0, ao.getLandingRunwayLength());
				}
				
				// Calculate flight time
				int ft = (int) (se.getDistance() * 10 / (a.getCruiseSpeed() / DistanceUnit.NM.getFactor())); 
				int ln = se.getLength(); int ff = Math.max(16, Math.round(ln * 0.325f) + 7);
				if ((ln < Math.max(4, ft - ff)) || (ln > (ft + ff))) {
					vr.setTime(true);
					vr.setRange(Math.max(4, ft - ff), ft + ff);
				}
				
				// Check for duplicate flight number
				if ((lse != null) && (FlightNumber.compare(se, lse, true) == 0)) {
					vr.setDuplicate(true);
					results.add(lse);
					results.add(se);
				}
				
				lse = se;
				if (!vr.getIsOK()) {
					results.add(se);
					vrs.put(se, vr);
				}
			}
			
			// Slice and save results
			ctx.setAttribute("srcInfo", srcInfo, REQUEST);
			ctx.setAttribute("results", vrs, REQUEST);
			if (vctxt.getStart() < results.size()) {
				List<ScheduleEntry> lr = new ArrayList<ScheduleEntry>(results);
				vctxt.setResults(lr.subList(vctxt.getStart(), Math.min(vctxt.getEnd(), lr.size())));
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setSuccess(true);
	}
}