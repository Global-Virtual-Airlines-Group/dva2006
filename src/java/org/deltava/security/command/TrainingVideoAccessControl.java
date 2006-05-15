// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import java.util.*;

import org.deltava.beans.academy.*;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Flight Academy training videos.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TrainingVideoAccessControl extends AccessControl {

	private Collection<Course> _courses;
	private TrainingVideo _video;
	
	private boolean _canRead;
	
	/**
	 * Initializes the Access Controller.
	 * @param ctx the security context
	 * @param courses the student's currently enrollled courses 
	 */
	public TrainingVideoAccessControl(SecurityContext ctx, Collection<Course> courses) {
		super(ctx);
		_courses = courses;
	}
	
	public void updateContext(TrainingVideo video) {
		_video = video;
	}

    /**
     * Calculates access rights.
     */
	public void validate() {
		_canRead = _ctx.isUserInRole("HR") || _ctx.isUserInRole("Instructor");
		if (!_canRead) {
			for (Iterator<Course> i = _courses.iterator(); !_canRead && i.hasNext(); ) {
				Course c = i.next();
				_canRead = _canRead || _video.getCertifications().contains(c.getName());
			}
		}
	}

	/**
	 * Returns wether the user can read this Training Video.
	 * @return TRUE if the video can be read, otherwise FALSE
	 */
	public boolean getCanRead() {
		return _canRead;
	}
}