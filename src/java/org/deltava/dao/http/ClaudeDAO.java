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

	private int _maxTokens = 1500;
	private final SequencedCollection<ClaudeMessage> _msgs = new ArrayList<ClaudeMessage>();
	private final SequencedCollection<String> _sysPrompts = new ArrayList<String>();
	private ClaudeModel _model = ClaudeModel.HAIKU55;
	private Effort _effort = Effort.MEDIUM;
	private ClaudeCacheTTL _ttl = ClaudeCacheTTL.NONE;
	
	private int _inTokens;
	private int _outTokens;
	private int _payloadSize;
	
	private static final String API_V1_URL = "https://api.anthropic.com/v1/messages";
	
	/** 
	 * Claude prompt message types.
	 */
	enum PromptType {
		USER, ASSISTANT
	}
	
	/**
	 * Thinking effort levels.
	 */
	enum Effort {
		LOW, MEDIUM, HIGH, XHIGH, MAX
	}
	
	// Claude message
	private record ClaudeMessage(PromptType type, String message) { }
	
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
	 * @param mt the PromptType
	 * @param text the message text
	 * @throws IllegalArgumentException if two of the same types of messages are enqueued in a row
	 */
	protected void addPrompt(PromptType mt, String text) {
		ClaudeMessage msg = new ClaudeMessage(mt, text);
		if (!_msgs.isEmpty() && (_msgs.getLast().type == mt))
			throw new IllegalArgumentException(String.format("Last message in queue is also %s", mt));
		
		_msgs.add(msg);
	}
	
	/**
	 * Updates the system prompt for this call.
	 * @param sp the system prompt
	 */
	protected void addSystemPrompt(String sp) {
		_sysPrompts.add(sp);
	}
	
	/**
	 * Updates the model to use for this call.
	 * @param m a ClaudeModel
	 */
	protected void setModel(ClaudeModel m) {
		_model = m;
	}
	
	/**
	 * Updates the thinking effort for this call.
	 * @param e an Effort level
	 */
	protected void setEffort(Effort e) {
		_effort = e;
	}
	
	
	/**
	 * Sets the cache expiration policy to use for this call.
	 * @param ttl a ClaudeCacheTTL
	 */
	protected void setTTL(ClaudeCacheTTL ttl) {
		_ttl = ttl;
	}
	
	/**
	 * Updates the maximum number of output tokens to return.
	 * @param maxTokens the number of tokens
	 */
	public void setMaxTokens(int maxTokens) {
		_maxTokens = Math.max(0, maxTokens);
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
	 * Returns the size of the API request payload.
	 * @return the request body size in bytes, or zero if the API has not been called yet
	 */
	public int getPayloadSize() {
		return _payloadSize;
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
		
		// Add output configuration
		JSONObject oco = new JSONObject();
		oco.put("effort", _effort.name().toLowerCase());
		jo.put("output_config", oco);
		
		// Add TTL
		if (_ttl != ClaudeCacheTTL.NONE) {
			JSONObject co = new JSONObject();
			co.put("type", "ephemeral");
			co.put("ttl", _ttl.getValue());
			jo.put("cache_control", co);
		}
		
		// Add system prompt
		for (String sysMsg : _sysPrompts) {
			JSONObject so = new JSONObject();
			so.put("text", sysMsg);
			so.put("type", "text");
			jo.accumulate("system", so);
		}
		
		System.out.println(jo.toString(2));
		
		// Add user prompts
		for (ClaudeMessage msg : _msgs) {
			JSONObject mo = new JSONObject();
			mo.put("content", msg.message);
			mo.put("role", msg.type.name().toLowerCase());
			jo.accumulate("messages", mo);
		}
		
		JSONUtils.ensureArrayPresent(jo, "messages", "system");
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

			// Send the POST body
			try (OutputStream out = getOut()) {
				String js = generatePayload().toString();
				_payloadSize = js.length();
				out.write(js.getBytes(UTF_8));
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
				JSONObject uo = jo.optJSONObject("usage");
				if (uo != null) {
					_inTokens = uo.getInt("input_tokens");
					_outTokens = uo.getInt("output_tokens");
				}
				
				// Check for token exhaustion
				if (_outTokens >= _maxTokens)
					throw new IllegalArgumentException("Token Limit reached");
				
				return jo;
			}
		} catch (IOException e) {
			throw new DAOException(e);
		} finally {
			reset();
		}
	}
}