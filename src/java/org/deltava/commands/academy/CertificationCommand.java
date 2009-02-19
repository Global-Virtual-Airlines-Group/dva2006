// Copyright 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.academy.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CertificationAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to view and update Flight Academy certification profiles.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class CertificationCommand extends AbstractFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Get the certification name
		String name = (String) ctx.getCmdParameter(ID, null);
		try {
			Connection con = ctx.getConnection();
			
			// Check our access
			CertificationAccessControl access = new CertificationAccessControl(ctx);
			access.validate();
			
			// If we're saving an existing cert, get it
			Certification cert = null;
			if (name != null) {
				GetAcademyCertifications dao = new GetAcademyCertifications(con);
				cert = dao.get(name);
				if (cert == null)
					throw notFoundException("Unknown Certification - " + name);

				// Check our access
				if (!access.getCanEdit())
					throw securityException("Cannot edit Certification");
				
				cert.setName(ctx.getParameter("name"));
			} else {
				// Check our access
				if (!access.getCanCreate())
					throw securityException("Cannot create Certification");

				cert = new Certification(ctx.getParameter("name"));
			}
			
			// Update from the request
			cert.setCode(ctx.getParameter("code"));
			cert.setStage(Integer.parseInt(ctx.getParameter("stage")));
			cert.setReqs(StringUtils.arrayIndexOf(Certification.REQ_NAMES, ctx.getParameter("preReqs")));
			cert.setActive(Boolean.valueOf(ctx.getParameter("isActive")).booleanValue());
			cert.setAutoEnroll(Boolean.valueOf(ctx.getParameter("autoEnroll")).booleanValue());
			
			// Load the examination names
			Collection<String> eNames = ctx.getParameters("reqExams");
			cert.setExams(eNames);
			
			// Get the write DAO and save the certification
			SetAcademy wdao = new SetAcademy(con);
			if (name != null)
				wdao.update(cert, name);
			else
				wdao.write(cert);
			
			// Save the certification in the request
			ctx.setAttribute("cert", cert, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attributes
		ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
		ctx.setAttribute("isNew", Boolean.valueOf(name == null), REQUEST);
		
		// Foward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/academy/certUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Get the certification name
		String name = (String) ctx.getCmdParameter(ID, null);
		
		try {
			Connection con = ctx.getConnection();

			// Check our access
			CertificationAccessControl access = new CertificationAccessControl(ctx);
			access.validate();
			
			// Get the DAO and the certification
			if (name != null) {
				GetAcademyCertifications dao = new GetAcademyCertifications(con);
				Certification cert = dao.get(name);
				if (cert == null)
					throw notFoundException("Unknown Certification - " + name);
				
				// Check our access
				if (!access.getCanEdit())
					throw securityException("Cannot edit Certification");
				
				ctx.setAttribute("cert", cert, REQUEST);
			} else if (!access.getCanCreate())
				throw securityException("Cannot create Certification");
			
			// Get available examinations
			GetExamProfiles exdao = new GetExamProfiles(con);
			ctx.setAttribute("exams", exdao.getExamProfiles(true), REQUEST); 
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save prerequisite choices
		ctx.setAttribute("preReqNames", ComboUtils.fromArray(Certification.REQ_NAMES), REQUEST);
		
		// Foward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/certEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when reading the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execRead(CommandContext ctx) throws CommandException {

		// Get the certification name
		String name = (String) ctx.getCmdParameter(ID, null);
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Certification
			GetAcademyCertifications dao = new GetAcademyCertifications(con);
			Certification cert = dao.get(name);
			if (cert == null)
				throw notFoundException("Unknown Certification - " + name);

			// Check our access - this'll blow up if we cannot view
			CertificationAccessControl access = new CertificationAccessControl(ctx);
			access.validate();
			
			// Get associated documents
			GetDocuments ddao = new GetDocuments(con);
			ctx.setAttribute("docs", ddao.getByCertification(SystemData.get("airline.db"), cert.getName()), REQUEST);

			// Save in the request
			ctx.setAttribute("cert", cert, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Foward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/certView.jsp");
		result.setSuccess(true);
	}
}