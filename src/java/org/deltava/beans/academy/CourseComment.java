// Copyright 2006, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import org.deltava.beans.*;

/**
 * A bean to store comments between Instructors and Students. 
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class CourseComment extends AbstractComment {

	/**
	 * Creates a new Comment bean.
	 * @param courseID the database ID of the Course
	 * @param authorID the database ID of the author
	 * @throws IllegalArgumentException if courseID or authorID are zero or negative
	 */
	public CourseComment(int courseID, int authorID) {
		super();
		setParentID(courseID);
		setAuthorID(authorID);
	}
}