// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import java.util.*;

import org.deltava.beans.Pilot;

public class AccessControlUser extends Pilot {

	public AccessControlUser(String fName, String lName) {
		super(fName, lName);
	}

	@Override
	public String getRowClassName() {
		return null;
	}

	public void removeRole(String roleName) {
		Collection<String> roles = new HashSet<String>();
		roles.add(roleName);
		removeRoles(roles);
	}
}