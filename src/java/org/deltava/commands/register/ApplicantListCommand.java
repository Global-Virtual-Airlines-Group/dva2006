// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.Applicant;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display applicants.
 * @author Luke
 * @version 6.3
 * @since 1.0
 */

public class ApplicantListCommand extends AbstractViewCommand {
	
	private static final List<String> LETTERS = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", 
			"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Initialize the view context
		ViewContext vc = initView(ctx);
		boolean isQueue = false;
		
		// Set combobox options
		ctx.setAttribute("letters", LETTERS, REQUEST);
		ctx.setAttribute("statuses", ComboUtils.fromArray(Applicant.STATUS), REQUEST);

		try {
			Connection con = ctx.getConnection();
			
			// Get available equipment types
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqTypes", eqdao.getAll(), REQUEST);

			// Get the DAO and the applicants
			GetApplicant dao = new GetApplicant(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			
			// Figure out which method to call
			List<Applicant> results = null;
			if (ctx.getParameter("status") != null) {
				int statusCode = StringUtils.arrayIndexOf(Applicant.STATUS, ctx.getParameter("status"));
				results = dao.getByStatus((statusCode == -1) ? Applicant.PENDING : statusCode, "CREATED DESC");
			} else if (ctx.getParameter("eqType") != null)
				results = dao.getByEquipmentType(ctx.getParameter("eqType"));
			else if (!StringUtils.isEmpty(ctx.getParameter("letter")))
				results = dao.getByLetter(ctx.getParameter("letter"));
			else {
				isQueue = true;
				results = dao.getByStatus(Applicant.PENDING, "CREATED DESC");
			}
			
			// Get the applicant/pilot IDs
			Collection<Integer> IDs = results.stream().map(Applicant::getID).collect(Collectors.toSet());
			Collection<Integer> pIDs = results.stream().filter(a -> (a.getPilotID() > 0)).map(Applicant::getPilotID).collect(Collectors.toSet());
			
			// Load the questionnaires
			GetExam exdao = new GetExam(con);
			GetQuestionnaire qdao = new GetQuestionnaire(con);
			ctx.setAttribute("qMap", qdao.getByID(IDs), REQUEST);
			ctx.setAttribute("pqMap", exdao.getQuestionnaires(pIDs), REQUEST);
			
			// Load address validation entries
			if (isQueue) {
				Map<Integer, Boolean> addrOK = new HashMap<Integer, Boolean>();
				GetAddressValidation avdao = new GetAddressValidation(con);
				for (Integer id : IDs)
					addrOK.put(id, Boolean.valueOf(avdao.isValid(id.intValue())));
			
				ctx.setAttribute("addrValid", addrOK, REQUEST);
			}
			
			// Load the airline size
			GetStatistics stdao = new GetStatistics(con);
			ctx.setAttribute("airlineSize", Integer.valueOf(stdao.getActivePilots(SystemData.get("airline.db"))), REQUEST);

			vc.setResults(results);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/register/" + (isQueue ? "applicantQueue.jsp" : "applicantList.jsp"));
		result.setSuccess(true);
	}
}