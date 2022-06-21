// Copyright 2006, 2010, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.academy.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CertificationAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to update Flight Academy Certification requirements.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public class CertificationRequirementsCommand extends AbstractFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		String name = (String) ctx.getCmdParameter(ID, "");
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the certification
			GetAcademyCertifications dao = new GetAcademyCertifications(con);
			Certification cert = dao.get(name);
			if (cert == null)
				throw notFoundException("Unknown Certification - " + name);
			
			// Check our access
			CertificationAccessControl access = new CertificationAccessControl(ctx);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot update requirements");

			// Load the certifications from the request
			Collection<CertificationRequirement> reqs = new TreeSet<CertificationRequirement>();
			int reqNumber = 0;
			while (reqNumber >= 0) {
				String txt = ctx.getParameter("reqText" + String.valueOf(++reqNumber));
				if (StringUtils.isEmpty(txt)) {
					reqNumber = -1;
					break;
				}
				
				// Get the exam
				String examName = ctx.getParameter("reqExam" + String.valueOf(reqNumber));
				if ((examName != null) && !cert.getExamNames().contains(examName))
					examName = null;
				
				// Create the requirement bean
				CertificationRequirement req = new CertificationRequirement(reqNumber);
				req.setExamName(examName);
				req.setText(txt);
				reqs.add(req);
			}
			
			// Save the requirements
			cert.setRequirements(reqs);
			
			// Get the DAO and update the Certification
			SetAcademyCertification wdao = new SetAcademyCertification(con);
			wdao.update(cert, cert.getName());
			
			// Save the certification in the request
			ctx.setAttribute("cert", cert, REQUEST);
			ctx.setAttribute("updateReqs", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Determine if we want to add more requirements - Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setSuccess(true);
		if (Boolean.parseBoolean(ctx.getParameter("doMore"))) {
			result.setURL("certreqs", "edit", name);
			result.setType(ResultType.REDIRECT);
		} else {
			result.setURL("/jsp/academy/certUpdate.jsp");
			result.setType(ResultType.REQREDIRECT);
		}
	}

	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {

		String name = (String) ctx.getCmdParameter(ID, "");
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the certification
			GetAcademyCertifications dao = new GetAcademyCertifications(con);
			Certification cert = dao.get(name);
			if (cert == null)
				throw notFoundException("Unknown Certification - " + name);
			
			// Check our access
			CertificationAccessControl access = new CertificationAccessControl(ctx);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot update requirements");

			// Save the certification in the request
			ctx.setAttribute("cert", cert, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/certReqEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when reading the form.
	 * @param ctx the Command context
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}