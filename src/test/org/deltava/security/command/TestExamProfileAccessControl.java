// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

public class TestExamProfileAccessControl extends AccessControlTestCase {

   private ExamProfileAccessControl _ac;

   public static Test suite() {
      return new CoverageDecorator(TestExamProfileAccessControl.class, new Class[] { AccessControl.class,
            ExamProfileAccessControl.class });
   }

   @Override
protected void setUp() throws Exception {
      super.setUp();
      _ac = new ExamProfileAccessControl(_ctxt, null);
   }

   @Override
protected void tearDown() throws Exception {
      _ac = null;
      super.tearDown();
   }

   public void testAccess() throws Exception {
      _user.addRole("Examination");
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanEdit());
      
      _user.addRole("HR");
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertTrue(_ac.getCanEdit());
   }
   
   public void testAnonymousAccess() throws Exception {
      _ctxt.logoff();
      _ac.validate();
      assertFalse(_ac.getCanRead());
   }
   
   @SuppressWarnings("static-method")
public void testContextValidation() {
      doContextValidation(new ExamProfileAccessControl(null, null));
   }
}