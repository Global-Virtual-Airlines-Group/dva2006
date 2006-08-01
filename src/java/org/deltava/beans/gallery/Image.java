// Copyright 2004, 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.gallery;

import java.util.*;

import org.deltava.beans.*;

/**
 * A class to store Image Gallery images.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Image extends ImageBean implements ComboAlias {

   private int _authorID;
   
    private String _name;
    private String _desc;
    private boolean _fleet;
    private Date _created;
    
    private Map<Integer, Vote> _votes;
    
    private double _score = -1;
    private int _voteCount;
    
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
        _created = new Date();
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
     * @return the Database ID of the Person who created this Image.
     * @see Image#setAuthorID(int)
     */
    public int getAuthorID() {
       return _authorID;
    }
    
    /**
     * Returns a description of the image.
     * @return the image's description
     * @see Image#setDescription(String)
     */
    public String getDescription() {
        return _desc;
    }
    
    public String getComboName() {
        return getName();
    }
    
    public String getComboAlias() {
        return Integer.toHexString(getID());
    }

    /**
     * Returns the date this Image was created. 
     * @return the date/time this Image was created on
     * @see Image#setCreatedOn(Date)
     */
    public Date getCreatedOn() {
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
    public void setCreatedOn(Date dt) {
        _created = dt;
    }
    
    /**
     * Updates the Author of this Image.
     * @param id the Database ID of the author of this Image
     * @throws IllegalArgumentException if id is zero or negative
     * @see Image#getAuthorID()
     */
    public void setAuthorID(int id) {
       validateID(_authorID, id);
       _authorID = id;
    }
    
    /**
     * Helper method to check if we have loaded Votes for this Image.
     */
    private boolean isPopulated() {
    	return (_votes != null);
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
     * Adds a vote for this image.
     * @param v the vote to add. If the person has already voted, the previous vote is overriden. 
     */
    public void addVote(Vote v) {
    	if (_votes == null)
    		_votes = new HashMap<Integer, Vote>();
    	
        _votes.put(new Integer(v.getAuthorID()), v);
    }
    
    /**
     * Updates the average score for this Image.
     * @param score the average score
     * @throws IllegalStateException if the Votes have already been loaded
     * @throws IllegalArgumentException if score is negative or greater than 10.0
     * @see Image#getScore()
     */
    public void setScore(double score) {
    	checkParam((int) score, "Image Score");
    	if (isPopulated())
    		throw new IllegalStateException("Votes already loaded");
    	
    	if (score > 10.0)
    		throw new IllegalArgumentException("Score cannot be > 10.0");
    	
    	_score = score;
    }
    
    /**
     * Updates the number of Votes cast for this Image.
     * @param count the number of Votes
     * @throws IllegalStateException if the Votes have already been loaded
     * @throws IllegalArgumentException if count is negative
     * @see Image#getVoteCount()
     */
    public void setVoteCount(int count) {
    	checkParam(count, "Vote Count");
    	if (isPopulated())
    		throw new IllegalStateException("Votes already loaded");
    	
    	_voteCount = count;
    }
    
    /**
     * Checks if a particular person has cast a vote for this image
     * @param p the Person to check for
     * @return TRUE if the person has voted, otherwise FALSE
     * @see Person
     */
    public boolean hasVoted(Person p) {
       if (p == null)
          return false;
       
        return isPopulated() ? _votes.containsKey(new Integer(p.getID())) : false;
    }
    
    /**
     * Returns the vote of a particular user.
     * @param p the Person to check for
     * @return the person's vote, or -1 if the Person has not voted
     * @see Image#addVote(Vote)
     * @see Image#hasVoted(Person)
     * @see Vote
     * @see Person
     */
    public int myScore(Person p) {
        Vote v = isPopulated() ? _votes.get(new Integer(p.getID())) : null;
        return (v == null) ? -1 : v.getScore();
    }
    
    /**
     * Returns all votes for this image. 
     * @return a Collection of votes for this image
     * @see Vote
     * @see Image#addVote(Vote)
     */
    public Collection<Vote> getVotes() {
        return isPopulated() ? new ArrayList<Vote>(_votes.values()) : new ArrayList<Vote>();
    }
    
    /**
     * Gets the average score for this image.
     * @return the average of all votes for this image, or -1 if no votes have been recorded
     * @see Image#setScore(double)
     */
    public double getScore() {
        if (!isPopulated()) return _score;
        
        int tmpResult = 0;
        for (Iterator<Vote> i = _votes.values().iterator(); i.hasNext(); ) {
            Vote v = i.next();
            tmpResult += v.getScore();
        }
        
        return (tmpResult / _votes.size());
    }
    
    /**
     * Returns the number of Votes cast for this Image.
     * @return the number of Votes
     * @see Image#setVoteCount(int)
     */
    public int getVoteCount() {
       return isPopulated() ? _votes.size() : _voteCount;
    }
}