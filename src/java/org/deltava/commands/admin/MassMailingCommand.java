// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import javax.activation.*;
import javax.mail.util.ByteArrayDataSource;

import org.deltava.beans.FileUpload;
import org.deltava.beans.EMailAddress;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to send group e-mail messages.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class MassMailingCommand extends AbstractCommand {
   
   private static final String ALL_ACTIVE = "$ALL$";

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result and equipment type to process
		CommandResult result = ctx.getResult();
		String eqType = ctx.getParameter("eqType");

		// If we're just executing the command and not in the HR role, get our equipment type and return
		if ((eqType == null) && (!ctx.getRequest().isUserInRole("HR"))) {
			Collection<String> eqTypes = new HashSet<String>();
			eqTypes.add(ctx.getUser().getEquipmentType());
			ctx.setAttribute("eqTypes", eqTypes, REQUEST);
			result.setURL("/jsp/admin/massMail.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Initialize the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());
		mctxt.addData("body", ctx.getParameter("body"));
		
		// Check if we're sending to a role
		boolean isRole = (eqType != null) && (eqType.startsWith("$role_"));
		
		List<? extends EMailAddress> pilots = new ArrayList<EMailAddress>();
		Collection<Object> eqTypes = null;
		try {
			Connection con = ctx.getConnection();

			// Get a list of equipment types
			GetEquipmentType eqdao = new GetEquipmentType(con);
			eqTypes = new ArrayList<Object>(eqdao.getActive());
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("MASSMAIL"));
			
			// Check if we're sending to a different equipment type
			if ((!ctx.getRequest().isUserInRole("HR")) && (!eqType.equals(ctx.getUser().getEquipmentType())))
				throw securityException("Equipment Type " + eqType + " != " + ctx.getUser().getEquipmentType());

			// If we're posting to the command, get the pilots to display
			if (isRole) {
			   GetPilotDirectory dao = new GetPilotDirectory(con);
			   pilots = dao.getByRole(eqType.substring(6), SystemData.get("airline.db"));
			   ctx.setAttribute("eqType", eqType.substring(6), REQUEST);
			   ctx.setAttribute("isRole", Boolean.TRUE, REQUEST);
			} else if (ALL_ACTIVE.equals(eqType)) {
			   GetPilot dao = new GetPilot(con);
			   pilots = dao.getActivePilots(null);
			   ctx.setAttribute("eqType", eqType, REQUEST);
			} else if (eqType != null) {
			   GetPilot dao = new GetPilot(con);
				pilots = dao.getPilotsByEQ(eqType, null, true);
				ctx.setAttribute("eqType", eqType, REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Add the roles to the request with a special marker
		Collection roles = (Collection) SystemData.getObject("security.roles");
		for (Iterator i = roles.iterator(); i.hasNext(); ) {
		   String role = (String) i.next();
		   eqTypes.add(ComboUtils.fromString(role, "$role_" + role));
		}
		
		// Add all users
		eqTypes.add(ComboUtils.fromString("All Users", ALL_ACTIVE));
		
		// Save the choices
		ctx.setAttribute("eqTypes", eqTypes, REQUEST);

		// If we're not sending to anyone, just redirect to the JSP
		if (eqType == null) {
			result.setURL("/jsp/admin/massMail.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Create the e-mail message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		
		// Add an attachment if we have one
		FileUpload file = ctx.getFile("fAttach");
		if (file != null) {
			FileTypeMap typeMap = FileTypeMap.getDefaultFileTypeMap();
			ByteArrayDataSource src = new ByteArrayDataSource(file.getBuffer(), typeMap.getContentType(file.getName()));
			src.setName(file.getName());
			mailer.setAttachment(src);
		}
		
		// Send the message
		mailer.send(pilots);
		
		// Save results
		ctx.setAttribute("msgSent", new Integer(pilots.size()), REQUEST);
		
		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/admin/massMailComplete.jsp");
		result.setSuccess(true);
	}
}