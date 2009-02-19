// Copyright 2005, 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.io.File;
import java.util.List;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.fleet.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.file.WriteBuffer;
import org.deltava.mail.*;

import org.deltava.security.command.FleetEntryAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update Document Library entries.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class ManualCommand extends LibraryEditCommand {
	
	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {
		super.execEdit(ctx, "manual");
	}

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the file name and if we are saving a new document
		String fName = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (fName == null);

		// Get the uploaded file
		FileUpload mFile = ctx.getFile("file");
		if (isNew && (mFile == null))
			throw notFoundException("No Manual Uploaded");
		else if (isNew && (mFile != null))
			fName = mFile.getName();

		// Check if we notify people
		boolean noNotify = Boolean.valueOf(ctx.getParameter("noNotify")).booleanValue();

		// Create the Message Context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		List<? extends EMailAddress> pilots = null;
		Manual entry = null;
		try {
			Connection con = ctx.getConnection();

			// Get the Library entry
			GetDocuments dao = new GetDocuments(con);
			entry = dao.getManual(fName, SystemData.get("airline.db"));

			// Check our access level
			FleetEntryAccessControl access = new FleetEntryAccessControl(ctx, entry);
			access.validate();
			boolean ourAccess = (isNew) ? access.getCanCreate() : access.getCanEdit();
			if (!ourAccess)
				throw securityException("Cannot create/edit Document Library entry");

			// Check if we're uploading to ensure that the file does not already exist
			if (isNew && (entry != null))
				throw new CommandException("Document " + fName + " already exists");
			else if (isNew) {
				File f = new File(SystemData.get("path.library"), fName);
				entry = new Manual(f.getPath());
				ctx.setAttribute("fileAdded", Boolean.TRUE, REQUEST);
			}

			// Populate fields from the request
			entry.setDescription(ctx.getParameter("desc"));
			entry.setName(ctx.getParameter("title"));
			entry.setVersion(StringUtils.parse(ctx.getParameter("version"), 1));
			entry.addCertifications(ctx.getParameters("certNames"));
			entry.setShowOnRegister(Boolean.valueOf(ctx.getParameter("showRegister")).booleanValue());
			entry.setSecurity(StringUtils.arrayIndexOf(LibraryEntry.SECURITY_LEVELS, ctx.getParameter("security")));
			entry.setLastModified(new java.util.Date());
			if (mFile != null)
				entry.setSize(mFile.getBuffer().length);

			// Get the message template
			if (!noNotify) {
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("LIBUPDATE"));
				mctxt.addData("manual", entry);
			}

			// Get the pilots to notify
			GetPilotNotify pdao = new GetPilotNotify(con);
			pilots = pdao.getNotifications(Person.FLEET);

			// Start the transaction
			ctx.startTX();

			// Get the write DAO and update the database
			SetLibrary wdao = new SetLibrary(con);
			wdao.write(entry, isNew);

			// Dump the uploaded file to the filesystem
			if (mFile != null) {
				WriteBuffer fsdao = new WriteBuffer(entry.file());
				fsdao.write(mFile.getBuffer());
			}

			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attributes
		ctx.setAttribute("library", "Document", REQUEST);
		ctx.setAttribute("librarycmd", "doclibrary", REQUEST);

		// Send notification
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