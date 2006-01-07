package org.deltava.security;

import java.util.*;

import junit.framework.TestCase;

import org.deltava.beans.Person;

public abstract class AuthenticatorTestCase extends TestCase {

	protected class AuthPerson extends Person {
		
		private Collection<String> _roles = new TreeSet<String>();
		
		public AuthPerson(String fName, String lName, String dn) {
			super(fName, lName);
			setDN(dn);
		}
		
		public void addRole(String role) {
			_roles.add(role);
		}
		
		public Collection<String> getRoles() {
			return _roles;
		}
		
		public boolean isInRole(String role) {
			return _roles.contains(role);
		}
		
		public String getRowClassName() {
			return null;
		}
	}
}