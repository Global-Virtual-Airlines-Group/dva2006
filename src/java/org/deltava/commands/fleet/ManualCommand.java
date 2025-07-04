// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2014, 2015, 2016, 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.io.File;
import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.fleet.*;
import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.file.WriteBuffer;

import org.deltava.mail.*;
import org.deltava.security.command.ManualAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update Document Library entries.
 * @author Luke
 * @version 10.4
 * @since 1.0
 */

public class ManualCommand extends LibraryEditCommand {
	
	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		super.execEdit(ctx, "manual");
	}

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the file name and if we are saving a new document
		String fName = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (fName == null);

		// Get the uploaded file
		FileUpload mFile = ctx.getFile("file", 0);
		if (isNew && (mFile == null))
			throw notFoundException("No Manual Uploaded");
		else if (isNew && (mFile != null))
			fName = mFile.getName();

		// Check if we notify people
		boolean noNotify = Boolean.parseBoolean(ctx.getParameter("noNotify"));

		// Create the Message Context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		List<? extends EMailAddress> pilots = null;
		Manual entry = null;
		try {
			Connection con = ctx.getConnection();

			// Get the Library entry
			GetDocuments dao = new GetDocuments(con);
			entry = dao.getManual(fName, ctx.getDB());
			Manual oe = BeanUtils.clone(entry);

			// Check our access level
			ManualAccessControl access = new ManualAccessControl(ctx, null);
			access.setEntry(entry);
			access.validate();
			boolean ourAccess = isNew ? access.getCanCreate() : access.getCanEdit();
			if (!ourAccess)
				throw securityException("Cannot create/edit Document Library entry");

			// Check if we're uploading to ensure that the file does not already exist
			if (isNew) {
				if (entry != null)
					throw new CommandException("Document " + fName + " already exists");
				
				File f = new File(SystemData.get("path.library"), fName);
				entry = new Manual(f);
				ctx.setAttribute("fileAdded", Boolean.TRUE, REQUEST);
			} else if (entry == null)
				throw new IllegalStateException("No Library Entry to modify");

			// Populate fields from the request
			entry.setDescription(ctx.getParameter("desc"));
			entry.setName(ctx.getParameter("title"));
			entry.setVersion(StringUtils.parse(ctx.getParameter("version"), 1));
			entry.setShowOnRegister(Boolean.parseBoolean(ctx.getParameter("showRegister")));
			entry.setSecurity(Security.valueOf(ctx.getParameter("security")));
			entry.setLastModified(Instant.now());
			if (mFile != null)
				entry.setSize(mFile.getBuffer().length);
			
			// Populate Flight Academy Certifications
			boolean hasCerts = Boolean.parseBoolean(ctx.getParameter("hasCerts"));
			if (hasCerts)
				entry.addCertifications(ctx.getParameters("certNames", Collections.emptySet()));
			
			// Set public field
			entry.setIgnoreCertifcations(Boolean.parseBoolean(ctx.getParameter("ignoreCerts")) && (!entry.getCertifications().isEmpty()));
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(oe, entry, "lastModified");
			AuditLog ae = AuditLog.create(entry, delta, ctx.getUser().getID());

			// Get the message template
			if (!noNotify) {
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("LIBUPDATE"));
				mctxt.addData("manual", entry);
			}

			// Get the pilots to notify
			GetPilotNotify pdao = new GetPilotNotify(con);
			pilots = pdao.getNotifications(Notification.FLEET);
			
			// Start the transaction
			ctx.startTX();

			// Get the write DAO and update the database
			SetLibrary wdao = new SetLibrary(con);
			wdao.write(entry, isNew);
			writeAuditLog(ctx, ae);

			// Dump the uploaded file to the filesystem
			if (mFile != null) {
				WriteBuffer fsdao = new WriteBuffer(entry.file());
				fsdao.write(mFile.getBuffer());
			}
			
			// Commit
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