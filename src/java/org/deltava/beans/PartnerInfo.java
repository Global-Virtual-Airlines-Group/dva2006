// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.Instant;

import org.deltava.util.StringUtils;

/**
 * A bean to store virtual airline partner information.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class PartnerInfo extends ImageBean {

	private String _name;
	private String _url;
	private String _desc;
	
	private Instant _lastRefer;
	private int _referCount;
	
	private String _bannerExt;
	
	/**
	 * Creates the bean.
	 * @param name the partner name 
	 */
	public PartnerInfo(String name) {
		super();
		setName(name);
	}

	/**
	 * Returns the partner name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the partner URL.
	 * @return the URL
	 */
	public String getURL() {
		return _url;
	}

	/**
	 * Returns the partner description text.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns the partner referral count.
	 * @return the count
	 */
	public int getReferCount() {
		return _referCount;
	}
	
	/**
	 * Returns the time of the last partner referral.
	 * @return the date/time of the last link out
	 */
	public Instant getLastRefer() {
		return _lastRefer;
	}
	
	/**
	 * Queries if the Partner has a banner image.
	 * @return TRUE if the partner has an image, otherwise FALSE
	 */
	public boolean getHasBanner() {
		return (_bannerExt != null);
	}
	
	/**
	 * Updates the partner name.
	 * @param name the name
	 */
	public void setName(String name) {
		_name = name.trim();
	}
	
	/**
	 * Updates the partner URL.
	 * @param url the URL
	 */
	public void setURL(String url) {
		_url = url;
	}
	
	/**
	 * Updates the partner description text.
	 * @param desc the description
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}
	
	/**
	 * Updates the partner referral count.
	 * @param cnt the referral count
	 */
	public void setReferCount(int cnt) {
		_referCount = Math.max(0, cnt);
	}
	
	/**
	 * Updates the time of the last partner referral.
	 * @param dt the date/time of the last link out
	 */
	public void setLastRefer(Instant dt) {
		_lastRefer = dt;
	}
	
	/**
	 * Sets if this Partner has a banner image available.
	 * @param ext the banner extension, or null
	 */
	public void setBannerExtension(String ext) {
		_bannerExt = StringUtils.isEmpty(ext) ? null : ext.toLowerCase();
	}
}