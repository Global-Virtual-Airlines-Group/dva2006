// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

import org.deltava.beans.fleet.LibraryEntry;

/**
 * An Access Controller to support Fleet/File Library entry operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FleetEntryAccessControl extends AccessControl {

   protected LibraryEntry _entry;
   
   protected boolean _canView;
   private boolean _canEdit;
   private boolean _canDelete;
   private boolean _canCreate;

   /**
    * Initializes the access controller.
    * @param ctx the Command context
    */
   public FleetEntryAccessControl(SecurityContext ctx, LibraryEntry e) {
      super(ctx);
      _entry = e;
   }

   /**
    * Updates the Fleet Entry to validate access to.
    * @param e the entry
    */
   public void setEntry(LibraryEntry e) {
      _entry = e;
   }

   /**
    * Calculates access rights.
    */
   public void validate() {
      validateContext();
      _canCreate = _ctx.isUserInRole("Fleet");
      
      // Check for null entry
      if (_entry == null) return;

      // Set access variables
      _canEdit = _ctx.isUserInRole("Fleet");
      _canDelete = _ctx.isUserInRole("Admin") || _ctx.isUserInRole("HR");
      switch (_entry.getSecurity()) {
         case LibraryEntry.PUBLIC:
            _canView = true;
            break;

         case LibraryEntry.AUTH_ONLY:
            _canView = _ctx.isAuthenticated();
            break;

         default:
         case LibraryEntry.STAFF_ONLY:
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