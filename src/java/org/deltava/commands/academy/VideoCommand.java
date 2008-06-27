// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.io.File;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.fleet.*;
import org.deltava.beans.academy.TrainingVideo;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.file.WriteBuffer;
import org.deltava.mail.*;

import org.deltava.security.command.CertificationAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to view and update Flight Academy Training videos.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class VideoCommand extends AbstractFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the file name and if we are saving a new document
		String fName = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (fName == null);
		
		// Check our access
		CertificationAccessControl access = new CertificationAccessControl(ctx);
		access.validate();
		boolean canExec = isNew ? access.getCanCreateVideo() : access.getCanEditVideo();
		if (!canExec)
			throw securityException("Cannot create/edit Training Video");
		
		// Get the uploaded file - look for a file
		FileUpload mFile = ctx.getFile("file");
		if (isNew && (mFile == null)) {
			File f = new File(SystemData.get("path.video"), ctx.getParameter("fileName"));
			if (f.exists())
				fName = f.getName();
			
			if (fName == null)
				throw notFoundException("No Training Video Uploaded/Specified");
		} else if (isNew && (mFile != null)) {
			fName = mFile.getName();
		}

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

			// Check if we're uploading to ensure that the file does not already exist
			if (isNew && (v != null)) {
				throw new CommandException("Document " + fName + " already exists");
			} else if (isNew) {
				File f = new File(SystemData.get("path.video"), fName);
				video = new TrainingVideo(f.getPath());
				video.setAuthorID(ctx.getUser().getID());
				video.setSize(f.length());
				ctx.setAttribute("fileAdded", Boolean.TRUE, REQUEST);
			} else if (v != null) {
				video = new TrainingVideo(v);
				video.setCertifications(dao.getCertifications(fName));
			}

			// Populate fields from the request
			video.setCategory(ctx.getParameter("category"));
			video.setDescription(ctx.getParameter("desc"));
			video.setName(ctx.getParameter("title"));
			video.setCertifications(ctx.getParameters("certNames"));
			video.setSecurity(StringUtils.arrayIndexOf(LibraryEntry.SECURITY_LEVELS, ctx.getParameter("security")));
			if (mFile != null)
				video.setSize(mFile.getSize());

			// Get the message template
			if (!noNotify) {
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("TVLIBUPDATE"));
				mctxt.addData("video", video);
			}
			
			// Get the pilots to notify
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			pilots.addAll(pdao.getByRole("HR", SystemData.get("airline.db")));
			pilots.addAll(pdao.getByRole("Instructor", SystemData.get("airline.db")));

			// Start the transaction
			ctx.startTX();

			// Get the write DAO and update the database
			SetLibrary wdao = new SetLibrary(con);
			wdao.write(video);
			
			// Write the certifications
			SetAcademy awdao = new SetAcademy(con);
			awdao.writeCertifications(video);

			// Dump the uploaded file to the filesystem
			if (mFile != null) {
				WriteBuffer fsdao = new WriteBuffer(video.file());
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
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/fleet/libraryUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
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
		
		// Set security options attribute
		ctx.setAttribute("securityOptions", ComboUtils.fromArray(LibraryEntry.SECURITY_LEVELS), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/fleet/videoEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when reading the form. <i>NOT IMPLEMENTED</i>
	 * @param ctx the Command context
	 * @throws UnsupportedOperationException always
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		throw new UnsupportedOperationException();
	}
}