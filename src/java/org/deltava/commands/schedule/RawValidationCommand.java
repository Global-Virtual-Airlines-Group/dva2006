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
			List<RawScheduleEntry> entries = rsdao.load(src, srcInfo.getEffectiveDate()); Collection<ScheduleEntry> results = new LinkedHashSet<ScheduleEntry>();
			Collections.sort(entries, new ScheduleEntryComparator(ScheduleEntryComparator.ROUTE_FLIGHT));
			ScheduleEntry lse = null;
			for (ScheduleEntry se : entries) {
				Aircraft a = allAC.get(se.getEquipmentType());
				
				// Calculate flight time
				int ft = (int) (se.getDistance() * 10 / (a.getCruiseSpeed() / DistanceUnit.NM.getFactor())); 
				int ln = se.getLength(); int ff = Math.max(16, Math.round(ln * 0.325f) + 7);
				if ((ln < Math.max(4, ft - ff)) || (ln > (ft + ff))) {
					lse = se;
					results.add(se);
					continue;
				}
				
				// Check for duplicate flight number
				if ((lse != null) && (FlightNumber.compare(se, lse, true) == 0)) {
					results.add(lse);
					results.add(se);
				}
				
				lse = se;
			}
			
			// Slice and save results
			ctx.setAttribute("srcInfo", srcInfo, REQUEST);
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