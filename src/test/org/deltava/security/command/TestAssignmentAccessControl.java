// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.assign.*;

public class TestAssignmentAccessControl extends AccessControlTestCase {

   private AssignmentAccessControl _ac;
   private AssignmentInfo _a;

   public static Test suite() {
      return new CoverageDecorator(TestAssignmentAccessControl.class, new Class[] { AccessControl.class,
            AssignmentAccessControl.class });
   }

   @Override
protected void setUp() throws Exception {
      super.setUp();
      _a = new AssignmentInfo("B737-800");
      _ac = new AssignmentAccessControl(_ctxt, _a);
   }

   @Override
protected void tearDown() throws Exception {
      _ac = null;
      _a = null;
      super.tearDown();
   }

   public void testAccess() throws Exception {
      _ac.validate();
      
      assertFalse(_ac.getCanCreateAvailable());
      assertFalse(_ac.getCanDelete());
      assertFalse(_ac.getCanRelease());
      assertFalse(_ac.getCanReserve());
      
      //_user.addRole("Pilot");
      //_ac.validate();
   }
   
   public void testContextValidation() {
      doContextValidation(new AssignmentAccessControl(null, _a));
   }
}