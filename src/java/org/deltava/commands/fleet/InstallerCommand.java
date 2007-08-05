// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.util.*;
import java.io.File;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.fleet.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.FleetEntryAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update Fleet Library entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstallerCommand extends LibraryEditCommand {

	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {
		ctx.setAttribute("fsVersions", ComboUtils.fromArray(Installer.FS_NAMES, Installer.FS_CODES), REQUEST);
		super.execEdit(ctx, "fleet");
	}

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the file name and if we are saving a new document
		String fName = (String) ctx.getCmdParameter(ID, null);
		boolean isNew = (fName == null);
		if (isNew)
			fName = ctx.getParameter("fileName");

		// Create the Message Context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		// Check if we notify people
		boolean noNotify = Boolean.valueOf(ctx.getParameter("noNotify")).booleanValue();

		List<? extends EMailAddress> pilots = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Library entry
			GetLibrary dao = new GetLibrary(con);
			Installer entry = dao.getInstaller(fName, SystemData.get("airline.db"));

			// Check our access level
			FleetEntryAccessControl access = new FleetEntryAccessControl(ctx, entry);
			access.validate();
			boolean ourAccess = (isNew) ? access.getCanCreate() : access.getCanEdit();
			if (!ourAccess)
				throw securityException("Cannot create/edit Fleet Library entry");

			// Check if we're uploading to ensure that the file does not already exist
			if (isNew && (entry != null)) {
				throw notFoundException("Installer " + fName + " already exists");
			} else if (isNew) {
				File f = new File(SystemData.get("path.library"), fName);
				entry = new Installer(f.getAbsolutePath());
				ctx.setAttribute("fileAdded", Boolean.TRUE, REQUEST);
			}

			// Populate fields from the request
			entry.setDescription(ctx.getParameter("desc"));
			entry.setName(ctx.getParameter("title"));
			entry.setCode(ctx.getParameter("code"));
			entry.setImage(ctx.getParameter("img"));
			entry.setSecurity(StringUtils.arrayIndexOf(LibraryEntry.SECURITY_LEVELS, ctx.getParameter("security")));
			entry.setVersion(StringUtils.parse(ctx.getParameter("majorVersion"), 1), StringUtils.parse(ctx
					.getParameter("minorVersion"), 0), StringUtils.parse(ctx.getParameter("subVersion"), 0));
			
			// Add FS Codes
			Collection<String> fsCodes = ctx.getParameters("fsVersion");
			if (fsCodes != null)
				entry.setFSVersions(StringUtils.listConcat(fsCodes, ","));

			// Add airline codes
			Collection<String> appCodes = ctx.getParameters("airlines");
			if (appCodes != null) {
				entry.getApps().clear();
				for (Iterator<String> i = appCodes.iterator(); i.hasNext();) {
					String appCode = i.next();
					entry.addApp(SystemData.getApp(appCode));
				}
			}
			
			// Get the message template
			if (!noNotify) {
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("FLEETUPDATE"));
				mctxt.addData("installer", entry);
			}

			// Get the pilots to notify
			if (!noNotify) {
				GetPilotNotify pdao = new GetPilotNotify(con);
				pilots = pdao.getNotifications(Person.FLEET);
			}

			// Get the write DAO and update the database
			SetLibrary wdao = new SetLibrary(con);
			wdao.write(entry);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attributes
		ctx.setAttribute("library", "Fleet", REQUEST);
		ctx.setAttribute("librarycmd", "fleetlibrary", REQUEST);
		ctx.setAttribute("libraryop", "admin", REQUEST);

		// Send the email message
		if (!noNotify) {
			Mailer mailer = new Mailer(ctx.getUser());
			mailer.setContext(mctxt);
			mailer.send(pilots);
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/fleet/libraryUpdate.jsp");
		result.setSuccess(true);
	}
}