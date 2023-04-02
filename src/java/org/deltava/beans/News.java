// Copyright 2004, 2005, 2006, 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.Instant;

/**
 * A class for storing System News entries.
 * @author Luke
 * @version 10.6
 * @since 1.0
 */

public class News extends ImageBean implements AuthoredBean {

    private Instant _date = Instant.now();
    private String _subject;
    private String _body;
    private boolean _isHTML;
    private int _bannerWidth;

    private int _authorID;

    /**
     * Create a new System News entry object, with the Date defaulting to the current date/time
     * @param sbj This entry's title
     * @param body This entry's content
     * @throws NullPointerException if sbj is null
     */
    public News(String sbj, String body) {
        super();
        setSubject(sbj);
        setBody(body);
    }

    /**
     * Return the date/entry this entry was created.
     * @return The date/time this System News entry was created
     */
    public Instant getDate() {
        return _date;
    }

    /**
     * Return this entry's Title.
     * @return The Title of this System News entry
     * @see News#setSubject(String)
     */
    public String getSubject() {
        return _subject;
    }

    @Override
    public int getAuthorID() {
        return _authorID;
    }
    
    @Override
	public ImageType getImageType() {
    	return ImageType.NEWS;
    }
    
    /**
     * Returns the width of the banner image.
     * @return the width as a percentage of its parent container
     * @see News#setBannerWidth(int)
     */
    public int getBannerWidth() {
    	return getHasImage() ? _bannerWidth : 0;
    }

    /**
     * Return this entry's Content.
     * @return The Content of this System News entry
     * @see News#setBody(String)
     */
    public String getBody() {
        return _body;
    }
    
    /**
     * Returns if this entry is raw HTML.
     * @return TRUE if the entry is raw HTML, otherwise FALSE
     * @see News#getIsHTML()
     */
    public boolean getIsHTML() {
 	   return _isHTML;
    }

    @Override
    public void setAuthorID(int id) {
       validateID(_authorID, id);
        _authorID = id;
    }

    /**
     * Update the date of this entry.
     * @param d The new date/time for this System News entry
     * @see News#getDate()
     */
    public void setDate(Instant d) {
        _date = d;
    }
    
    /**
     * Updates this subject of this entry.
     * @param subj the new subject
     * @throws NullPointerException if subj is null
     * @see News#getSubject()
     */
    public void setSubject(String subj) {
       _subject = subj.trim();
    }
    
    /**
     * Updates this entry's body content.
     * @param body the new content
     * @see News#getBody()
     */
    public void setBody(String body) {
       _body = body;
    }
    
    /**
     * Updates the width of the banner image.
     * @param w the width as a percentage of the parent container
     * @throws IllegalStateException if no banner image is present
     */
    public void setBannerWidth(int w) {
    	if (!getHasImage())
    		throw new IllegalStateException("No Banner Image");
    	
    	_bannerWidth = Math.max(0,  Math.min(100, w));
    }
    
    /**
     * Updates if this entry is raw HTML.
     * @param html TRUE if raw HTML text, otherwise FALSE
     * @see News#getIsHTML()
     */
    public void setIsHTML(boolean html) {
 	   _isHTML = html;
    }
    
    @Override
    public int compareTo(Object o2) {
        News n2 = (News) o2;
        return -(_date.compareTo(n2.getDate()));
    }
}