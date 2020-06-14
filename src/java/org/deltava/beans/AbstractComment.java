// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.Instant;

/**
 * A bean to store comments with attachments.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public abstract class AbstractComment extends DatabaseBlobBean implements AuthoredBean {
	
	private int _authorID;
	private int _parentID;
	private Instant _createdOn = Instant.now();
	private String _body;
	
	private String _name;
	private int _size;

	/**
	 * Returns the body text of this Comment.
	 * @return the Comment text
	 * @see AbstractComment#setBody(String)
	 */
	public String getBody() {
		return _body;
	}

	/**
	 * Returns the size of the attachment.
	 * @return the size in bytes
	 * @see AbstractComment#setSize(int)
	 */
	@Override
	public int getSize() {
		return isLoaded() ? super.getSize() : _size;
	}
	
	/**
	 * Returns the file buffer.
	 * @return the buffer
	 * @throws IllegalStateException if not loaded
	 */
	public byte[] getBuffer() {
		if (!isLoaded())
			throw new IllegalStateException("Not loaded");
		
		return _buffer;
	}
	
	/**
	 * Returns the file extension.
	 * @return the extension, or the file name if none
	 */
	public String getExtension() {
		int pos = _name.lastIndexOf('.');
		return _name.substring(pos + 1);
	}
	
	/**
	 * Returns the name of the attachment.
	 * @return the file name
	 * @see AbstractComment#setName(String)
	 */
	public String getName() {
		return _name;
	}
	
	@Override
	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the parent record's database ID.
	 * @return the ID
	 * @see AbstractComment#setParentID(int)
	 */
	public int getParentID() {
		return _parentID;
	}
	
	/**
	 * Returns the creation date of this Comment.
	 * @return the date/time the Comment was created
	 * @see AbstractComment#setCreatedOn(Instant)
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Updates the creation date of this Comment.
	 * @param dt the date/time the Comment was created
	 * @see AbstractComment#getCreatedOn()
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}
	
	/**
	 * Updates the body text of this Comment.
	 * @param body the body text
	 * @see AbstractComment#setBody(String)
	 */
	public void setBody(String body) {
		_body = body;
	}
	
	/**
	 * Updates the name of the attachment.
	 * @param name the file name
	 * @see AbstractComment#getName()
	 */
	public void setName(String name) {
		_name = name;
	}
	
	/**
	 * Updates the size of the attachment.
	 * @param size the size in bytes
	 * @throws IllegalArgumentException if the attachment has been loaded
	 * @see AbstractComment#getSize()
	 */
	public void setSize(int size) {
		if (isLoaded())
			throw new IllegalArgumentException("Attachment already loaded");
		
		_size = Math.max(0, size);
	}
	
	@Override
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}
	
	/**
	 * Updates the ID of the parent record.
	 * @param id the parent's database record
	 * @see AbstractComment#getParentID()
	 */
	public void setParentID(int id) {
		if (id != 0) validateID(_parentID, id);
		_parentID = id;
	}
	
	@Override
	public int compareTo(Object o) {
		AbstractComment ic2 = (AbstractComment) o;
		int tmpResult = super.compareTo(ic2);
		return (tmpResult == 0) ? _createdOn.compareTo(ic2._createdOn) : tmpResult;
	}
}