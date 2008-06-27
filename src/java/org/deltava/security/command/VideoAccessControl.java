// Copyright 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import java.util.*;

import org.deltava.beans.fleet.Video;
import org.deltava.beans.academy.*;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Flight Academy training videos.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class VideoAccessControl extends AccessControl {

	private Collection<Course> _courses;
	private Video _video;
	
	private boolean _canRead;
	
	/**
	 * Initializes the Access Controller.
	 * @param ctx the security context
	 * @param courses the student's currently enrollled courses 
	 */
	public VideoAccessControl(SecurityContext ctx, Collection<Course> courses) {
		super(ctx);
		_courses = courses;
	}
	
	/**
	 * Updates the video to validate access for.
	 * @param video the Video to check
	 */
	public void updateContext(Video video) {
		_video = video;
	}

    /**
     * Calculates access rights.
     */
	public void validate() {
		boolean isTV = (_video instanceof TrainingVideo);
		_canRead = _ctx.isUserInRole("HR") || _ctx.isUserInRole("Instructor") || !isTV;
		if (!_canRead && isTV) {
			TrainingVideo tv = (TrainingVideo) _video;
			for (Iterator<Course> i = _courses.iterator(); !_canRead && i.hasNext(); ) {
				Course c = i.next();
				_canRead |= tv.getCertifications().contains(c.getName());
			}
		}
	}

	/**
	 * Returns whether the user can view this Video.
	 * @return TRUE if the video can be read, otherwise FALSE
	 */
	public boolean getCanRead() {
		return _canRead;
	}
}