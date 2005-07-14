// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util;

import java.util.*;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

public class TestRoleUtils extends TestCase {

   public static Test suite() {
      return new CoverageDecorator(TestRoleUtils.class, new Class[] { RoleUtils.class } );
  }

   public void testAccess() {
      
      // Initialize roles
      List adminUserRoles = Arrays.asList(new String[] {"Admin", "HR", "Fleet"});
      List userRoles = Arrays.asList(new String[] {"HR", "Fleet"});
      
      List r1Roles = Arrays.asList(new String[] {"Fleet", "PIREP" });
      List r2Roles = Arrays.asList(new String[] {"PIREP" });
      List r3Roles = Arrays.asList(new String[] {"*"});
      
      // Test access when we have admin role
      assertTrue(RoleUtils.hasAccess(adminUserRoles, r1Roles));
      assertTrue(RoleUtils.hasAccess(adminUserRoles, r2Roles));
      assertTrue(RoleUtils.hasAccess(adminUserRoles, r3Roles));
      
      // Test access when we don't have admin role
      assertTrue(RoleUtils.hasAccess(userRoles, r1Roles));
      assertFalse(RoleUtils.hasAccess(userRoles, r2Roles));
      assertTrue(RoleUtils.hasAccess(userRoles, r3Roles));
      
      // Test access with empty list
      assertFalse(RoleUtils.hasAccess(Collections.EMPTY_LIST, r2Roles));
      assertTrue(RoleUtils.hasAccess(Collections.EMPTY_LIST, r3Roles));
   }
}