// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import static java.nio.charset.StandardCharsets.UTF_8;
import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import java.io.*;
import java.util.*;

import org.json.*;

import org.deltava.dao.DAOException;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to makde Claude API calls.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

abstract class ClaudeDAO extends DAO {

	protected int _maxTokens = 1500;
	private final SequencedCollection<ClaudeMessage> _msgs = new ArrayList<ClaudeMessage>();
	private ClaudeModel _model = ClaudeModel.HAIKU55;
	private String _sysPrompt;
	
	private int _inTokens;
	private int _outTokens;
	
	private static final String API_V1_URL = "https://api.anthropic.com/v1/messages";
	
	/* 
	 * Claude message type.
	 */
	enum MessageType {
		USER, ASSISTANT
	}
	
	// Claude message
	private record ClaudeMessage(MessageType type, String message) { }
	
	/**
	 * Claude API error data encapsulation class.
	 */
	public class ClaudeException extends HTTPDAOException {
		private final String _msg;

		ClaudeException(String type, String msg) {
			super(type, SC_BAD_REQUEST);
			_msg = msg;
			setLogStackDump(false);
		}
		
		@Override
		public String getMessage() {
			return String.format("%s (%s)", super.getMessage(), _msg);
		}
	}
	
	/**
	 * Adds a prompt message.
	 * @param mt the message type
	 * @param text the message text
	 * @throws IllegalArgumentException if two of the same types of messages are enqueued in a row
	 */
	protected void addMessage(MessageType mt, String text) {
		ClaudeMessage msg = new ClaudeMessage(mt, text);
		if (!_msgs.isEmpty() && (_msgs.getLast().type == mt))
			throw new IllegalArgumentException(String.format("Last message in queue is also %s", mt));
		
		_msgs.add(msg);
	}
	
	/**
	 * Updates the system prompt for this call.
	 * @param sp the system prompt
	 */
	protected void setSystemPrompt(String sp) {
		_sysPrompt = sp;
	}
	
	/**
	 * Updates the model to use.
	 * @param m a ClaudeModel
	 */
	protected void setModel(ClaudeModel m) {
		_model = m;
	}
	
	/**
	 * Returns the number of input tokens used.
	 * @return the number of tokens, or zero if the API has not been called yet
	 */
	public int getInputTokens() {
		return _inTokens;
	}
	
	/**
	 * Returns the number of output tokens used.
	 * @return the number of tokens, or zero if the API has not been called yet
	 */
	public int getOutputTokens() {
		return _outTokens;
	}

	/**
	 * Generates the Claude API payload
	 * @return a JSONObject
	 */
	protected JSONObject generatePayload() {
		
		JSONObject jo = new JSONObject();
		jo.put("max_tokens", _maxTokens);
		jo.put("model", _model.getID());
		jo.put("stream", false);
		
		// Add system prompt
		if (!StringUtils.isEmpty(_sysPrompt)) {
			JSONObject so = new JSONObject();
			so.put("text", _sysPrompt);
			so.put("type", "text");
			jo.accumulate("system", so);
		}
		
		// Add user prompts
		for (ClaudeMessage msg : _msgs) {
			JSONObject mo = new JSONObject();
			mo.put("content", msg.message);
			mo.put("role", msg.type.name().toLowerCase());
			jo.accumulate("messages", mo);
		}
		
		JSONUtils.ensureArrayPresent(jo, "messages");
		return jo;
	}
	
	/**
	 * Executes the CLaude API call.
	 * @return JSONObject the JSON API output
	 * @throws DAOException if an error occurs
	 */
	protected JSONObject send() throws DAOException {
		try {
			setReturnErrorStream(true);
			init(API_V1_URL);
			setMethod("POST");
			setCompression(Compression.GZIP, Compression.DEFLATE, Compression.BROTLI);
			setRequestHeader("Content-Type", "application/json");
			setRequestHeader("X-Api-Key", SystemData.get("security.key.claude"));
			setRequestHeader("anthropic-version", "2023-06-01");

			try (OutputStream out = getOut()) {
				out.write(generatePayload().toString().getBytes(UTF_8));
				out.flush();
			}
			
			// If we're an error, throw a status code exception, except a 400 in which case we parse the error
			int statusCode = getResponseCode();
			if (statusCode == SC_BAD_REQUEST) {
				String errorData = downloadStream();
				try {
					JSONObject eo = new JSONObject(new JSONTokener(errorData));
					if (eo.has("error")) {
						JSONObject io = eo.getJSONObject("error");
						throw new ClaudeException(io.getString("type"), io.optString("message", "???"));
					}
					
					throw new HTTPDAOException(API_V1_URL, statusCode);
				} catch (JSONException je) {
					throw new DAOException(String.format("Error parsing error message - %s", errorData));
				}
			} else if (statusCode > SC_BAD_REQUEST)
				throw new HTTPDAOException(API_V1_URL, statusCode);
		
			// Parse the response, read token counts
			try (InputStream in = getIn()) {
				JSONObject jo = new JSONObject(new JSONTokener(in));
				_inTokens = jo.optInt("input_tokens");
				_outTokens = jo.optInt("output_tokens");
				return jo;
			}
		} catch (IOException e) {
			throw new DAOException(e);
		} finally {
			reset();
		}
	}
}