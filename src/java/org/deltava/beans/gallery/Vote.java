// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.gallery;

import org.deltava.beans.*;

/**
 * A class to score user voting for Image Gallery Images.
 * 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Vote implements AuthoredBean {

    private int _imgID;
    private int _userID;
    private int _score;
    
    /**
     * Creates a new Vote for an Image.
     * @param p the Person casting the vote
     * @param score the score (0-10) for the image
     * @param imgID the database ID of the image
     * @throws NullPointerException if p is Null 
     * @throws IllegalArgumentException if imgID is negative
     * @throws IllegalArgumentException if score < 0 or score > 10
     * @see Image#getID()
     * @see Vote#setUser(Person)
     * @see Vote#setScore(int)
     * @see Vote#setImageID(int)
     */
    public Vote(Person p, int score, int imgID) {
        setImageID(imgID);
        setScore(score);
        setUser(p);
    }
    
    /**
     * Creates a new vote for an Image.
     * @param personID the datbase of the Person casting the vote
     * @param score the score (0-10) for the image
     * @param imgID the database ID of the image
     * @throws IllegalArgumentException if personID is negative
     * @throws IllegalArgumentException if imgID is negative
     * @throws IllegalArgumentException if score < 0 or score > 10
     * @see Image#getID()
     * @see Vote#setAuthorID(int)
     * @see Vote#setScore(int)
     * @see Vote#setImageID(int)
     */
    public Vote(int personID, int score, int imgID) {
        super();
        setAuthorID(personID);
        setImageID(imgID);
        setScore(score);
    }

    /**
     * Returns the database ID of the image.
  	 * @return The primary key of the entry in the <b>GALLERY</b> table in the database that corresponds
 	 *  to the image.
 	 * @see Vote#setImageID(int)
 	 * @see Image#getID()
     */
    public int getImageID() {
        return _imgID;
    }
    
    /**
     * Returns the database ID of the Person casting this vote.
 	 * @return The primary key of the entry in the <b>PILOTS</b> table in the database that corresponds
 	 * to the person casting this vote.
 	 * @see Vote#setAuthorID(int)
 	 * @see Person#getID()
     */
    public int getAuthorID() {
        return _userID;
    }
    
    /**
     * Returns the score of the image. 
     * @return the image's score (0-10)
     * @see Vote#setScore(int)
     */
    public int getScore() {
        return _score;
    }
    
    /**
     * Updates the database ID for this vote's image. <i>This will typically be called by a DAO</i>
     * @param id The primary key of the image in the <b>GALLERY</b> table in the database that corresponds
  	  * to this vote's image.
 	  * @throws IllegalArgumentException if the database ID is zero or negative
 	  * @see Vote#getImageID()
 	  * @see Image#getID()
     */
    public void setImageID(int id) {
        DatabaseBean.validateID(_imgID, id);
  	    _imgID = id;
    }
    
    /**
     * Updates the score for this vote.
     * @param score the score for this vote (0-10)
     * @throws IllegalArgumentException if score < 0 or score > 10
     * @see Vote#getScore()
     */
    public void setScore(int score) {
        if ((score < 0) || (score > 10))
            throw new IllegalArgumentException("Score must be 0-10");
        
        _score = score;
    }
    
    /**
     * Updates the user ID for this image. <i>This will typically be called by a DAO</i>
     * @param id The primary key of the image in the <b>PILOTS</b> table in the database that corresponds
  	  * to this Person who cast this vote.
  	  * @throws IllegalArgumentException if the database ID is zero or negative
  	  * @see Vote#getAuthorID()
  	  * @see Person#getID()
     */
    public void setAuthorID(int id) {
        DatabaseBean.validateID(_userID, id);
        _userID = id;
    }
    
    /**
     * Updates the user ID for this image. <i>This will typically be called by a DAO</i>
     * @param p the Person object who cast this vote
     * @throws NullPointerException if p is null
     * @see Vote#setAuthorID(int)
     * @see Vote#getAuthorID()
     * @see Person#getID()
     */
    public void setUser(Person p) {
        setAuthorID(p.getID());
    }
}