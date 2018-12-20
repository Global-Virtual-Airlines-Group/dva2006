// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.aws;

import org.json.JSONObject;

/**
 * A class to store Amazon SNS payloads.
 * @author Luke
 * @version 8.5
 * @since 8.5
 */

class SNSPayload {

	private final String _id;
	private final String _topic;
	private final String _type;
	
	private JSONObject _body;
	
	/**
	 * Creates the message payload.
	 * @param id the message ID
	 * @param topic the SNS topic name
	 * @param type the message type
	 */
	public SNSPayload(String id, String topic, String type) {
		_id = id;
		_topic = topic;
		_type = type;
	}

	/**
	 * Returns the message ID.
	 * @return the ID
	 */
	public String getID() {
		return _id;
	}
	
	/**
	 * Returns the SNS topic name.
	 * @return the topic name
	 */
	public String getTopic() {
		return _topic;
	}

	/**
	 * Returns the message type.
	 * @return the type
	 */
	public String getType() {
		return _type;
	}

	/**
	 * Returns the JSON payload of this message.
	 * @return a JSONObject
	 */
	public JSONObject getBody() {
		return _body;
	}
	
	/**
	 * Returns the SNS signature version number.
	 * @return the signature version, 0 if none or -1 if no body
	 */
	public int getSignatureVersion() {
		return (_body == null) ? -1 : _body.optInt("SignatureVersion", 0);
	}
	
	/**
	 * Updates the message payload.
	 * @param jo a JSONObject
	 */
	public void setBody(JSONObject jo) {
		_body = jo;
	}
	
	@Override
	public int hashCode() {
		return _id.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_topic);
		buf.append('-');
		return buf.append(_id).toString();
	}
}