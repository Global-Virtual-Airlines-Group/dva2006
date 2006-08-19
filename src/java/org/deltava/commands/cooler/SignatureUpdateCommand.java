// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.beans.FileUpload;
import org.deltava.beans.Pilot;

import org.deltava.dao.GetPilot;
import org.deltava.dao.SetSignatureImage;
import org.deltava.dao.DAOException;

import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.ImageInfo;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update a Pilot's Water Cooler signature image.
 * @author Luke
 * @version 1.0
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
      result.setURL("/jsp/pilot/sigUpdate.jsp");
      
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
         
         // Load the signature Image, get the write DAO and update the image
         FileUpload imgData = ctx.getFile("coolerImg");
         if (imgData != null) {
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
               ctx.setMessage("Your Signature Image is too large. (Max = " + maxX + "x" + maxY + ", Yours = " +
                     info.getWidth() + "x" + info.getHeight());
               return;
            }
            
            // Check the image size
            int maxSize = SystemData.getInt("cooler.sig_max.size");
            if (!isHR && (imgData.getSize() > maxSize)) {
               ctx.release();
               ctx.setMessage("Your signature Image is too large. (Max = " + maxSize + "bytes, Yours =" +
                     imgData.getSize() + " bytes");
               return;
            }
            
            // Write the signature image to the database
            SetSignatureImage wdao = new SetSignatureImage(con);
            p.load(imgData.getBuffer());
            wdao.write(p, info.getWidth(), info.getHeight(), info.getFormatName());
         
            // Set status variable for the result JSP
            ctx.setAttribute("sigUpdated", Boolean.TRUE, REQUEST);

            // Forward to the update JSP
            result.setType(CommandResult.REQREDIRECT);
            result.setURL("/jsp/pilot/pilotUpdate.jsp");
         }
         
         // Save the pilot in the request
         ctx.setAttribute("pilot", p, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      result.setSuccess(true);
   }
}