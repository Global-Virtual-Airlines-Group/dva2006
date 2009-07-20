// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.acars.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A helper class for viewing ACARS logs.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public abstract class ACARSLogViewCommand extends AbstractViewCommand {

	/**
	 * Calculates the search type from the request, and updates request attributes.
	 * @param ctx the Command context
	 * @return a search type constant
	 */
	protected LogSearchCriteria getSearchCriteria(CommandContext ctx, Connection con) throws DAOException {
		
		// Get the start/end dates
		Date sd = parseDateTime(ctx, "start", "MM/dd/yyyy", "HH:mm");
		Date ed = parseDateTime(ctx, "end", "MM/dd/yyyy", "HH:mm");

		// Create the criteria bean
		LogSearchCriteria result = new LogSearchCriteria(sd, ed);

		// Get the pilot ID
		String pCode = ctx.getParameter("pilotCode");
		if (validatePilotCode(pCode)) {
			UserID id = new UserID(pCode);
            GetPilot pdao = new GetPilot(con);
            Pilot usr = pdao.getPilotByCode(id.getUserID(), id.getAirlineCode());
            result.setPilotID((usr == null) ? 0 : usr.getID());
		}

		return result;
	}

	/**
	 * Returns a Set of Pilot IDs from the view results.
	 * @param viewEntries the view result entries
	 * @return a Set of Pilot IDs
	 */
	protected Set<Integer> getPilotIDs(Collection<?> viewEntries) {
		Set<Integer> results = new HashSet<Integer>();
		for (Iterator<?> i = viewEntries.iterator(); i.hasNext();) {
			ACARSLogEntry entry = (ACARSLogEntry) i.next();
			results.add(new Integer(entry.getPilotID()));
		}

		return results;
	}

	/**
	 * Validates that a Pilot Code contains a valid database name.
	 * @param pCode the Pilot Code
	 * @return TRUE if the Pilot Code starts with a valid database, otherwise FALSE
	 */
	private boolean validatePilotCode(String pCode) {
		if (pCode == null)
			return false;

		// Get the airline codes
		Map<?, ?> apps = (Map<?, ?>) SystemData.getObject("apps");
		for (Iterator<?> i = apps.values().iterator(); i.hasNext();) {
			AirlineInformation info = (AirlineInformation) i.next();
			if (pCode.toUpperCase().startsWith(info.getCode().toUpperCase()))
				return true;
		}

		return false;
	}
}