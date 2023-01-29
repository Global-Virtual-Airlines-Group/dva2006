// Copyright 2017, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.io.File;
import java.sql.Connection;

import org.deltava.beans.acars.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to upload ACARS installers.
 * @author Luke
 * @version 10.4
 * @since 7.5
 */

public class InstallerUploadCommand extends AbstractFormCommand {
	
	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Get the file name
		String fName = (String) ctx.getCmdParameter(Command.ID, null);
		
		// Build client version
		int version = StringUtils.parse(ctx.getParameter("version"), 3); boolean isBeta = Boolean.parseBoolean(ctx.getParameter("isBeta"));
		int beta = isBeta ? StringUtils.parse(ctx.getParameter("beta"), 0) : 0;
		ClientInfo info = new ClientInfo(version, StringUtils.parse(ctx.getParameter("build"), 0), beta);
		info.setClientType(EnumUtils.parse(ClientType.class, ctx.getParameter("clientType"), ClientType.PILOT));
		String newFile = String.format("%s-%s-%d-%sInc.exe", SystemData.get("airline.code"), info.getClientType().getFilePrefix(), Integer.valueOf(info.getVersion()), info.isBeta() ? "Beta" : "");
				
		File nf = new File(SystemData.get("path.inc"), newFile);
		File f = new File(SystemData.get("path.upload"), ctx.getParameter("id"));
		if (f.exists())
			fName = f.getName();
		if (fName == null)
			throw notFoundException("No Installer Uploaded");
		
		try {
			Connection con = ctx.getConnection();
			
			// Start transaction
			ctx.startTX();

			// Update the build number
			SetACARSBuilds bwdao = new SetACARSBuilds(con);
			bwdao.setLatest(info, false);
			
			// Copy the file
			if (nf.exists()) nf.delete();
			if (!f.renameTo(nf))
				throw new DAOException("Cannot move " + f.getAbsolutePath() + " to " + nf.getAbsolutePath());
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attributes
		ctx.setAttribute("newInfo", info, REQUEST);
		ctx.setAttribute("fileName", newFile, REQUEST);
		ctx.setAttribute("library", "Fleet", REQUEST);
		ctx.setAttribute("librarycmd", "fleetlibrary", REQUEST);
		ctx.setAttribute("libraryop", "admin", REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/fleet/libraryUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
	
	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		ClientInfo inf = new ClientInfo(3, 0);
		inf.setClientType(EnumUtils.parse(ClientType.class, ctx.getParameter("clientType"), ClientType.PILOT));
		try {
			GetACARSBuilds bdao = new GetACARSBuilds(ctx.getConnection());
			ctx.setAttribute("latest", bdao.getLatestBeta(inf), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();	
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/fleet/installerUpload.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}