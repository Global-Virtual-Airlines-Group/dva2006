// Copyright 2005 Luke J. Kolin. All Rights Reserved.
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
 * @version 1.0
 * @since 1.0
 */

public class ManualSaveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the file name and if we are saving a new document
		String fName = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (fName == null);

		// Get the uploaded file
		FileUpload mFile = ctx.getFile("file");
		if (isNew && (mFile == null)) {
			throw new CommandException("No Manual Uploaded");
		} else if (mFile != null) {
			fName = mFile.getName();
		}

		// Check if we notify people
		boolean noNotify = Boolean.valueOf(ctx.getParameter("noNotify")).booleanValue();

		// Create the Message Context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		List pilots = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Library entry
			GetLibrary dao = new GetLibrary(con);
			Manual entry = dao.getManual(fName);

			// Check our access level
			FleetEntryAccessControl access = new FleetEntryAccessControl(ctx, entry);
			access.validate();
			boolean ourAccess = (isNew) ? access.getCanCreate() : access.getCanEdit();
			if (!ourAccess)
				throw securityException("Cannot create/edit Document Library entry");

			// Check if we're uploading to ensure that the file does not already exist
			if (isNew && (entry != null)) {
				throw new CommandException("Document " + fName + " already exists");
			} else if (isNew) {
			   File f = new File (SystemData.get("path.library"), fName);
				entry = new Manual(f.getPath());
			}

			// Populate fields from the request
			entry.setDescription(ctx.getParameter("desc"));
			entry.setName(ctx.getParameter("title"));
			entry.setVersion(Integer.parseInt(ctx.getParameter("version")), 0, 0);
			entry.setSecurity(StringUtils.arrayIndexOf(LibraryEntry.SECURITY_LEVELS, ctx.getParameter("security")));
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

			// Get the write DAO and update the database
			SetLibrary wdao = new SetLibrary(con);
			wdao.write(entry);

			// Dump the uploaded file to the filesystem
			if (mFile != null) {
				WriteBuffer fsdao = new WriteBuffer(entry.file());
				fsdao.write(mFile.getBuffer());
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attribute
		ctx.setAttribute(isNew ? "manualAdded" : "manualUpdated", Boolean.TRUE, REQUEST);

		// Send notification
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