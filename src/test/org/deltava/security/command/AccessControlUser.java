// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import java.util.*;

import org.deltava.beans.Person;

public class AccessControlUser extends Person {
   
   private List<String> _roles = new ArrayList<String>();
   
   public AccessControlUser(String fName, String lName) {
      super(fName, lName);
      _roles.add("User");
   }
   
   public String getRowClassName() {
      return null;
   }
   
   public boolean isInRole(String roleName) {
      return (_roles.contains("Admin") || "*".equals(roleName) || _roles.contains(roleName));
   }
   
   public void addRole(String roleName) {
      _roles.add(roleName);
   }
   
   public void removeRole(String roleName) {
      _roles.remove(roleName);
   }
   
   public Collection<String> getRoles() {
      return _roles;
   }
}