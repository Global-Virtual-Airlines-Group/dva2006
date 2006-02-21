// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.register;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Applicant;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display applicants.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ApplicantListCommand extends AbstractViewCommand {
	
	private static final List LETTERS = Arrays.asList(new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
			"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"});

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Initialize the view context
		ViewContext vc = initView(ctx);
		
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
			List results = null;
			if (ctx.getParameter("status") != null) {
				int statusCode = StringUtils.arrayIndexOf(Applicant.STATUS, ctx.getParameter("status"));
				results = dao.getByStatus((statusCode == -1) ? Applicant.PENDING : statusCode, "CREATED DESC");
			} else if (ctx.getParameter("eqType") != null) {
				results = dao.getByEquipmentType(ctx.getParameter("eqType"));
			} else if (!StringUtils.isEmpty(ctx.getParameter("letter"))) {
				results = dao.getByLetter(ctx.getParameter("letter"));
			} else {
				results = dao.getByStatus(Applicant.PENDING, "CREATED");
			}

			// Save the results
			vc.setResults(results);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/register/applicantList.jsp");
		result.setSuccess(true);
	}
}