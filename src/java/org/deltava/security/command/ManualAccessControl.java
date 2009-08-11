// Copyright 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import java.util.*;

import org.deltava.beans.academy.*;
import org.deltava.beans.fleet.*;

import org.deltava.security.SecurityContext;

import org.deltava.util.CollectionUtils;

/**
 * An Access Controller for Document Library entries.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class ManualAccessControl extends FleetEntryAccessControl {

	private Collection<Course> _courses;

	/**
	 * Initializes the access controller.
	 * @param ctx the security context
	 * @param courses the User's active Flight Academy courses
	 */
	public ManualAccessControl(SecurityContext ctx, Collection<Course> courses) {
		super(ctx, null);
		_courses = courses;
	}

	/**
	 * Updates the Fleet Entry to validate access to.
	 * @param e the entry
	 * @throws SecurityException if e is not a manual
	 */
	public final void setEntry(LibraryEntry e) {
		if (!(e instanceof Manual))
			throw new SecurityException("Invalid object - " + e.getClass().getSimpleName());

		super.setEntry(e);
	}

	/**
	 * Calculates access rights.
	 */
	public void validate() {
		super.validate();

		// If the entry has certifications attached, see if we have any courses.
		Manual m = (Manual) _entry;
		if (CollectionUtils.isEmpty(m.getCertifications()))
			return;

		// If we're an instructor/HR/Fleet, we're OK
		if (_ctx.isUserInRole("Instructor") || _ctx.isUserInRole("HR") || _ctx.isUserInRole("Fleet"))
			return;

		// Check if we have any active courses
		for (Iterator<Course> i = _courses.iterator(); i.hasNext();) {
			Course c = i.next();
			if (c.getStatus() != Course.ABANDONED) {
				if (m.getCertifications().contains(c.getName()))
					return;
			}
		}

		// If we got this far, then disable read access
		_canView = false;
	}
}