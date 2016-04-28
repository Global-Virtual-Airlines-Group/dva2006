// Copyright 2005, 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Approach Charts. 
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ChartAccessControl extends AccessControl {
   
   private boolean _canCreate;
   private boolean _canEdit;
   private boolean _canDelete;

   /**
    * Initializes the Access Controller.
    * @param ctx the Command context
    */
   public ChartAccessControl(SecurityContext ctx) {
      super(ctx);
   }

   /**
    * Calculates access rights.
    * @throws AccessControlException if the user is unauthenticated
    */
   @Override
   public void validate() throws AccessControlException {
	   validateContext();
      
      // Check if we're logged in
      if (!_ctx.isAuthenticated())
         throw new AccessControlException("Not Authorized");
      
      // Check if we can create charts
      _canCreate = _ctx.isUserInRole("Charts");
      _canEdit = _canCreate;
      _canDelete = _ctx.isUserInRole("Admin");
   }

   /**
    * Returns if a new Approach Chart can be created.
    * @return TRUE if a new Chart can be created, otherwise FALSE
    */
   public boolean getCanCreate() {
      return _canCreate;
   }
   
   /**
    * Returns if an Approach Chart can be edited.
    * @return TRUE if the Chart can be edited, otherwise FALSE
    */
   public boolean getCanEdit() {
      return _canEdit;
   }
   
   /**
    * Returns if an Approach Chart can be deleted.
    * @return TRUE if the Chart can be deleted, otherwise FALSE
    */
   public boolean getCanDelete() {
      return _canDelete;
   }
}