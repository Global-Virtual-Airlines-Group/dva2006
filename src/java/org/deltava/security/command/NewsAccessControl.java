// Copyright 2005, 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

import org.deltava.beans.News;
import org.deltava.beans.Notice;

/**
 * An Access Controller for System News and NOTAMs.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class NewsAccessControl extends AccessControl {

   private final News _nws;
   
   private boolean _canCreateNews;
   private boolean _canCreateNOTAM;
   private boolean _canEdit;
   private boolean _canSave;
   private boolean _canDelete;
   
   /**
    * Initializes the Access Controller.
    * @param ctx the Command context
    * @param nws the System News/NOTAM entry
    */
   public NewsAccessControl(SecurityContext ctx, News nws) {
      super(ctx);
      _nws = nws;
   }

   /**
    * Calculates access rights.
    */
   @Override
   public void validate() {
      validateContext();
      
      // Set access variables
      _canCreateNews = _ctx.isUserInRole("News");
      _canCreateNOTAM = _ctx.isUserInRole("NOTAM");
      
      // If no news/notam entry defined, don't go farther
      if (_nws == null) {
         _canSave = _canCreateNews || _canCreateNOTAM;
         return;
      }

      // Figure out what we have
      if (_nws instanceof Notice) {
         _canEdit = _ctx.isUserInRole("HR") || _canCreateNOTAM;
      } else {
         _canEdit = _ctx.isUserInRole("HR");
      }
      
      _canSave = _canEdit;
      _canDelete = _ctx.isUserInRole("Admin") && (_nws.getID() != 0);
   }
   
   /**
    * Returns if a new System News entry can be created.
    * @return TRUE if a News entry can be created, otherwise FALSE
    */
   public boolean getCanCreateNews() {
      return _canCreateNews;
   }
   
   /**
    * Returns if a new Notice to Airmen (NOTAM) can be created.
    * @return TRUE if a NOTAM can be created, otherwise FALSE
    */
   public boolean getCanCreateNOTAM() {
      return _canCreateNOTAM;
   }
   
   /**
    * Returns if the System News/Notice entry can be edited.
    * @return TRUE if the entry can be edited, otherwise FALSE
    */
   public boolean getCanEdit() {
      return _canEdit;
   }
   
   /**
    * Returns if the System News/Notice entry can be saved.
    * @return TRUE if the entry can be saved, otherwise FALSE
    */
   public boolean getCanSave() {
      return _canSave;
   }
   
   /**
    * Returns if the System News/Notice entry can be deleted.
    * @return TRUE if the entry can be deleted, otherwise FALSE
    */
   public boolean getCanDelete() {
      return _canDelete;
   }
}