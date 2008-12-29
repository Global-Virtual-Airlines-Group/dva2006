// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.awt.*;
import java.awt.image.*;

import java.io.*;
import java.sql.Connection;

import javax.imageio.ImageIO;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.ImageInfo;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update a Pilot's Water Cooler signature image.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class SignatureUpdateCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result
		CommandResult result = ctx.getResult();
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Pilot
			GetPilot dao = new GetPilot(con);
			Pilot p = dao.get(ctx.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot - " + ctx.getID());

			// Check our access
			PilotAccessControl access = new PilotAccessControl(ctx, p);
			access.validate();
			if (!access.getCanChangeSignature())
				throw securityException("Cannot Update Signature Image");
			
			// Save the pilot in the request
			ctx.setAttribute("pilot", p, REQUEST);
			
			// If not image uploaded, then redirect
			FileUpload imgData = ctx.getFile("coolerImg");
			if (imgData == null) {
				ctx.release();
				result.setURL("/jsp/pilot/sigUpdate.jsp");
				result.setSuccess(true);
				return;
			}

			// Check the image
			ImageInfo info = new ImageInfo(imgData.getBuffer());
			if (!info.check()) {
				ctx.release();
				ctx.setMessage("Invalid Image format");
				result.setSuccess(true);
				return;
			}

			// Check the image dimensions
			boolean isHR = ctx.isUserInRole("HR");
			int maxX = SystemData.getInt("cooler.sig_max.x");
			int maxY = SystemData.getInt("cooler.sig_max.y");
			if (!isHR && ((info.getWidth() > maxX) || (info.getHeight() > maxY))) {
				ctx.release();
				ctx.setMessage("Your Signature Image is too large. (Max = " + maxX + "x" + maxY + ", Yours = "
						+ info.getWidth() + "x" + info.getHeight());
				return;
			}

			// Check the image size
			int maxSize = SystemData.getInt("cooler.sig_max.size");
			if (!isHR && (imgData.getSize() > maxSize)) {
				ctx.release();
				ctx.setMessage("Your signature Image is too large. (Max = " + maxSize + "bytes, Yours ="
						+ imgData.getSize() + " bytes");
				return;
			}

			// Check if signature is authorized
			boolean isAuth = (isHR || ctx.isUserInRole("Signature")) && Boolean.valueOf(ctx.getParameter("isAuth")).booleanValue();
				
			// Write the data
			SetSignatureImage wdao = new SetSignatureImage(con);
			if (isAuth) {
				BufferedImage img = ImageIO.read(imgData.getInputStream());
				Graphics2D g = img.createGraphics();
				g.setColor(Color.WHITE);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.225f));
				g.drawString("Approved Signature", img.getWidth() - 120, img.getHeight() - 4);
				g.dispose();

				// Create the new Image
				ByteArrayOutputStream os = new ByteArrayOutputStream(16384);
				ImageIO.write(img, "png", os);
				img.flush();
				p.load(os.toByteArray());
				wdao.write(p, img.getWidth(), img.getHeight(), "png", isAuth);
			} else {
				p.load(imgData.getBuffer());
				wdao.write(p, info.getWidth(), info.getHeight(), info.getFormatName(), isAuth);
			}
		} catch (IOException ie) {
			throw new CommandException(ie);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status variable for the result JSP
		ctx.setAttribute("sigUpdated", Boolean.TRUE, REQUEST);

		// Forward to the update JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/pilotUpdate.jsp");

		// Forward to the JSP
		result.setSuccess(true);
	}
}