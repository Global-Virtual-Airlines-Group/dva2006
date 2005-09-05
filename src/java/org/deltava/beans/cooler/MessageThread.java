package org.deltava.beans.cooler;

import java.util.*;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.ViewEntry;

/**
 * A class to store Water Cooler threads.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MessageThread extends DatabaseBean implements Comparable, ViewEntry {

    private String _subject;
    private String _channel;
    
    private int _imgID;
    private int _authorID;
    private int _lastUpdateID;
    
    private boolean _hidden;
    private boolean _locked;
    private boolean _stickyChannel;
    
    private Date _lastUpdatedOn;
    private Date _stickyUntil;
    
    private int _views;
    private int _postCount;
    
    private Set _posts;
    
    /**
     * Create a new thread with a particular subject.
     * @param subject the thread subject
     * @throws NullPointerException if subject is null
     * @see MessageThread#getSubject()
     */
    public MessageThread(String subject) {
        super();
        _subject = subject.trim();
    }
    
    /**
     * Returns the channel name for this thread.
     * @return the channel name
     * @see MessageThread#setChannel(String)
     * @see Channel
     */
    public String getChannel() {
        return _channel;
    }
    
    /**
     * Returns the database ID of the first Person to post to this thread.
     * @return the database ID
     * @see MessageThread#setAuthorID(int)
     * @see MessageThread#getLastUpdateID()
     */
    public int getAuthorID() {
        return (_posts == null) ? _authorID : ((Message) _posts.iterator().next()).getAuthorID();
    }
    
    /**
     * Returns the database ID of the last Person to post to this thread.
     * @return the database ID
     * @see MessageThread#setLastUpdateID(int)
     * @see MessageThread#getAuthorID()
     */
    public int getLastUpdateID() {
        return _lastUpdateID;
    }
    
    /**
     * Returns the date of the latest post in this thread.
     * @return the date/time of the latest post
     * @see MessageThread#setLastUpdatedOn(Date)
     */
    public Date getLastUpdatedOn() {
        return _lastUpdatedOn;
    }
    
    /**
     * Returns the date/time this thread will be &quot;stuck&quot; at the top of the thread list until.
     * @return the date/time the post will be a sticky until
     * @see MessageThread#setStickyUntil(Date)
     */
    public Date getStickyUntil() {
        return _stickyUntil;
    }
    
    /**
     * Returns if this thread should be considered a &quot;sticky&quot; thread in its Channel only.
     * @return TRUE if the thread is &quot;sticky&quot; and is sticky in the Channel, otherwise FALSE
     */
    public boolean getStickyInChannelOnly() {
        return (_stickyChannel && (_stickyUntil != null));
    }
    
    /**
     * Returns the number of times this thread has been read.
     * @return the number of views
     * @see MessageThread#setViews(int)
     */
    public int getViews() {
        return _views;
    }
    
    /**
     * Returns the number of messages in this thread.
     * @return the number of posts
     * @see MessageThread#setPostCount(int)
     */
    public int getPostCount() {
        return (_posts == null) ? _postCount : _posts.size();
    }
    
    /**
     * Returns the linked image ID for this thread.
     * @return the linked Image ID
     * @see MessageThread#setImage(int)
     */
    public int getImage() {
        return _imgID;
    }
    
    /**
     * Returns the thread's subject.
     * @return the subject
     */
    public String getSubject() {
        return _subject;
    }
    
    /**
     * Returns if this thread is hidden to general users.
     * @return TRUE if the thread is hidden, otherwise FALSE
     * @see MessageThread#setHidden(boolean)
     * @see MessageThread#getLocked()
     */
    public boolean getHidden() {
        return _hidden;
    }
    
    /**
     * Returns if this thread is locked (no new posts allowed)
     * @return TRUE if the thread is locked, otherwise FALSE
     * @see MessageThread#setLocked(boolean)
     * @see MessageThread#getHidden()
     */
    public boolean getLocked() {
        return _locked;
    }
    
    /**
     * Updates this thread's Channel.
     * @param channelName the name of the channel
     * @throws NullPointerException if channelName is null
     * @see MessageThread#getChannel()
     * @see Channel
     */
    public void setChannel(String channelName) {
        _channel = channelName.trim();
    }
    
    /**
     * Updates the database ID of the first Person to post to this thread.
     * @param id the database ID
     * @throws IllegalArgumentException if the ID is invalid
     * @see MessageThread#getAuthorID()
     * @see MessageThread#setLastUpdateID(int)
     * @see DatabaseBean#validateID(int, int)
     */
    public void setAuthorID(int id) {
        validateID(_authorID, id);
        _authorID = id;
    }

    /**
     * Updates the database ID of the last Person to post to this thread.
     * @param id the database ID
     * @throws IllegalArgumentException if the ID is invalid
     * @see MessageThread#getLastUpdateID()
     * @see MessageThread#setAuthorID(int)
     * @see DatabaseBean#validateID(int, int)
     */
    public void setLastUpdateID(int id) {
        validateID(_lastUpdateID, id);
        _lastUpdateID = id;
    }
    
    /**
     * Updates the date this thread's latest message was posted.
     * @param dt the date/time the last message was posted
     */
    public void setLastUpdatedOn(Date dt) {
        _lastUpdatedOn = dt;
    }
    
    /**
     * Updates the date/time this threa will be a &quot;sticky&quot; until.
     * @param dt the date/time
     * @see MessageThread#getStickyUntil()
     */
    public void setStickyUntil(Date dt) {
        _stickyUntil = dt;
    }
    
    public void setStickyInChannelOnly(boolean isSticky) {
        _stickyChannel = isSticky;
    }
    
    /**
     * Sets this thread's hidden flag.
     * @param hidden TRUE if the thread is hidden, otherwise FALSE
     * @see MessageThread#getHidden()
     */
    public void setHidden(boolean hidden) {
        _hidden = hidden;
    }
    
    /**
     * Sets this thread's locked flag.
     * @param locked TRUE if the thread is locked, otherwise FALSE
     * @see MessageThread#getLocked()
     */
    public void setLocked(boolean locked) {
        _locked = locked;
    }
    
    /**
     * Updates the linked image for this thread.
     * @param id the linked image database ID
     * @see MessageThread#getImage()
     */
    public void setImage(int id) {
        if (id != 0)
            validateID(_imgID, id);
        
        _imgID = id;
    }
    
    /**
     * Updates the number of times this thread has been read.
     * @param views the number of views
     * @throws IllegalArgumentException if views is negative
     * @see MessageThread#getViews()
     */
    public void setViews(int views) {
        if (views < 0)
            throw new IllegalArgumentException("Thread View count cannot be negative");
        
        _views = views;
    }
    
    /**
     * Updates the number of messages in this thread.
     * @param posts the number of messages
     * @throws IllegalArgumentException if posts is negative
     * @throws IllegalStateException if a message has been added via addPost()
     * @see MessageThread#getPostCount()
     * @see MessageThread#addPost(Message)
     */
    public void setPostCount(int posts) {
        if (posts < 0)
            throw new IllegalArgumentException("Thread Post count cannot be negative");
        
        if (_posts != null)
            throw new IllegalStateException("Post Set already initialized");
        
        _postCount = posts; 
    }
    
    /**
     * Adds a message to this thread.
     * @param msg the message to add
     * @throws NullPointerException if msg is null
     * @see MessageThread#getPosts()
     * @see MessageThread#getPostCount()
     */
    public void addPost(Message msg) {
        if (_posts == null) {
            _posts = new TreeSet();
            _lastUpdatedOn = new Date(1);
        }
        
        // Check last updated date
        if (msg.getCreatedOn().after(_lastUpdatedOn)) {
            _lastUpdatedOn = msg.getCreatedOn();
            _lastUpdateID = msg.getAuthorID();
        }
        
        _posts.add(msg);
    }
    
    /**
     * Returns this thread's messages.
     * @return a List of posts, or an empty list if no messages have been added
     * @see MessageThread#getPostCount()
     * @see MessageThread#addPost(Message)
     */
    public List getPosts() {
        return (_posts == null) ? Collections.EMPTY_LIST : new ArrayList(_posts);
    }
    
    /**
     * Compares to another thread via the last updated on date.
     */
    public int compareTo(Object o2) {
        MessageThread t2 = (MessageThread) o2;
        return _lastUpdatedOn.compareTo(t2.getLastUpdatedOn());
    }

    /**
     * Selects a table row class based upon wether the thread is hidden or not.
     * @return the row CSS class name
     */
    public String getRowClassName() {
    	return (_hidden) ? "warn" : null;
    }
}