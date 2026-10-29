// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.finance;

import java.time.Instant;

/**
 * A bean to store Stripe transaction data.
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class Transaction implements java.io.Serializable {
	
	private final String _id;
	private final Instant _dt;
	private int _amount;
	
	private String _merchantName;
	private String _merchantCity;
	private String _merchantState;
	private String _merchantZIP;
	
	private String _category;

	/**
	 * Creates the Transaction object.
	 * @param id the transaction ID
	 * @param dt the date/time of the transaction
	 */
	public Transaction(String id, Instant dt) {
		super();
		_id = id;
		_dt = dt;
	}

	/**
	 * Returns the transaction ID
	 * @return the ID
	 */
	public String getID() {
		return _id;
	}
	
	/**
	 * Returns the date/time of the transaction.
	 * @return the transaction date/time
	 */
	public Instant getDate() {
		return _dt;
	}
	
	/**
	 * Returns the amount of the transaction.
	 * @return the amount in cents
	 */
	public int getAmount() {
		return _amount;
	}
	
	/**
	 * Returns the merchant category.
	 * @return the category
	 */
	public String getCategory() {
		return _category;
	}
	
	/**
	 * Returns the merchant name.
	 * @return the name
	 */
	public String getMerchantName() {
		return _merchantName;
	}
	
	/**
	 * Returns the merchant city.
	 * @return the city
	 */
	public String getMerchantCity() {
		return _merchantCity;
	}
	
	/**
	 * Returns the merchant state.
	 * @return the state code
	 */
	public String getMerchantState() {
		return _merchantState;
	}
	
	/**
	 * Returns the merchant ZIP code.
	 * @return the ZIP code
	 */
	public String getMerchantZIP() {
		return _merchantZIP;
	}
	
	/**
	 * Updates the merchant category.
	 * @param category the category
	 */
	public void setCategory(String category) {
		_category = category;
	}
	
	/**
	 * Updates the merchant name.
	 * @param name the name
	 */
	public void setMerchantName(String name) {
		_merchantName = name;
	}
	
	/**
	 * Updates the merchant location.
	 * @param city the city
	 * @param state the state
	 * @param zip the ZIP code
	 */
	public void setMerchantLocation(String city, String state, String zip) {
		_merchantCity = city;
		_merchantState = state;
		_merchantZIP = zip;
	}
	
	@Override
	public int hashCode() {
		return _id.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof Transaction tx) && (_id.equalsIgnoreCase(tx._id));
	}
	
	@Override
	public String toString() {
		return _id;
	}
}