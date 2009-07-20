// Copyright 2005, 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.fleet.*;
import org.deltava.beans.system.AirlineInformation;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.FleetEntryAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to support editing Fleet/Document Library entries.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public abstract class LibraryEditCommand extends AbstractFormCommand {

	private static final List<String> DOC_TYPES = Arrays.asList("fleet", "manual", "newsletter");
	private static final List<String> JSP_HDR = Arrays.asList("installer", "manual", "newsletter");
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

		boolean isNew = "NEW".equalsIgnoreCase(fName);
		LibraryEntry entry = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the library entry
			GetDocuments dao = new GetDocuments(con);
			if ("manual".equals(docType)) {
				String db = SystemData.get("airline.db");
				
				// Load the manual
				if (!isNew)
					entry = dao.getManual(fName, db);
				
				// Load Academy certifications
				GetAcademyCertifications cdao = new GetAcademyCertifications(con);
				ctx.setAttribute("certs", cdao.getAll(), REQUEST);
				
				// If we're new, load all manual titles
				if (isNew) {
					Collection<String> manualNames = new HashSet<String>();
					Map<?, ?> airlines = (Map<?, ?>) SystemData.getObject("apps");
					for (Iterator<?> i = airlines.values().iterator(); i.hasNext(); ) {
						AirlineInformation aInfo = (AirlineInformation) i.next();
						Collection<Manual> docs = dao.getManuals(aInfo.getDB());
						for (Manual mn : docs)
							manualNames.add(mn.getFileName().toLowerCase());
					}
					
					ctx.setAttribute("manualNames", manualNames, REQUEST);
				}
			} else if (!isNew && ("newsletter".equals(docType)))
				entry = dao.getNewsletter(fName, SystemData.get("airline.db"));
			else if (!isNew)
				entry = dao.getInstaller(fName, SystemData.get("airline.db"));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Check our access level
		FleetEntryAccessControl access = new FleetEntryAccessControl(ctx, entry);
		access.validate();
		if (isNew && !access.getCanCreate())
			throw securityException("Cannot create Library Entry");
		else if (!isNew && !access.getCanEdit())
			throw securityException("Cannot edit Library Entry for " + fName);

		// Save the entry in the request
		ctx.setAttribute("entry", entry, REQUEST);
		ctx.setAttribute("access", access, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
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