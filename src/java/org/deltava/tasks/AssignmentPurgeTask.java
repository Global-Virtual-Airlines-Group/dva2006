// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
import org.deltava.beans.assign.*;
import org.deltava.dao.*;

import org.deltava.taskman.DatabaseTask;
import org.deltava.util.system.SystemData;

/**
 * A Schedule Task to automatically release Flight Assignments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AssignmentPurgeTask extends DatabaseTask {
   
   private static final Logger log = Logger.getLogger(AssignmentPurgeTask.class);

   /**
    * Initializes the Task.
    */
   public AssignmentPurgeTask() {
      super("Assignment Purge");
   }

	/**
	 * Executes the task.
	 */
   protected void execute() {
      
		// Get the inactivity cutoff time
		int inactiveDays = SystemData.getInt("users.inactive_assign");
		Calendar cld = Calendar.getInstance();
		cld.add(Calendar.DATE, inactiveDays * -1);
		log.info("Executing");
		
		try {
		   GetAssignment rdao = new GetAssignment(_con);
		   List assignments = rdao.getByStatus(AssignmentInfo.RESERVED);
		   
		   // Check the open assignments
		   GetPilot pdao = new GetPilot(_con);
		   SetAssignment wdao = new SetAssignment(_con);
		   for (Iterator i = assignments.iterator(); i.hasNext(); ) {
		      AssignmentInfo a = (AssignmentInfo) i.next();
		      if (cld.getTime().before(a.getAssignDate())) {
		         Pilot usr = pdao.get(a.getPilotID());
		         
		         // If the assignment is repeatable, then release it - otherwise delete it
		         if (a.isRepeating()) {
		            log.info("Releasing Assignment " + a.getID() + " reserved by " + usr.getName());
		            wdao.reset(a);
		         } else {
		            log.info("Deleting Assignment " + a.getID() + " reserved by " + usr.getName());
		            wdao.delete(a);
		         }
		      } else {
		         log.debug("Skipping Assignment " + a.getID() + ", assigned on " + a.getAssignDate());
		      }
		   }
		} catch (DAOException de) {
		   log.error(de.getMessage(), de);
		}

		log.info("Completed");
   }
}