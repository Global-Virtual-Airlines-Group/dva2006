// Copyright 2005, 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.system.TransferRequest;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for equipment program Transfer Requests.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class TransferAccessControl extends AccessControl {

   private TransferRequest _treq;
   
   private boolean _canAssignRide;
   private boolean _canToggleRatings;
   private boolean _canApprove;
   private boolean _canReject;
   private boolean _canDelete;
   
   /**
    * Initialize the Access Controller.
    * @param ctx the security context
    * @param treq the Transfer Request
    */
   public TransferAccessControl(SecurityContext ctx, TransferRequest treq) {
      super(ctx);
      _treq = treq;
   }
   
   /**
    * Calculates access rights.
    * @throws AccessControlException if the Transfer Request cannot be viewed
    */
   public void validate() throws AccessControlException {
      validateContext();
      
      // Set role status
      boolean isMine = (_ctx.isAuthenticated() && (_ctx.getUser().getID() == _treq.getID()));
      boolean isHR = _ctx.isUserInRole("HR");
      boolean isOps = _ctx.isUserInRole("Operations");
      boolean isExam = isHR || isOps || _ctx.isUserInRole("Examination");
      boolean isPIREP = isHR || isOps ||_ctx.isUserInRole("PIREP");
      if (!isExam && !isPIREP && !isHR && !isPIREP && !isMine)
         throw new AccessControlException("Cannot view Transfer Request");

      // Set access rights
      int status = _treq.getStatus();
      _canToggleRatings = (isMine || isExam || isPIREP);
      _canApprove = (status == TransferRequest.OK) && isPIREP;
      _canAssignRide = (status == TransferRequest.PENDING) && isExam;
      _canReject = isPIREP || isExam;
      _canDelete = isHR || isOps || (isMine && (status != TransferRequest.ASSIGNED));
   }

   /**
    * Returns if the Transfer Request can be approved and the Pilot transferred.
    * @return TRUE if the Pilot can be transferred, otherwise FALSE
    */
   public boolean getCanApprove() {
      return _canApprove;
   }
   
   /**
    * Returns if this Transfer Request can be canceled and deleted.
    * @return TRUE if the Request can be rejected, otherwise FALSE
    */
   public boolean getCanReject() {
      return _canReject;
   }
   
   /**
    * Returns if a new Check Ride can be assigned.
    * @return TRUE if the user can assign a Check Ride, otherwise FALSE
    */
   public boolean getCanAssignRide() {
      return _canAssignRide;
   }
   
   /**
    * Returns if the user can toggle the Ratings only flag.
    * @return TRUE if the ratings flag can be changed, otherwise FALSE
    */
   public boolean getCanToggleRatings() {
	   return _canToggleRatings;
   }
   
   /**
    * Returns if the Transfer Request can be deleted without cancellation.
    * @return TRUE if the Request can be deleted, otherwise FALSE
    */
   public boolean getCanDelete() {
	   return _canDelete;
   }
}