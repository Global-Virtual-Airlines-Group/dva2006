// Copyright 2018, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to track e-mail delivery.
 * @author Luke
 * @version 10.2
 * @since 8.5
 */

public class EMailDelivery extends DatabaseBean implements RemoteAddressBean, ViewEntry {
	
	private final DeliveryType _type;
	private Instant _sendTime;
	private final Instant _deliveryTime;
	private String _addr;
	private int _processTime;
	private String _msgID;
	
	private String _remoteAddr;
	private String _remoteHost;
	
	private String _response;

	/**
	 * Creates the bean.
	 * @param type the DeliveryType
	 * @param pilotID the recipient's database ID
	 * @param deliveryTime the delivery date/time
	 */
	public EMailDelivery(DeliveryType type, int pilotID, Instant deliveryTime) {
		super();
		_type = type;
		setID(pilotID);
		_deliveryTime = deliveryTime;
	}
	
	/**
	 * Returns the message delivery time.
	 * @return the date/time
	 */
	public Instant getDeliveryTime() {
		return _deliveryTime;
	}
	
	/**
	 * Returns the notification type.
	 * @return the DeliveryType
	 */
	public DeliveryType getType() {
		return _type;
	}
	
	/**
	 * Returns the e-mail message ID.
	 * @return the ID
	 */
	public String getMessageID() {
		return _msgID;
	}
	
	/**
	 * Returns the message send time.
	 * @return the date/time
	 */
	public Instant getSendTime() {
		return _sendTime;
	}
	
	/**
	 * Returns the receipient's e-mail address.
	 * @return the address
	 */
	public String getEmail() {
		return _addr;
	}
	
	/**
	 * Returns the remote SMTP message processing time.
	 * @return the time in milliseconds
	 */
	public int getProcessTime() {
		return _processTime;
	}
	
	/**
	 * Returns the remote SMTP server address.
	 * @return the address
	 */
	@Override
	public String getRemoteAddr() {
		return _remoteAddr;
	}
	
	/**
	 * Returns the remote SMTP server host name.
	 * @return the host name
	 */
	@Override
	public String getRemoteHost() {
		return _remoteHost;
	}
	
	/**
	 * Returns the remote SMTP server response message.
	 * @return the message
	 */
	public String getResponse() {
		return _response;
	}
	
	/**
	 * Updates the receipient e-mail address.
	 * @param addr the address
	 */
	public void setEmail(String addr) {
		_addr = addr;
	}
	
	/**
	 * Updates the message ID.
	 * @param id the e-mail message ID
	 */
	public void setMessageID(String id) {
		_msgID = id;
	}
	
	/**
	 * Updates the message processing tme.
	 * @param ms the time in milliseconds
	 */
	public void setProcessTime(int ms) {
		_processTime = Math.max(0, ms);
	}
	
	/**
	 * Updates the message sending time.
	 * @param dt the date/time the message was first sent
	 */
	public void setSendTime(Instant dt) {
		_sendTime = dt;
	}

	/**
	 * Updates the remote SMTP server host name.
	 * @param hostName the host name
	 */
	public void setRemoteHost(String hostName) {
		_remoteHost = hostName;
	}
	
	/**
	 * Updates the remote SMTP server address.
	 * @param addr the server address
	 */
	public void setRemoteAddress(String addr) {
		_remoteAddr = addr;
	}
	
	/**
	 * Updates the remote SMTP server response message.
	 * @param msg the message
	 */
	public void setResponse(String msg) {
		_response = msg;
	}
	
	@Override
	public String getRowClassName() {
		switch (_type) {
		case BOUNCE:
			return "warn";
		case COMPLAINT:
			return "error";
		default:
			return null;
		}
	}
}