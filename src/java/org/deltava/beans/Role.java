// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * A bean to store an access role. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class Role implements java.io.Serializable, Comparable<Role> {
	
	public static final Role PILOT = new Role("Pilot", false);
	public static final Role ADMIN = new Role("Admin", true);
	public static final Role ANONYMOUS = new Role("Anonymous", false);
	public static final Role APPLICANT = new Role("Applicant", false);
	
	private final String _name;
	private boolean _isPersistent;

	/**
	 * Creates the bean.
	 * @param name the Role name
	 * @param isPersistent TRUE if membership is persisted in the database, otherwise FALSE 
	 */
	Role(String name, boolean isPersistent) {
		super();
		_name = name;
		_isPersistent = isPersistent;
	}

	/**
	 * Returns the security role name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns if membership is persisted in the database.
	 * @return TRUE if persisted, otherwise FALSE
	 */
	public boolean isPersistent() {
		return _isPersistent;
	}
	
	@Override
	public int compareTo(Role r) {
		return _name.compareTo(r._name);
	}
	
	@Override
	public String toString() {
		return _name;
	}

	@Override
	public int hashCode() {
		return _name.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof Role) && (compareTo((Role) o) == 0);
	}
}