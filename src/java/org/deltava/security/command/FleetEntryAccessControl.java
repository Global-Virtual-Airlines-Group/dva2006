// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;
import org.deltava.commands.CommandSecurityException;

import org.deltava.beans.fleet.FleetEntry;

/**
 * An Access Controller to support Fleet Library entry operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public final class FleetEntryAccessControl extends AccessControl {

   private FleetEntry _entry;

   private boolean _canEdit;
   private boolean _canDelete;
   private boolean _canView;
   private boolean _canCreate;

   /**
    * Initializes the access controller.
    * @param ctx the Command context
    */
   public FleetEntryAccessControl(SecurityContext ctx, FleetEntry e) {
      super(ctx);
      _entry = e;
   }

   /**
    * Updates the Fleet Entry to validate access to.
    * @param e the entry
    */
   public void setEntry(FleetEntry e) {
      _entry = e;
   }

   /**
    * Calculates access rights.
    * @throws CommandSecurityException never
    */
   public void validate() throws CommandSecurityException {
      validateContext();
      _canCreate = _ctx.isUserInRole("Fleet");
      
      // Check for null entry
      if (_entry == null) return;

      // Set access variables
      _canEdit = _ctx.isUserInRole("Fleet");
      _canDelete = _ctx.isUserInRole("Admin");
      switch (_entry.getSecurity()) {
         case FleetEntry.PUBLIC:
            _canView = true;
            break;

         case FleetEntry.AUTH_ONLY:
            _canView = _ctx.isAuthenticated();
            break;

         default:
         case FleetEntry.STAFF_ONLY:
            _canView = (_ctx.getRoles().size() > 1);
            break;
      }
   }

   /**
    * Returns if the Fleet/Document Library entry can be edited.
    * @return TRUE if the entry can be edited, otherwise FALSE
    */
   public boolean getCanEdit() {
      return _canEdit;
   }

   /**
    * Returns if the Fleet/Document Library entry can be deleted.
    * @return TRUE if the entry can be deleted, otherwise FALSE
    */
   public boolean getCanDelete() {
      return _canDelete;
   }

   /**
    * Returns if the Fleet/Document Library entry can be viewed.
    * @return TRUE if the entry can be viewed, otherwise FALSE
    */
   public boolean getCanView() {
      return _canView;
   }
   
   /**
    * Returns if a new Fleet/Document Library entry can be created.
    * @return TRUE if a new entry can be created, otherwise FALSE
    */
   public boolean getCanCreate() {
      return _canCreate;
   }
}