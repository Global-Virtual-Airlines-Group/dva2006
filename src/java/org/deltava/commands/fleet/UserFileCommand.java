// Copyright 2005, 2006, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.io.File;
import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.fleet.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.file.WriteBuffer;

import org.deltava.security.command.FileEntryAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to support editing the User File Library.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class UserFileCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the File Entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		// Check if we're uploading a file, that the name is unique
		FileUpload fu = ctx.getFile("file");
		if (fu != null) {
			File f = new File(SystemData.get("path.userfiles"), fu.getName());
			FileEntry entry = new FileEntry(f);
			if (entry.getSize() != 0)
				throw securityException(fu.getName() + " already exists");
		}

		// Get the file name
		String fName = (fu != null) ? fu.getName() : (String) ctx.getCmdParameter(ID, null);
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and load the file
			GetLibrary dao = new GetLibrary(con);
			FileEntry entry = dao.getFile(fName);

			// Check our access
			FileEntryAccessControl access = new FileEntryAccessControl(ctx, entry);
			access.validate();
			boolean isOK = (entry == null) ? access.getCanCreate() : access.getCanEdit();
			if (!isOK)
				throw securityException("Cannot create/edit File Library entry");

			// Create a new bean if we need to
			if (entry == null) {
				File f = new File(SystemData.get("path.userfiles"), fName);
				entry = new FileEntry(f);
				entry.setAuthorID(ctx.getUser().getID());
				ctx.setAttribute("fileAdded", Boolean.TRUE, REQUEST);
			}

			// Populate the bean
			entry.setName(ctx.getParameter("title"));
			entry.setCategory(ctx.getParameter("category"));
			entry.setDescription(ctx.getParameter("desc"));
			entry.setSecurity(Security.valueOf(ctx.getParameter("security")));

			// Start a JDBC transaction
			ctx.startTX();

			// Create the file dao and write the file
			if (fu != null) {
				WriteBuffer fwdao = new WriteBuffer(entry.file());
				fwdao.write(fu.getBuffer());
			}

			// Write the entry
			SetLibrary wdao = new SetLibrary(con);
			wdao.write(entry);

			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attribute
		ctx.setAttribute("isFile", Boolean.TRUE, REQUEST);
		ctx.setAttribute("library", "User File", REQUEST);
		ctx.setAttribute("librarycmd", "filelibrary", REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/fleet/libraryUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the File Entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {

		String fName = (String) ctx.getCmdParameter(ID, null);
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the file
			GetLibrary dao = new GetLibrary(con);
			FileEntry entry = dao.getFile(fName);
			if ((entry == null) && !StringUtils.isEmpty(fName))
				throw notFoundException("Invalid Filename - " + fName);

			// Check our access
			FileEntryAccessControl access = new FileEntryAccessControl(ctx, entry);
			access.validate();
			boolean isOK = (entry == null) ? access.getCanCreate() : access.getCanEdit();
			if (!isOK)
				throw securityException("Cannot create/edit File Library entry - " + entry);

			// Save the entry and the access controller
			ctx.setAttribute("entry", entry, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/fleet/fileEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the File Entry.
	 * @param ctx the Command context
	 * @throws UnsupportedOperationException always
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}