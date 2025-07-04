// Copyright 2005, 2006, 2007, 2011, 2012, 2014, 2015, 2016, 2017, 2020, 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 10.4
 * @since 1.0
 */

public class InstallerCommand extends LibraryEditCommand {
	
	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		super.execEdit(ctx, "fleet");
	}

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
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
		boolean noNotify = Boolean.parseBoolean(ctx.getParameter("noNotify"));
		List<? extends EMailAddress> pilots = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Library entry
			GetLibrary dao = new GetLibrary(con);
			Installer entry = dao.getInstaller(fName, ctx.getDB());
			Installer oi = BeanUtils.clone(entry);

			// Check our access level
			FleetEntryAccessControl access = new FleetEntryAccessControl(ctx, entry);
			access.validate();
			boolean ourAccess = (isNew) ? access.getCanCreate() : access.getCanEdit();
			if (!ourAccess)
				throw securityException("Cannot create/edit Fleet Library entry");

			// Check if we're uploading to ensure that the file does not already exist
			if (isNew) {
				if (entry != null)
					throw notFoundException("Installer " + fName + " already exists");
				
				File f = new File(SystemData.get("path.library"), fName);
				entry = new Installer(f);
				ctx.setAttribute("fileAdded", Boolean.TRUE, REQUEST);
			} else if (entry == null)
				throw new IllegalStateException("No Library Entry to modify");

			// Populate fields from the request
			entry.setDescription(ctx.getParameter("desc"));
			entry.setName(ctx.getParameter("title"));
			entry.setCode(ctx.getParameter("code"));
			entry.setImage(ctx.getParameter("img"));
			entry.setSecurity(Security.valueOf(ctx.getParameter("security")));
			entry.setVersion(StringUtils.parse(ctx.getParameter("majorVersion"), 1), StringUtils.parse(ctx.getParameter("minorVersion"), 0), StringUtils.parse(ctx.getParameter("subVersion"), 0));
			
			// Add Simulator Codes
			final Installer e = entry;
			Collection<String> fsCodes = ctx.getParameters("fsVersion");
			if (fsCodes != null) {
				e.getFSVersions().clear();
				fsCodes.forEach(sim -> e.addFSVersion(Simulator.fromName(sim, Simulator.UNKNOWN)));
			}

			// Add airline codes
			Collection<String> appCodes = ctx.getParameters("airlines");
			if (appCodes != null) {
				e.getApps().clear();
				appCodes.stream().map(c -> SystemData.getApp(c)).filter(Objects::nonNull).forEach(e::addApp);
			}
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(oi, entry);
			AuditLog ae = AuditLog.create(entry, delta, ctx.getUser().getID());
			
			// Get the message template
			if (!noNotify) {
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("FLEETUPDATE"));
				mctxt.addData("installer", entry);
			}

			// Get the pilots to notify
			if (!noNotify) {
				GetPilotNotify pdao = new GetPilotNotify(con);
				pilots = pdao.getNotifications(Notification.FLEET);
			}
			
			// Start transaction
			ctx.startTX();

			// Get the write DAO and update the database
			SetLibrary wdao = new SetLibrary(con);
			wdao.write(entry);
			writeAuditLog(ctx, ae);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
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
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/fleet/libraryUpdate.jsp");
		result.setSuccess(true);
	}
}