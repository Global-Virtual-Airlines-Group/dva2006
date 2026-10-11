// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.*;
import java.util.*;

import org.json.*;

import org.deltava.dao.DAOException;

import org.deltava.util.JSONUtils;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to makde Claude API calls.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

abstract class ClaudeDAO extends DAO {

	protected int _maxTokens;
	private final Collection<String> _msgs = new ArrayList<String>();
	private ClaudeModel _model = ClaudeModel.HAIKU55;
	private String _sysPrompt;
	
	private int _inTokens;
	private int _outTokens;
	
	/**
	 * Adds a prompt message.
	 * @param msg the message
	 */
	protected void addMessage(String msg) {
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
		jo.put("maxTokens", _maxTokens);
		jo.put("model", _model.getID());
		
		// Add system prompt
		if (!StringUtils.isEmpty(_sysPrompt)) {
			JSONObject so = new JSONObject();
			so.put("text", _sysPrompt);
			so.put("type", "text");
			jo.put("system", so);
		}
		
		// Add user prompts
		for (String msg : _msgs) {
			JSONObject mo = new JSONObject();
			mo.put("content", msg);
			mo.put("role", "user");
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
			init("https://api.anthropic.com/v1/messages");
			setMethod("POST");
			setRequestHeader("Content-Type", "application/json");
			setRequestHeader("X-Api-Key", SystemData.get("security.key.claude"));
			try (OutputStream out = getOut()) {
				out.write(generatePayload().toString().getBytes(UTF_8));
				out.flush();
			}
		
			// Parse the response, read token counts
			try (InputStream in = getIn()) {
				JSONObject jo = new JSONObject(new JSONTokener(in));
				_inTokens = jo.optInt("input_tokens");
				_outTokens = jo.optInt("output_tokens");
				return jo;
			}
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
}