// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.Collection;
import java.util.TreeSet;

import org.deltava.beans.Person;

class AuthPerson extends Person {
	
	private final Collection<String> _roles = new TreeSet<String>();
	
	public AuthPerson(String fName, String lName, String dn) {
		super(fName, lName);
		setDN(dn);
	}
	
	public String getStatusName() {
		return "MockPerson";
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