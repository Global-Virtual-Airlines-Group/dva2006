package org.deltava.beans.testing;

/**
 * A class to store Check Ride and Flight Video data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CheckRide extends Test {
    
	private String _comments;
	private String _fileName;
	private int _size;

    /**
     * Creates a new Check Ride/Video.
     * @param name the name of the checkride
     * @see Test#getName()
     */
    public CheckRide(String name) {
        super(name);
    }

    /**
     * Returns the type of Test.
     * @return Test.CHECKRIDE
     */
    public int getType() {
        return Test.CHECKRIDE;
    }
    
    /**
     * Returns the Check Ride comments.
     * @return the comments
     * @see CheckRide#setComments(String)
     */
    public String getComments() {
    	return _comments;
    }
    
    /**
     * Returns the Flight Video file name.
     * @return the file name
     * @see CheckRide#setFileName(String)
     */
    public String getFileName() {
       return _fileName;
    }
    
    /**
     * Returns the Flight Video size.
     * @return the video size in bytes
     * @see CheckRide#setSize(int)
     */
    public int getSize() {
    	return _size;
    }

    /**
     * Updates the Flight Video comments.
     * @param comments the comments
     * @see CheckRide#getComments()
     */
    public void setComments(String comments) {
    	_comments = comments;
    }
    
    /**
     * Updates the Flight Video file name.
     * @param fileName the file name
     * @see CheckRide#getFileName()
     */
    public void setFileName(String fileName) {
       _fileName = fileName;
    }
    
    /**
     * Updates the Flight Video size.
     * @param size the video size in bytes
     * @throws IllegalArgumentException if size is negative
     * @see CheckRide#getSize()
     */
    public void setSize(int size) {
    	if (size < 0)
    		throw new IllegalArgumentException("Size cannot be negative");
    	
    	_size = size;
    }
    
    /**
     * Sets the score for the Check Ride.
     * @param passFail TRUE if a pass (1), otherwise FALSE (0) for a fail
     * @see CheckRide#setScore(int)
     * @see Test#setPassFail(boolean)
     */
    public void setScore(boolean passFail) {
        setScore((passFail) ? 1 : 0);
        setPassFail(passFail);
    }
    
    /**
     * Sets the score for the Check ride.
     * @param score the score, either 0 or 1 for pass or fail
     * @throws IllegalArgumentException if the score is not 0 or 1
     * @see CheckRide#setScore(boolean)
     */
    public final void setScore(int score) {
        if ((score != 0) && (score != 1))
            throw new IllegalArgumentException("Score must be 0 or 1");
        
        _score = score;
    }
}