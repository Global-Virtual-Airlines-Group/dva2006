// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

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
   
   /**
    * Initializes the Task.
    */
   public AssignmentPurgeTask() {
      super("Assignment Purge", AssignmentPurgeTask.class);
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
			Connection con = getConnection();
			
			// Get the assignments
		   GetAssignment rdao = new GetAssignment(con);
		   List<AssignmentInfo> assignments = rdao.getByStatus(AssignmentInfo.RESERVED);
		   
		   // Check the open assignments
		   GetPilot pdao = new GetPilot(con);
		   SetAssignment wdao = new SetAssignment(con);
		   for (Iterator<AssignmentInfo> i = assignments.iterator(); i.hasNext(); ) {
		      AssignmentInfo a = i.next();
		      if (cld.getTime().after(a.getAssignDate())) {
		         Pilot usr = pdao.get(a.getPilotID());
		         
		         // If the assignment is repeatable, then release it - otherwise delete it
		         if (a.isRepeating()) {
		            log.warn("Releasing Assignment " + a.getID() + " reserved by " + usr.getName());
		            wdao.reset(a);
		         } else {
		            log.warn("Deleting Assignment " + a.getID() + " reserved by " + usr.getName());
		            wdao.delete(a);
		         }
		      } else {
		         log.debug("Skipping Assignment " + a.getID() + ", assigned on " + a.getAssignDate());
		      }
		   }
		} catch (DAOException de) {
		   log.error(de.getMessage(), de);
		} finally {
			release();
		}

		log.info("Completed");
   }
}