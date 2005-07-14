package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * Web site command to display the Pilot Center.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotCenterCommand extends AbstractTestHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		Pilot p = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the pilot profile from the database and stuff it in the request and the session
			GetPilot pdao = new GetPilot(con);
			p = pdao.get(ctx.getUser().getID());
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.getSession().setAttribute(CommandContext.USER_ATTR_NAME, p);

			// Load all PIREPs
			GetFlightReports frdao = new GetFlightReports(con);
			List results = frdao.getByPilot(p.getID(), null);
			ctx.setAttribute("flights", results, REQUEST);
			p.setFlights(results);

			// Save the latest PIREP as a separate bean in the request
			if (results.size() > 0)
				ctx.setAttribute("lastFlight", p.getFlights().get(0), REQUEST);

			// Get the Assistant Chief Pilots (if any) for the equipment program
			ctx.setAttribute("asstCP", pdao.getPilotsByEQRank(Ranks.RANK_ACP, p.getEquipmentType()), REQUEST);

			// Initialize the testing history helper
			initTestHistory(p, con);

			// Save the pilot's equipment program and check if we can get promoted to Captain
			ctx.setAttribute("eqType", _testHistory.getEquipmentType(), REQUEST);
			ctx.setAttribute("captPromote", Boolean.valueOf(_testHistory.canPromote(_testHistory.getEquipmentType())), REQUEST);

			// Check if we are trying to switch equipment types
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.get(p.getID());
			if (txreq == null) {
				// Get all active equipment programs, and see which we can switch to
				GetEquipmentType eqdao = new GetEquipmentType(con);
				List activeEQ = eqdao.getActive();
				for (Iterator i = activeEQ.iterator(); i.hasNext();) {
					EquipmentType eq = (EquipmentType) i.next();
					if (!_testHistory.canSwitchTo(eq) && !_testHistory.canRequestCheckRide(eq))
						i.remove();
				}
				
				// Save the equipment types we can get promoted to
				ctx.setAttribute("eqSwitch", activeEQ, REQUEST);
			} else {
				ctx.setAttribute("txreq", txreq, REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Figure out the image to display
		Map acImgs = (Map) SystemData.getObject("pcImages");
		ctx.setAttribute("acImage", acImgs.get(p.getEquipmentType().toLowerCase()), REQUEST);

		// Redirect to the home page
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pilotCenter.jsp");
		result.setSuccess(true);
	}
}