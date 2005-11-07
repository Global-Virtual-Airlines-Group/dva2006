// Copyright 2005 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.system.EMailConfiguration;

import org.deltava.security.SecurityContext;
import org.deltava.commands.CommandSecurityException;

/**
 * An Access Controller for mailbox profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MailboxAccessControl extends AccessControl {
   
   private EMailConfiguration _cfg;
   
   private boolean _canChangePassword;
   private boolean _canEdit;
   private boolean _canDelete;

   /**
    * Initializes the Access Controller.
    * @param ctx the command context
    * @param cfg the IMAP configuration bean
    */
   public MailboxAccessControl(SecurityContext ctx, EMailConfiguration cfg) {
      super(ctx);
      _cfg = cfg;
   }

   /**
    * Calculates access rights.
    * @throws CommandSecurityException never
    */
   public void validate() throws CommandSecurityException {
      if ((_cfg == null) || !_ctx.isAuthenticated())
         return;
      
      // Calculate access properties
      _canDelete = _ctx.isUserInRole("Admin");
      _canEdit = _ctx.isUserInRole("Admin");
      _canChangePassword = _canDelete || _canEdit || (_ctx.getUser().getID() == _cfg.getID());
   }
   
   /**
    * Returns if the user can change the mailbox password.
    * @return TRUE if the password can be changed, otherwise FALSE
    */
   public boolean getCanChangePassword() {
      return _canChangePassword; 
   }
   
   /**
    * Returns if the user can edit the mailbox profile.
    * @return TRUE if the profile can be edited, otherwise FALSE
    */
   public boolean getCanEdit() {
      return _canEdit;
   }
   
   /**
    * Returns if the user can delete the mailbox profile.
    * @return TRUE if the profile can be deleted, otherwise FALSE
    */
   public boolean getCanDelete() {
      return _canDelete;
   }
}