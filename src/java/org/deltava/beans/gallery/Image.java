// Copyright 2004, 2005, 2006, 2007, 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.gallery;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.*;

/**
 * A class to store Image Gallery images.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class Image extends ImageBean implements AuthoredBean, ComboAlias {

	private int _authorID;
	private int _threadID;

	private String _name;
	private String _desc;
	private boolean _fleet;
	private Instant _created = Instant.now();

	private final Collection<Integer> _likes = new LinkedHashSet<Integer>();
	private int _likeCount;

	/**
	 * Create a new Image with a particular name.
	 * @param name the name of the Image
	 * @param desc a description of the Image
	 * @throws NullPointerException if name or desc are null
	 */
	public Image(String name, String desc) {
		super();
		setName(name);
		setDescription(desc);
	}

	/**
	 * Returns the name of the image.
	 * @return the name of the image
	 * @see Image#getName()
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns the database ID of the Author of this Gallery Image.
	 * @return the Database ID of the Person who created this Image
	 * @see Image#setAuthorID(int)
	 */
	@Override
	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the database ID of the Water Cooler discussion thread associated with this Gallery Image.
	 * @return the Database ID of the Thread linked to this Image
	 * @see Image#setThreadID(int)
	 */
	public int getThreadID() {
		return _threadID;
	}

	/**
	 * Returns a description of the image.
	 * @return the image's description
	 * @see Image#setDescription(String)
	 */
	public String getDescription() {
		return _desc;
	}

	@Override
	public String getComboName() {
		return getName();
	}

	@Override
	public String getComboAlias() {
		return Integer.toHexString(getID());
	}

	/**
	 * Returns the date this Image was created.
	 * @return the date/time this Image was created on
	 * @see Image#setCreatedOn(Instant)
	 */
	public Instant getCreatedOn() {
		return _created;
	}

	/**
	 * Returns if this image is part of the Fleet Gallery.
	 * @return TRUE if the image is in the Fleet Gallery, otherwise FALSE
	 * @see Image#setFleet(boolean)
	 */
	public boolean getFleet() {
		return _fleet;
	}

	/**
	 * Updates the date this Image was created.
	 * @param dt the date/time this Image was created on
	 * @see Image#getCreatedOn()
	 */
	public void setCreatedOn(Instant dt) {
		_created = dt;
	}

	/**
	 * Updates the Author of this Image.
	 * @param id the Database ID of the author of this Image
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see Image#getAuthorID()
	 */
	@Override
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}
	
	/**
	 * Updates the Water Cooler discussion thread associated with this Image.
	 * @param id the Database ID of the discussion thread
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see Image#getThreadID()
	 */
	public void setThreadID(int id) {
		if (id != 0)
			validateID(_threadID, id);
		
		_threadID = id;
	}

	/**
	 * Updates the name of this Image.
	 * @param name the new Image name
	 * @throws NullPointerException if name is null
	 * @see Image#getName()
	 */
	public void setName(String name) {
		_name = name.trim();
	}

	/**
	 * Updates the Image's description.
	 * @param desc the new Image description
	 * @throws NullPointerException if desc is null
	 * @see Image#getDescription()
	 */
	public void setDescription(String desc) {
		_desc = desc.trim();
	}

	/**
	 * Updates if this image is part of the Fleet Gallery.
	 * @param fleet TRUE if the image is part of the Fleet Gallery, otherwise FALSE
	 * @see Image#getFleet()
	 */
	public void setFleet(boolean fleet) {
		_fleet = fleet;
	}

	/**
	 * Adds a like for this image.
	 * @param userID the database ID of the liking user
	 */
	public void addLike(int userID) {
		_likes.add(Integer.valueOf(userID));
	}

	/**
	 * Updates the number of Likes for this Image.
	 * @param count the number of Likes
	 * @throws IllegalStateException if the Votes have already been loaded
	 * @see Image#getLikeCount()
	 */
	public void setLikeCount(int count) {
		if (_likes.size() > 0)
			throw new IllegalStateException("Likes already loaded");

		_likeCount = Math.max(0, count);
	}

	/**
	 * Checks if a particular person has cast a vote for this image
	 * @param p the Person to check for
	 * @return TRUE if the person has voted, otherwise FALSE
	 * @see Person
	 */
	public boolean hasLiked(Person p) {
		return (p == null) ? false : _likes.contains(Integer.valueOf(p.getID()));
	}

	/**
	 * Returns all votes for this image.
	 * @return a Collection of votes for this image
	 * @see Image#addLike(int)
	 */
	public Collection<Integer> getLikes() {
		return new ArrayList<Integer>(_likes);
	}

	/**
	 * Returns the number of Votes cast for this Image.
	 * @return the number of Votes
	 * @see Image#setLikeCount(int)
	 */
	public int getLikeCount() {
		return (_likeCount > 0) ? _likeCount : _likes.size();
	}
}