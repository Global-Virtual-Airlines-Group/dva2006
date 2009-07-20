// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.Pilot;
import org.deltava.beans.assign.AssignmentInfo;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Flight Assignments.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class AssignmentAccessControl extends AccessControl {

   private AssignmentInfo _assign;
   
   private boolean _canCreateAvailable;
   private boolean _canReserve;
   private boolean _canRelease;
   private boolean _canDelete;
   
   /**
    * Initializes the Access Controller.
    * @param ctx the Command context
    * @param ai the Assignment Info object
    */
   public AssignmentAccessControl(SecurityContext ctx, AssignmentInfo ai) {
      super(ctx);
      _assign = ai;
   }

   /**
	 * Calculates access rights.
	 */
   public void validate() {
      validateContext();
      
      // Calculate generic access
      _canCreateAvailable = _ctx.isUserInRole("Operations") || _ctx.isUserInRole("Schedule");
      if ((!_ctx.isUserInRole("Pilot")) || (_assign == null))         
         return;
      
      // Get user object for ratings
      Pilot p = (Pilot) _ctx.getUser();
      
      // Get state variables
      boolean isMine = (_assign.getPilotID() == p.getID());
      boolean isAvailable = (_assign.getStatus() == AssignmentInfo.AVAILABLE);
      boolean isAssignAdmin = _canCreateAvailable || (_assign.getEventID() != 0 && _ctx.isUserInRole("Event")); 
      
      // Calculate access roles
      _canReserve = isAvailable && (p.hasRating(_assign.getEquipmentType()));
      _canRelease = (_assign.getStatus() == AssignmentInfo.RESERVED) && (isMine || isAssignAdmin);
      _canDelete = isAvailable && _ctx.isUserInRole("Admin");
   }
   
   /**
    * Returns if the user can create available Flight Assignments (assignments for other people).
    * @return TRUE if the user can create avilable Assignments, otherwise FALSE
    */
   public boolean getCanCreateAvailable() {
      return _canCreateAvailable;
   }

   /**
    * Returns if the Assignment can be reserved.
    * @return TRUE if the Assignment can be reserved, otherwise FALSE
    */
   public boolean getCanReserve() {
      return _canReserve;
   }
   
   /**
    * Returns if the Assignment can be released.
    * @return TRUE if the Assignment can be released, otherwise FALSE
    */
   public boolean getCanRelease() {
      return _canRelease;
   }
   
   /**
    * Returns if the Assignment can be deleted.
    * @return TRUE if the Assignment can be deleted, otherwise FALSE
    */
   public boolean getCanDelete() {
      return _canDelete;
   }
}