// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
import org.deltava.beans.StatusUpdate;
import org.deltava.beans.system.MessageTemplate;

import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.taskman.DatabaseTask;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to disable Users who have not logged in within a period of time.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InactivityUpdateTask extends DatabaseTask {
   
   private static final Logger log = Logger.getLogger(InactivityUpdateTask.class);

   /**
    * Initializes the Schedued Task.
    */
   public InactivityUpdateTask() {
      super("Inactivity Status Update");
   }

   /**
    * Executes the task.
    */
   protected void execute() {
      
      // Get the inactivity cutoff time
      int inactiveDays = SystemData.getInt("users.inactive_days");
      Calendar cld = Calendar.getInstance();
      cld.add(Calendar.DATE, inactiveDays * -1);
      log.info("Executing");

      try {
         SetStatusUpdate sudao = new SetStatusUpdate(_con);
         SetPilot pwdao = new SetPilot(_con);
         
         // Get the Message template
         GetMessageTemplate mtdao = new GetMessageTemplate(_con);
         MessageTemplate mt = mtdao.get("USERINACTIVE");
         
         // Figure out who we're operating as
         GetPilotNotify dao = new GetPilotNotify(_con);
         Pilot taskBy = dao.getByName(SystemData.get("users.tasks_by"));
         
         // Get the pilots to deactivate
         List pilots = dao.getPilotsByLastLogin(cld.getTime());
         for (Iterator i = pilots.iterator(); i.hasNext(); ) {
            Pilot p = (Pilot) i.next();
            log.info("Marking " + p.getName() + " Inactive");
            
            // Create the StatusUpdate bean
            StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
            upd.setAuthorID(taskBy.getID()); 
            upd.setCreatedOn(new Date());
            upd.setDescription("Marked Inactive due to no logins within " + inactiveDays + " days");
            sudao.write(upd);
            
            // Create the Message Context
            MessageContext mctxt = new MessageContext();
            mctxt.setTemplate(mt);
            mctxt.addData("user", taskBy);
            mctxt.addData("pilot", p);
            
            // Deactivate the Pilot
            p.setStatus(Pilot.INACTIVE);
            pwdao.write(p);
            
            // Send notification message
    		Mailer mailer = new Mailer(taskBy);
    		mailer.setContext(mctxt);
    		mailer.send(p);
         }
      } catch (DAOException de) {
         log.error(de.getMessage());
      }
      
      log.info("Completed");
   }
}