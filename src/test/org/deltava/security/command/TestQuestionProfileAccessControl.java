// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

public class TestQuestionProfileAccessControl extends AccessControlTestCase {

   private QuestionProfileAccessControl _ac;
   
   public static Test suite() {
      return new CoverageDecorator(TestQuestionProfileAccessControl.class, new Class[] { AccessControl.class,
            QuestionProfileAccessControl.class });
   }
   
   protected void setUp() throws Exception {
      super.setUp();
      _ac = new QuestionProfileAccessControl(_ctxt, null);
   }

   protected void tearDown() throws Exception {
      _ac = null;
      super.tearDown();
   }

   public void testAccess() throws Exception {
	   try {
		   _ac.validate();
		   fail("AccessControlException expected");
	   } catch (AccessControlException ace) {
		// empty
	   }
      
      assertFalse(_ac.getCanRead());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
      
      _user.addRole("Examination");
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());

      _user.addRole("HR");
      _user.removeRole("Examination");
      _ac.validate();

      assertTrue(_ac.getCanRead());
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
      
      _user.addRole("Admin");
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
   }
   
   public void testContextValidation() {
      doContextValidation(new QuestionProfileAccessControl(null, null));
   }
}