// Copyright 2006, 2010, 2014, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.io.File;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.fleet.*;
import org.deltava.beans.academy.TrainingVideo;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.CertificationAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to view and update Flight Academy Training videos.
 * @author Luke
 * @version 7.5
 * @since 1.0
 */

public class VideoCommand extends AbstractFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the file name
		String fName = (String) ctx.getCmdParameter(Command.ID, null);
		
		// Get the uploaded file - look for a file
		File f = new File(SystemData.get("path.upload"), fName);
		if (f.exists())
			fName = f.getName();
		if (fName == null)
			throw notFoundException("No Video Uploaded");

		// Check if we notify people
		boolean noNotify = Boolean.valueOf(ctx.getParameter("noNotify")).booleanValue();

		// Create the Message Context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		Collection<Pilot> pilots = new TreeSet<Pilot>();
		TrainingVideo video = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Training Video
			GetVideos dao = new GetVideos(con);
			Video v = dao.getVideo(fName);
			boolean isNew = (v == null);
			
			// Check our access
			CertificationAccessControl access = new CertificationAccessControl(ctx);
			access.validate();
			boolean canExec = isNew ? access.getCanCreateVideo() : access.getCanEditVideo();
			if (!canExec)
				throw securityException("Cannot create/edit Training Video");

			// Check if we're uploading to ensure that the file does not already exist
			if (isNew) {
				video = new TrainingVideo(new File(SystemData.get("path.video"), fName));
				video.setAuthorID(ctx.getUser().getID());
				ctx.setAttribute("fileAdded", Boolean.TRUE, REQUEST);
			} else {
				video = new TrainingVideo(v);
				video.setCertifications(dao.getCertifications(fName));
			}

			// Populate fields from the request
			video.setCategory(ctx.getParameter("category"));
			video.setDescription(ctx.getParameter("desc"));
			video.setName(ctx.getParameter("title"));
			video.setCertifications(ctx.getParameters("certNames"));
			video.setSecurity(Security.valueOf(ctx.getParameter("security")));

			// Get the message template and pilots to notify
			if (!noNotify) {
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("TVLIBUPDATE"));
				mctxt.addData("video", video);

				GetPilotDirectory pdao = new GetPilotDirectory(con);
				pilots.addAll(pdao.getByRole("HR", SystemData.get("airline.db")));
				pilots.addAll(pdao.getByRole("Instructor", SystemData.get("airline.db")));
				pilots.addAll(pdao.getByRole("AcademyAdmin", SystemData.get("airline.db")));
			}

			// Start the transaction
			ctx.startTX();

			// Get the write DAO and update the database
			SetLibrary wdao = new SetLibrary(con);
			wdao.write(video);
			
			// Write the certifications
			SetAcademy awdao = new SetAcademy(con);
			awdao.writeCertifications(video);
			
			// Delete if exists
			if (video.file().exists())
				video.file().delete();
			
			// Copy the file
			if (!f.renameTo(video.file()))
				throw new DAOException("Cannot move " + f.getAbsolutePath() + " to " + video.file().getAbsolutePath());

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

	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Get file name
		String fName = (String) ctx.getCmdParameter(ID, "NEW");
		boolean isNew = "NEW".equals(fName);
		
		// Check our access
		CertificationAccessControl access = new CertificationAccessControl(ctx);
		access.validate();
		boolean canExec = isNew ? access.getCanCreateVideo() : access.getCanEditVideo();
		if (!canExec)
			throw securityException("Cannot create/edit Training Video");

		// Save the access controller
		ctx.setAttribute("access", access, REQUEST);

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the library entry
			if (!isNew) {
				GetVideos dao = new GetVideos(con);
				Video v = dao.getVideo(fName);
				if (v == null)
					throw notFoundException("Invalid video filename - " + fName);
			
				// Create the proper bean
				TrainingVideo video = new TrainingVideo(v);
				video.setCertifications(dao.getCertifications(fName));
				
				// Save the video in the request 
				ctx.setAttribute("video", video, REQUEST);
			}
			
			// Get the certification options
			GetAcademyCertifications cdao = new GetAcademyCertifications(con);
			ctx.setAttribute("certs", cdao.getAll(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/fleet/videoEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when reading the form.
	 * @param ctx the Command context
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}