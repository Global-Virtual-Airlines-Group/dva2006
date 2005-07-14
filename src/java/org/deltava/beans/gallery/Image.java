package org.deltava.beans.gallery;

import java.io.*;
import java.util.*;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.DatabaseBlobBean;
import org.deltava.beans.Person;

import org.deltava.util.ImageInfo;

/**
 * A class to store Image Gallery images.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Image extends DatabaseBlobBean implements ComboAlias {

   private int _authorID;
   
    private String _name;
    private String _desc;
    private boolean _fleet;
    private Date _created;
    
    private int _imgSize;
    private int _imgX;
    private int _imgY;
    private int _imgType;
    
    private Map _votes;
    
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
     * Returns the size of the image.
     * @return the size of the image, in bytes
     * @see Image#setSize(int)
     * @see Image#load(InputStream)
     */
    public int getSize() {
        return (_buffer == null) ? _imgSize : super.getSize();
    }
    
    /**
     * Returns the type of image. This uses constants found in ImageInfo.
     * @return the image type code
     * @see ImageInfo#getFormat()
     * @see Image#setType(int)
     * @see Image#load(InputStream)
     */
    public int getType() {
        return _imgType;
    }
    
    /**
     * Returns the width of the image.
     * @return the width of the image, in pixels
     * @see Image#setWidth(int)
     * @see Image#load(InputStream)
     */
    public int getWidth() {
        return _imgX;
    }
    
    /**
     * Returns the height of the image.
     * @return the height of the image, in pixels
     * @see Image#setHeight(int)
     * @see Image#load(InputStream)
     */
    public int getHeight() {
        return _imgY;
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
     * Loads an image into the buffer. The image data should be available at the specified input stream, and the stream
     * is <u>not</u> closed when this method completes. This method populates the size, X, Y and type properties
     * if the stream is loaded successfully.
     * @param is the stream containing the image data
     * @throws IOException if an error occurs loading the data
     * @throws UnsupportedOperationException if ImageInfo cannot identify the image format
     */
    public final void load(InputStream is) throws IOException {
        super.load(is);
        getImageData();
    }
    
    /**
     * Updates the image buffer. This method populates the size, X, Y and type properties.
     * @param buffer an array containing the image data
     * @throws UnsupportedOperationException if ImageInfo cannot identify the image format
     */
    public final void load(byte[] buffer) {
       super.load(buffer);
       getImageData();
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
     * Helper method to read image metadata.
     */
    private void getImageData() {
       _imgSize = getSize();
       
       // Get the image dimensions and type. If it's not valid, throw an exception
       // and invalidate the buffer
       ImageInfo imgInfo = new ImageInfo(_buffer);
       if (!imgInfo.check()) {
           _buffer = null;
           throw new UnsupportedOperationException("Unknown Image Format");
       }

       _imgType = imgInfo.getFormat();
       _imgX = imgInfo.getWidth();
       _imgY = imgInfo.getHeight();
    }
    
    /**
     * Helper method to check for negative numeric parameters. 
     */
    private void checkParam(int param, String msg) throws IllegalArgumentException {
        if (param < 0)
            throw new IllegalArgumentException(msg + " cannot be negative");
    }
    
    /**
     * Updates the size of this image.
     * @param newSize the size of the image, in bytes
     * @throws IllegalStateException if the size of the image has already been set
     * @throws IllegalArgumentException if the image size is negative
     * @see Image#getSize()
     */
    public void setSize(int newSize) {
        if (_imgSize != 0)
            throw new IllegalStateException("Image Size already set");
        
        checkParam(newSize, "Image Size");
        _imgSize = newSize;
    }
    
    /**
     * Updates the image type. Note that this method does not change the image date (ie. convert formats)
     * @param type the type of the image, as defined in ImageInfo
     * @throws IllegalStateException if the image type has already been set
     * @throws IllegalArgumentException if the image type is not contained within ImageInfo
     * @see ImageInfo
     * @see Image#getType()
     * @see Image#load(InputStream)
     */
    public void setType(int type) {
        if (_imgType != 0)
            throw new IllegalStateException("Image Type already set");
        
        if ((type < 0) || (type >= ImageInfo.FORMAT_NAMES.length))
            throw new IllegalArgumentException("Invalid Image Type - " + type);
        
        _imgType = type;
    }
    
    /**
     * Updates the width of this image. Note that this method does not change the image date (ie. resize the image)
     * @param x the new width of the image, in pixels
     * @throws IllegalArgumentException if the image width has already been set
     * @see Image#getWidth()
     * @see Image#load(InputStream)
     */
    public void setWidth(int x) {
        checkParam(x, "Image Width");
        _imgX = x;
    }
    
    /**
     * Updates the height of this image. Note that this method does not change the image date (ie. resize the image)
     * @param y the new height of the image, in pixels
     * @throws IllegalArgumentException if the image height has already been set
     * @see Image#getHeight()
     * @see Image#load(InputStream)
     */
    public void setHeight(int y) {
        checkParam(y, "Image Height");
        _imgY = y;
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
    		_votes = new HashMap();
    	
        _votes.put(new Integer(v.getUserID()), v);
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
        Vote v = isPopulated() ? (Vote) _votes.get(new Integer(p.getID())) : null;
        return (v == null) ? -1 : v.getScore();
    }
    
    /**
     * Returns all votes for this image. 
     * @return a List of votes for this image
     * @see Vote
     * @see Image#addVote(Vote)
     */
    public List getVotes() {
        return isPopulated() ? new ArrayList(_votes.values()) : Collections.EMPTY_LIST;
    }
    
    /**
     * Gets the average score for this image.
     * @return the average of all votes for this image, or -1 if no votes have been recorded
     * @see Image#setScore(double)
     */
    public double getScore() {
        if (!isPopulated()) return _score;
        
        int tmpResult = 0;
        for (Iterator i = _votes.values().iterator(); i.hasNext(); ) {
            Vote v = (Vote) i.next();
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