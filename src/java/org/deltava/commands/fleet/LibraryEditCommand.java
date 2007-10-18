// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.fleet.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.FleetEntryAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to support editing Fleet/Document Library entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class LibraryEditCommand extends AbstractFormCommand {

	private static final List<String> DOC_TYPES = Arrays.asList(new String[] { "fleet", "manual", "newsletter" });
	private static final List<String> JSP_HDR = Arrays.asList(new String[] { "installer", "manual", "newsletter" });
	private static final Map<String, String> JSP_NAMES = CollectionUtils.createMap(DOC_TYPES, JSP_HDR);
	
	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @param docType the document type
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execEdit(CommandContext ctx, String docType) throws CommandException {

		// Get the file name, or if we're creating a new file
		String fName = (String) ctx.getCmdParameter(ID, "NEW");
		ctx.setAttribute("securityOptions", ComboUtils.fromArray(LibraryEntry.SECURITY_LEVELS), REQUEST);

		// Get the command results
		CommandResult result = ctx.getResult();

		// If we're creating a new entry, check our access
		if ("NEW".equals(fName)) {
			FleetEntryAccessControl access = new FleetEntryAccessControl(ctx, null);
			access.validate();
			if (!access.getCanCreate())
				throw securityException("Cannot create Library Entry");

			// Save the access controller
			ctx.setAttribute("access", access, REQUEST);

			// Forward to the JSP
			result.setURL("/jsp/fleet/" + JSP_NAMES.get(docType) + "Edit.jsp");
			result.setSuccess(true);
			return;
		}

		LibraryEntry entry = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the library entry
			GetDocuments dao = new GetDocuments(con);
			if ("manual".equals(docType)) {
				String db = SystemData.get("airline.db");
				
				// Load the manual
				Manual m = dao.getManual(fName, db);
				GetAcademyCertifications cdao = new GetAcademyCertifications(con);
				ctx.setAttribute("certs", cdao.getAll(), REQUEST);

				// Save the entry
				entry = m;
			} else if ("newsletter".equals(docType))
				entry = dao.getNewsletter(fName, SystemData.get("airline.db"));
			else
				entry = dao.getInstaller(fName, SystemData.get("airline.db"));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Check our access level
		FleetEntryAccessControl access = new FleetEntryAccessControl(ctx, entry);
		access.validate();
		if (!access.getCanEdit())
			throw securityException("Cannot edit Library Entry for " + fName);

		// Save the entry in the request
		ctx.setAttribute("entry", entry, REQUEST);
		ctx.setAttribute("access", access, REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/fleet/" + JSP_NAMES.get(docType) + "Edit.jsp");
		result.setSuccess(true);
	}
	
	/**
     * Method called when reading the form. <i>NOT IMPLEMENTED</i>
     * @param ctx the Command Context
     * @throws UnsupportedOperationException always
     */
	protected final void execRead(CommandContext ctx) throws CommandException {
		throw new UnsupportedOperationException();
	}
}