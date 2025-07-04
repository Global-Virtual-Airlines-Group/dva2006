// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.Instant;

/**
 * A bean to store virtual airline partner information.
 * @author Luke
 * @version 10.6
 * @since 10.3
 */

public class PartnerInfo extends ImageBean implements Auditable {

	private String _name;
	private String _url;
	private String _desc;
	
	private Instant _lastRefer;
	private int _referCount;
	private int _priority;
	
	/**
	 * Creates the bean.
	 * @param name the partner name 
	 */
	public PartnerInfo(String name) {
		super();
		setName(name);
	}
	
	@Override
	public String getAuditID() {
		return getHexID();
	}
	
	@Override
	public ImageType getImageType() {
		return ImageType.PARTNER;
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
	 * Returns the partner priority.
	 * @return the priority
	 */
	public int getPriority() {
		return _priority;
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
	 * Updates the partner priority.
	 * @param p the priority
	 */
	public void setPriority(int p) {
		_priority = Math.max(0, p);
	}
}