// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.blog;

import java.util.*;

import org.deltava.beans.*;

/**
 * A bean to store blog entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Entry extends DatabaseBean implements AuthoredBean, CalendarEntry {

	private int _authorID; 
	private Date _date;
	private String _title;
	private String _body;
	private boolean _private;
	private boolean _locked;
	
	private int _commentCount;
	private Collection<Comment> _comments;
	
	/**
	 * Creates a new blog entry.
	 * @param title the entry title
	 * @throws NullPointerException if title is null
	 * @see Entry#getTitle() 
	 */
	public Entry(String title) {
		super();
		setTitle(title);
		_date = new Date();
		_comments = new TreeSet<Comment>();
	}
	
	/**
	 * Returns the date of this blog entry.
	 * @return the date/time the entry was created
	 * @see CalendarEntry#getDate()
	 */
	public Date getDate() {
		return _date;
	}

	/**
	 * Returns the title of this blog entry.
	 * @return the entry title
	 * @see Entry#getTitle()
	 */
	public String getTitle() {
		return _title;
	}
	
	/**
	 * Returns the author of this blog entry.
	 * @return the author's database ID
	 * @see Entry#setAuthorID(int)
	 */
	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the body of this blog entry.
	 * @return the entry body
	 * @see Entry#setBody(String)
	 */
	public String getBody() {
		return _body;
	}
	
	/**
	 * Returns the number of comments to this entry.
	 * @return the number of comments
	 */
	public int getSize() {
		return (_commentCount == 0) ? _comments.size() : _commentCount;
	}
	
	/**
	 * Returns all comments and feedback to this blog entry.
	 * @return a Collection of Comment beans
	 * @see Entry#addComment(Comment)
	 */
	public Collection<Comment> getComments() {
		return _comments;
	}
	
	/**
	 * Returns wether this blog entry is private.
	 * @return TRUE if the entry is not visible to anyone other than the author, otherwise FALSE
	 * @see Entry#setPrivate(boolean)
	 */
	public boolean getPrivate() {
		return _private;
	}
	
	/**
	 * Returns wether new anonymous comments are locked out.
	 * @return TRUE if no new comments can be created by anonymous users, otherwise FALSE
	 * @see Entry#setLocked(boolean)
	 */
	public boolean getLocked() {
		return _locked;
	}
	
	/**
	 * Adds a comment to this blog entry.
	 * @param c a Comment bean
	 * @see Entry#getComments()
	 */
	public void addComment(Comment c) {
		_comments.add(c);
	}
	
	/**
	 * Updates the date of this entry.
	 * @param dt the date/time this entry was created
	 * @see Entry#getDate()
	 */
	public void setDate(Date dt) {
		_date = dt;
	}
	
	/**
	 * Updates the number of comments to this blog entry.
	 * @param size the number of comments
	 * @throws IllegalStateException if comments have already been loaded
	 * @throws IllegalArgumentException if size is negative
	 */
	public void setSize(int size) {
		if (!_comments.isEmpty())
			throw new IllegalStateException("Comments already loaded");
		else if (size < 0)
			throw new IllegalArgumentException("Size cannot be negative");
		
		_commentCount = size;
	}
	
	/**
	 * Updates the Author of this entry.
	 * @param id the database ID of the author
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see Entry#getAuthorID()
	 */
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}
	
	/**
	 * Updates the blog entry title
	 * @param title the title
	 * @throws NullPointerException if title is null
	 * @see Entry#getTitle()
	 */
	public void setTitle(String title) {
		_title = title.trim();
	}
	
	/**
	 * Updates the text of this blog entry.
	 * @param txt the entry text
	 * @see Entry#getBody()
	 */
	public void setBody(String txt) {
		_body = txt;
	}
	
	/**
	 * Updates wether this blog entry is private.
	 * @param isPrivate TRUE if the entry is invisible to anyone other than the author, otherwise FALSE
	 * @see Entry#getPrivate()
	 */
	public void setPrivate(boolean isPrivate) {
		_private = isPrivate;
	}
	
	/**
	 * Updates wether the entry is locked.
	 * @param isLocked TRUE if no new anonymous comments may be entered, otherwise FALSE
	 * @see Entry#getLocked()
	 */
	public void setLocked(boolean isLocked) {
		_locked = isLocked;
	}

	/**
	 * Compares two entries by comparing their dates.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		Entry e2 = (Entry) o;
		return _date.compareTo(e2._date);
	}
}