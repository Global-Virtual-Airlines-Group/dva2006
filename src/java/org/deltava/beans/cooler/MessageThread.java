// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

import java.util.*;

import org.deltava.beans.*;

/**
 * A class to store Water Cooler message threads.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MessageThread extends DatabaseBean implements AuthoredBean, ViewEntry {

	private String _subject;
	private String _channel;

	private int _imgID;
	private int _authorID;
	private int _lastUpdateID;
	
	private int _reportCount;
	private Collection<Integer> _reportIDs;

	private boolean _hidden;
	private boolean _locked;
	private boolean _stickyChannel;
	private boolean _isPoll;

	private Date _lastUpdatedOn;
	private Date _stickyUntil;

	private int _views;
	private int _postCount;

	private SortedSet<Message> _posts;
	private SortedSet<ThreadUpdate> _updates;
	
	private Collection<LinkedImage> _imgURLs;

	private Collection<PollOption> _pollOptions;
	private Collection<PollVote> _pollVotes;

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
		return (_posts == null) ? _authorID : (_posts.iterator().next()).getAuthorID();
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
	 * Returns the number of times this post has been reported for content.
	 * @return the number of reports, or -1 if no further reports can be made
	 * @see MessageThread#setReportCount(int)
	 */
	public int getReportCount() {
		return (_reportIDs == null) ? _reportCount : _reportIDs.size();
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
	 * Returns if this thread is locked (no new posts allowed).
	 * @return TRUE if the thread is locked, otherwise FALSE
	 * @see MessageThread#setLocked(boolean)
	 * @see MessageThread#getHidden()
	 */
	public boolean getLocked() {
		return _locked;
	}
	
	/**
	 * Returns if this thread has a poll.
	 * @return TRUE if there is a poll, otherwise FALSE
	 * @see MessageThread#getOptions()
	 * @see MessageThread#setPoll(boolean)
	 */
	public boolean getPoll() {
		return _isPoll || !getOptions().isEmpty(); 
	}

	/**
	 * Returns all poll options for this Message Thread.
	 * @return a Collection of PollOption beans
	 * @see MessageThread#addOptions(Collection)
	 * @see MessageThread#getVotes()
	 */
	public Collection<PollOption> getOptions() {
		return (_pollOptions == null) ? new HashSet<PollOption>() : _pollOptions;
	}

	/**
	 * Returns all poll votes for this Message Thread.
	 * @return a Collection of PollVote beans
	 * @see MessageThread#addVotes(Collection)
	 * @see MessageThread#getOptions()
	 */
	public Collection<PollVote> getVotes() {
		return (_pollVotes == null) ? new HashSet<PollVote>() : _pollVotes;
	}
	
	/**
	 * Returns all linked Image URLs for this Message Thread.
	 * @return a Collection of LinkedImages
	 * @see MessageThread#addImageURL(String, String)
	 */
	public Collection<LinkedImage> getImageURLs() {
		return (_imgURLs == null) ? new HashSet<LinkedImage>() : _imgURLs;
	}
	
	/**
	 * Returns the database IDs of all users reporting this thread for content.
	 * @return a Collection of database IDs
	 * @see MessageThread#getReportCount()
	 */
	public Collection<Integer> getReportIDs() {
		return (_reportIDs == null) ? new HashSet<Integer>() : _reportIDs;
	}
	
	/**
	 * Returns wether a Pilot has voted in a particular Water Cooler poll.
	 * @param pilotID the Pilot's database ID
	 * @return TRUE if the Pilot has voted, otherwise FALSE
	 */
	public boolean hasVoted(int pilotID) {
		if (_pollVotes == null)
			return false;
		
		// Check if the pilot has voted in the poll
		for (Iterator<PollVote> i = _pollVotes.iterator(); i.hasNext(); ) {
			PollVote v = i.next();
			if (v.getPilotID() == pilotID)
				return true;
		}
		
		return false;
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
	 * Updates the date/time this thread will be a &quot;sticky&quot; until.
	 * @param dt the date/time
	 * @see MessageThread#getStickyUntil()
	 */
	public void setStickyUntil(Date dt) {
		if (dt == null)
			_stickyUntil = null;
		else if (dt.getTime() > System.currentTimeMillis())
			_stickyUntil = dt;
	}

	/**
	 * Updates wether the thread will be sticky in the current channel only.
	 * @param isSticky TRUE if the thread is stick in the current channel, otherwise FALSE
	 * @see MessageThread#getStickyInChannelOnly()
	 */
	public void setStickyInChannelOnly(boolean isSticky) {
		_stickyChannel = isSticky;
	}
	
	/**
	 * Updates the thread's content report count.
	 * @param count the number of times it has been reported, or -1 if no reports allowed
	 * @see MessageThread#getReportCount()
	 */
	public void setReportCount(int count) {
		_reportCount = count;
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
	 * Sets this thread's poll flag.
	 * @param poll TRUE if the thread has a poll, otherwise FALSE
	 * @see MessageThread#getPoll()
	 */
	public void setPoll(boolean poll) {
		_isPoll = poll;
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
			_posts = new TreeSet<Message>();
			_lastUpdatedOn = msg.getCreatedOn();
			_lastUpdateID = msg.getAuthorID();
		} else if (msg.getCreatedOn().after(_lastUpdatedOn)) {
			_lastUpdatedOn = msg.getCreatedOn();
			_lastUpdateID = msg.getAuthorID();
		}

		_posts.add(msg);
	}
	
	/**
	 * Adds a thread update entry to this thread.
	 * @param upd the update entry bean
	 * @see MessageThread#getUpdates()
	 */
	public void addUpdate(ThreadUpdate upd) {
		if (_updates == null)
			_updates = new TreeSet<ThreadUpdate>();
		
		_updates.add(upd);
	}
	
	/**
	 * Adds a database ID to the list of users reporting a thread for content.
	 * @param id the database ID
	 * @see MessageThread#getReportCount()
	 * @see MessageThread#getReportIDs()
	 */
	public void addReportID(int id) {
		if (_reportIDs == null)
			_reportIDs = new HashSet<Integer>();
		
		_reportIDs.add(new Integer(id));
	}

	/**
	 * Adds poll options to this thread.
	 * @param opts a Colllection of PollOption beans
	 * @see MessageThread#addOption(PollOption)
	 * @see MessageThread#getOptions()
	 * @see MessageThread#addVotes(Collection)
	 */
	public void addOptions(Collection<PollOption> opts) {
		if (_pollOptions == null)
			_pollOptions = new LinkedHashSet<PollOption>(opts);
		else
			_pollOptions.addAll(opts);
	}
	
	/**
	 * Adds a URL to the list of linked Image URLs for this Message Thread.
	 * @param url the image URL
	 * @param desc the image description
	 * @see MessageThread#addImageURL(LinkedImage)
	 * @see MessageThread#getImageURLs()
	 */
	public void addImageURL(String url, String desc) {
		LinkedImage img = new LinkedImage(_imgURLs.size() + 1, url);
		img.setDescription(desc);
		addImageURL(img);
	}

	/**
	 * Adds a URL to the list of linked Image URLs for this Message Thread.
	 * @param img the LinkedImage bean
	 * @see MessageThread#addImageURL(String, String)
	 * @see MessageThread#getImageURLs()
	 */
	public void addImageURL(LinkedImage img) {
		if (_imgURLs == null)
			_imgURLs = new LinkedHashSet<LinkedImage>();

		_imgURLs.add(img);
	}
	
	/**
	 * Adds a single poll option to this thread.
	 * @param opt the PollOption bean
	 * @see MessageThread#addOptions(Collection)
	 * @see MessageThread#getOptions()
	 * @see MessageThread#addVotes(Collection)
	 */
	public void addOption(PollOption opt) {
		if (_pollOptions == null)
			_pollOptions = new LinkedHashSet<PollOption>();
		
		_pollOptions.add(opt);
	}

	/**
	 * Adds poll votes to this thread.
	 * @param votes a Collection of PollVote beans
	 * @see MessageThread#getVotes()
	 * @see MessageThread#addOptions(Collection)
	 */
	public void addVotes(Collection<PollVote> votes) {
		if (_pollVotes == null)
			_pollVotes = new HashSet<PollVote>(votes);
		else
			_pollVotes.addAll(votes);
	}

	/**
	 * Returns this thread's messages.
	 * @return a List of posts, or an empty list if no messages have been added
	 * @see MessageThread#getPostCount()
	 * @see MessageThread#getLastPost()
	 * @see MessageThread#addPost(Message)
	 */
	public List<Message> getPosts() {
		return (_posts == null) ? new ArrayList<Message>() : new ArrayList<Message>(_posts);
	}
	
	/**
	 * Returns this thread's update history
	 * @return a Collection of ThreadUpdate beans
	 * @see MessageThread#addUpdate(ThreadUpdate)
	 */
	public Collection<ThreadUpdate> getUpdates() {
		return (_updates == null) ? new TreeSet<ThreadUpdate>() : new ArrayList<ThreadUpdate>(_updates);
	}
	
	/**
	 * Returns the last post in a message thread.
	 * @return the last post, or null if no messages have been added.
	 * @see MessageThread#getPosts()
	 * @see MessageThread#getLastPost()
	 * @see MessageThread#addPost(Message) 
	 */
	public Message getLastPost() {
		return (_posts == null) ? null : _posts.last();
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