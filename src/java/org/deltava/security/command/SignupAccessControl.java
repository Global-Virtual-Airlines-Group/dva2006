// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.event.*;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Online Event signups.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SignupAccessControl extends AccessControl {

   private Event _ev;
   private Signup _su;
   
   private boolean _canRelease;
   
   /**
    * Initializes the Access Controller.
    * @param ctx the Command context
    * @param e the Online Event
    * @param s the Signup
    */
   public SignupAccessControl(SecurityContext ctx, Event e, Signup s) {
      super(ctx);
      _ev = e;
      _su = s;
   }

   /**
    * Calculates access levels.
    */
   public void validate() {
      validateContext();
      
      // If no data provided, abort
      if ((_ev == null) || (_su == null))
         return;

      // Do nothing if we're not authenticated
      if (!_ctx.isAuthenticated())
         return;
      
      // Calculate state variables
      boolean isMine = (_su.getPilotID() == _ctx.getUser().getID());
      boolean isEventOK = (_ev.getStatus() == Event.OPEN) || (_ev.getStatus() == Event.CLOSED);
      
      // Set access rights
      _canRelease = (isMine || _ctx.isUserInRole("Event")) && isEventOK; 
   }

   /**
    * Returns if this Signup can be released or canceled.
    * @return TRUE if the Signup can be canceled, otherwise FALSE
    */
   public boolean getCanRelease() {
      return _canRelease;
   }
}