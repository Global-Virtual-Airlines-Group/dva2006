// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import java.sql.Connection;

import javax.imageio.ImageIO;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to approve a Pilot's Water Cooler signature image.
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public class SignatureApproveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the signature
			GetImage idao = new GetImage(con);
			boolean isAuth = idao.isSignatureAuthorized(ctx.getID());
			
			// Load the Pilot profile
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getID());
			ctx.setAttribute("pilot", p, REQUEST);
			
			// If not authorized, give it the "seal of approval"
			if (!isAuth) {
				InputStream in = new ByteArrayInputStream(idao.getSignatureImage(ctx.getID(), SystemData.get("airline.db")));
				BufferedImage img = ImageIO.read(in);
				Graphics2D g = img.createGraphics();
				g.setColor(Color.WHITE);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.225f));
				g.drawString("Approved Signature", img.getWidth() - 120, img.getHeight() - 4);
				g.dispose();

				// Save the new Image
				ByteArrayOutputStream os = new ByteArrayOutputStream(16384);
				ImageIO.write(img, "png", os);
				img.flush();
				p.load(os.toByteArray());
				
				// Save the data
				SetSignatureImage swdao = new SetSignatureImage(con);
				swdao.write(p, img.getWidth(), img.getHeight(), "png", isAuth);
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

		// Forward to the JSP
		CommandResult result = ctx.getResult();
        result.setType(ResultType.REQREDIRECT);
        result.setURL("/jsp/pilot/pilotUpdate.jsp");
		result.setSuccess(true);
	}
}