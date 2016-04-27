// Copyright 2005, 2010, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;

import org.deltava.comparators.PilotComparator;

/**
 * A Web Site Command to display &quot;Century Club&quot; members.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class CenturyClubCommand extends AbstractCommand {

	 /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Load the accomplishments
			GetAccomplishment adao = new GetAccomplishment(con);
			List<Accomplishment> accs = adao.getByUnit(AccomplishUnit.LEGS).stream().filter(a -> (a.getValue() > 100)).collect(Collectors.toList());
			
			// Create the comparator
			Comparator<Pilot> cmp = new PilotComparator(PilotComparator.LEGS).reversed();
			
			// Get the Pilots
			GetPilotRecognition dao = new GetPilotRecognition(con);
			List<Pilot> pilots = CollectionUtils.sort(dao.getByAccomplishment(accs.get(0).getID()), cmp);
			
			// Combine them based on achievement
			Collections.reverse(accs);
			Map<Accomplishment, Collection<Pilot>> roster = new LinkedHashMap<Accomplishment, Collection<Pilot>>();
			for (Accomplishment a : accs) {
				Collection<Pilot> bucket = new ArrayList<Pilot>();
				roster.put(a, bucket);
				for (Iterator<Pilot> i = pilots.iterator(); i.hasNext(); ) {
					Pilot p = i.next();
					if (p.getLegs() >= a.getValue()) {
						bucket.add(p);
						i.remove();
					} else
						break;
				}
			}
			
			ctx.setAttribute("roster", roster, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/roster/centuryClub.jsp");
		result.setSuccess(true);
	}
}