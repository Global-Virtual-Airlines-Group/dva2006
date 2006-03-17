// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.Pilot;
import org.deltava.beans.testing.CheckRideScript;

import org.deltava.security.SecurityContext;

/**
 * An access controller for Check Ride scripts
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CheckrideScriptAccessControl extends AccessControl {
   
   private CheckRideScript _sc;
   
   private boolean _canCreate;
   private boolean _canEdit;
   private boolean _canDelete;

   /**
    * Initializes the access controller.
    * @param ctx the security context
    * @param sc the CheckRideScript bean to validate
    */
   public CheckrideScriptAccessControl(SecurityContext ctx, CheckRideScript sc) {
      super(ctx);
      _sc = sc;
   }

   /**
    * Calculates access rights.
    * @throws AccessControlException if we cannot read the script
    */
   public void validate() throws AccessControlException {
	   validateContext();

      // Do nothing if we are not in the exam role
      if (!_ctx.isUserInRole("Examination"))
    	  throw new AccessControlException("Cannot view Check Ride script");
      
      // Check creation/deletion access
      _canCreate = true;
      _canDelete = _ctx.isUserInRole("HR");
      if (_sc == null) {
         _canEdit = true;
         return;
      }

      // Allow edits if we are in the same eq program
      Pilot usr = (Pilot) _ctx.getUser();
      _canEdit = _ctx.isUserInRole("HR") || (usr.getEquipmentType().equals(_sc.getProgram()));
   }
   
   /**
    * Returns wether a new Check Ride script can be created.
    * @return TRUE if a script can be created, otherwise FALSE
    */
   public boolean getCanCreate() {
      return _canCreate;
   }

   /**
    * Returns wether the Check Ride script can be edited.
    * @return TRUE if the script can be edited, otherwise FALSE
    */
   public boolean getCanEdit() {
      return _canEdit;
   }
   
   /**
    * Returns wether the Check Ride script can be deleted.
    * @return TRUE if the script can be deleted, otherwise FALSE
    */
   public boolean getCanDelete() {
      return _canDelete;
   }
}