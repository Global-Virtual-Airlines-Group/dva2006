// Copyright 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.io.File;
import java.sql.Connection;

import org.deltava.beans.fleet.Video;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CertificationAccessControl;

/**
 * A Web Site Command to delete training Videos.
 * @author Luke
 * @version 5.3
 * @since 5.3
 */

public class VideoDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check our access
		CertificationAccessControl access = new CertificationAccessControl(ctx);
		access.validate();
		if (!access.getCanEditVideo())
			throw securityException("Cannot delete Training Video");

		String fName = (String) ctx.getCmdParameter(ID, null);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the library entry
			GetVideos dao = new GetVideos(con);
			Video v = dao.getVideo(fName);
			if (v == null)
				throw notFoundException("Invalid video filename - " + fName);
			
			// Start the transaction
			ctx.startTX();

			// Get the write DAO and update the database
			SetLibrary wdao = new SetLibrary(con);
			wdao.delete(v);
			
			// Remove from the filesystem
			File f = v.file();
			ctx.setAttribute("entry", v, REQUEST);
			ctx.setAttribute("fileExisted", Boolean.valueOf(f.exists()), REQUEST);
			ctx.setAttribute("isDeleted", Boolean.valueOf(f.delete()), REQUEST);
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attributes
		ctx.setAttribute("library", "Flight Academy Video", REQUEST);
		ctx.setAttribute("librarycmd", "tvideolibrary", REQUEST);
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/fleet/libraryUpdate.jsp");
	}
}