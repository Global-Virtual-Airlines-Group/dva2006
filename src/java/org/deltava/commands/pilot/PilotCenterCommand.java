package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.system.SystemData;

/**
 * Web site command to display the Pilot Center.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotCenterCommand extends AbstractTestHistoryCommand {
	
	private static int _scheduleSize = 0;

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
			
			// Save the pilot location
			ctx.setAttribute("geoLocation", pdao.getLocation(p.getID()), REQUEST);
			
			// Check our access level
			PilotAccessControl access = new PilotAccessControl(ctx, p);
			access.validate();
			ctx.setAttribute("access", access, REQUEST);

			// Load all PIREPs and save the latest PIREP as a separate bean in the request
			GetFlightReports frdao = new GetFlightReports(con);
			List results = frdao.getByPilot(p.getID(), "DATE DESC");
			if (results.size() > 0)
				ctx.setAttribute("lastFlight", results.get(0), REQUEST);
			
			// Get online hours
			GetFlightReports prdao = new GetFlightReports(con);
			prdao.getOnlineTotals(p);
			
			// Get the schedule size if uncached
			if (_scheduleSize == 0) {
				GetSchedule sdao = new GetSchedule(con);
				_scheduleSize = sdao.getFlightCount();
			}
			
			// Save the schedule size
			ctx.setAttribute("scheduleSize", new Integer(_scheduleSize), REQUEST);

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
				Collection activeEQ = eqdao.getActive();
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
		if (acImgs != null)
		   ctx.setAttribute("acImage", acImgs.get(p.getEquipmentType().toLowerCase()), REQUEST);

		// Redirect to the home page
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pilotCenter.jsp");
		result.setSuccess(true);
	}
}