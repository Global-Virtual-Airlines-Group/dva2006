// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fb;

/**
 * A bean to store a Facebook news feed entry.
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class NewsEntry extends FacebookObject {
	
	private String _body;
	
	private String _caption;
	private String _desc;
	
	private String _url;
	private String _imageURL;
	
	/**
	 * Creates a new News Entry.
	 * @param body the entry body
	 */
	public NewsEntry(String body) {
		this(body, null);
	}
	
	/**
	 * Creates a new News Entry.
	 * @param body the entry body
	 * @param url the URL
	 */
	public NewsEntry(String body, String url) {
		super(null);
		_body = body;
		_url = url;
	}
	
	/**
	 * Returns the comment text.
	 * @return the comment
	 */
	public String getBody() {
		return _body;
	}
	
	/**
	 * Returns the link caption.
	 * @return the caption
	 */
	public String getLinkCaption() {
		return _caption;
	}

	/**
	 * Returns the link description.
	 * @return the description
	 */
	public String getLinkDescription() {
		return _desc;
	}
	
	/**
	 * Retrieves the URL to an image associated wtih this post.
	 * @return the URL of the image, or null
	 */
	public String getImageURL() {
		return _imageURL;
	}
	
	/**
	 * Returns an entry URL.
	 * @return the URL
	 */
	public String getURL() {
		return _url;
	}

	/**
	 * Updates the link caption.
	 * @param caption the caption
	 */
	public void setLinkCaption(String caption) {
		_caption = caption;
	}
	
	/**
	 * Updates the link description.
	 * @param desc the description
	 */
	public void setLinkDescription(String desc) {
		_desc = desc;
	}

	/**
	 * Updates the URL to an image associated wtih this post.
	 * @param url the URL of the image, or null
	 */
	public void setImageURL(String url) {
		_imageURL = url;
	}
}