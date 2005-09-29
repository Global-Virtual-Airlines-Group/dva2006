// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.fleet;

import java.io.File;
import java.util.List;
import java.sql.Connection;

import org.deltava.beans.Person;
import org.deltava.beans.fleet.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.FleetEntryAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update Fleet Library entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstallerSaveCommand extends AbstractCommand {
	
   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the file name and if we are saving a new document
      String fName = (String) ctx.getCmdParameter(ID, null);
      boolean isNew = (fName == null);
      if (isNew)
         fName = ctx.getParameter("fileName");
      
      // Create the Message Context
      MessageContext mctxt = new MessageContext();
      mctxt.addData("user", ctx.getUser());

      List pilots = null;
      try {
         Connection con = ctx.getConnection();

         // Get the DAO and the Library entry
         GetLibrary dao = new GetLibrary(con);
         Installer entry = dao.getInstaller(fName);

         // Check our access level
         FleetEntryAccessControl access = new FleetEntryAccessControl(ctx, entry);
         access.validate();
         boolean ourAccess = (isNew) ? access.getCanCreate() : access.getCanEdit();
         if (!ourAccess) throw securityException("Cannot create/edit Fleet Library entry");

         // Check if we're uploading to ensure that the file does not already exist
         if (isNew && (entry != null)) {
            throw new CommandException("Installer " + fName + " already exists");
         } else if (isNew) {
            File f = new File(SystemData.get("path.library"), fName);
            entry = new Installer(f.getAbsolutePath());
         }

         // Populate fields from the request
         entry.setDescription(ctx.getParameter("desc"));
         entry.setName(ctx.getParameter("title"));
         entry.setCode(ctx.getParameter("code"));
         entry.setImage(ctx.getParameter("img"));
         entry.setSecurity(StringUtils.arrayIndexOf(FleetEntry.SECURITY_LEVELS, ctx.getParameter("security")));
         entry.setVersion(Integer.parseInt(ctx.getParameter("majorVersion")), Integer.parseInt(ctx.getParameter("minorVersion")),
               Integer.parseInt(ctx.getParameter("subVersion")));
         
         // Get the message template
         GetMessageTemplate mtdao = new GetMessageTemplate(con);
         mctxt.setTemplate(mtdao.get("FLEETUPDATE"));
         mctxt.addData("installer", entry);
         
         // Get the pilots to notify
         GetPilotNotify pdao = new GetPilotNotify(con);
         pilots = pdao.getNotifications(Person.FLEET);

         // Get the write DAO and update the database
         SetLibrary wdao = new SetLibrary(con);
         if (isNew) {
            wdao.createInstaller(entry);
            ctx.setAttribute("installerAdded", Boolean.TRUE, REQUEST);
         } else {
            wdao.updateInstaller(entry);
            ctx.setAttribute("installerUpdated", Boolean.TRUE, REQUEST);
         }
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Send the email message
      Mailer mailer = new Mailer(ctx.getUser());
      mailer.setContext(mctxt);
      mailer.send(pilots);

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REQREDIRECT);
      result.setURL("/jsp/fleet/libraryUpdate.jsp");
      result.setSuccess(true);
   }
}