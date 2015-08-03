// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.socket;

import java.io.*;
import java.util.*;

import org.deltava.beans.ts3.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * An abstract class to handle TS3 server query operations.
 * @author Luke
 * @version 6.1
 * @since 6.1
 */

abstract class TS3ServerQueryDAO extends SocketDAO {
	
	private String _userID;
	private String _pwd;
	
	protected BufferedReader _in;
	protected BufferedWriter _out;

	/**
	 * Sets the TeamSpeak 3 server query credentials
	 * @param userID the userID
	 * @param pwd the password
	 */
	public void setCredentials(String userID, String pwd) {
		_userID = userID;
		_pwd = pwd;
	}
	
	/**
	 * Connects to and authenticates with the TeamSpeak 3 server query interface.
	 * @throws IOException if an error occurs
	 */
	protected void login() throws IOException {
		connect(SystemData.get("airline.voice.ts3.serverquery.host"), SystemData.getInt("airline.voice.ts3.serverquery.port", 10011));
		_in = new BufferedReader(new InputStreamReader(_inStream), 1024);
		_out = new BufferedWriter(new OutputStreamWriter(_outStream), 512);
		exec("login client_login_name={0} client_login_password={1}", _userID, _pwd);
	}
	
	/**
	 * Logs out of the TeamSpeak 3 server query interface.
	 * @throws IOException if an error occurs
	 */
	protected void logout() throws IOException {
		exec("logout");
	}
	
	/**
	 * Sends a TeamSpeak 3 server query command. 
	 * @param msg the command with parameter placeholders
	 * @param params the parameters
	 * @throws IOException if an error occurs
	 */
	protected ServerQueryResponse exec(String msg, Object... params) throws IOException {
		String result = msg;
		if (params != null) {
			for (int x = 0; x < params.length; x++) { 
				Object p = params[x]; String placeHolder = "{" + String.valueOf(x) + "}";
				result = result.replace(placeHolder, (p instanceof Enum) ? String.valueOf(((Enum<?>) p).ordinal()) : QueryEncoder.encode(String.valueOf(p)));
			}
		}

		_out.write(result);
		_out.newLine();
		
		// Check for no data
		String data = _in.readLine();
		if (data.startsWith("error ")) {
			ServerQueryResponse r = parseResponse(data);
			if (r.isError()) throw new IOException(r.getMessage());
		}
		
		// If we get data, get the status line
		String status = _in.readLine();
		ServerQueryResponse r = parseResponse(status);
		r.putAll(parseData(data));
		return r;
	}
	
	/*
	 * Parses a TeamSpeak 3 Server Query status code response.
	 */
	private static ServerQueryResponse parseResponse(String rsp) {
		if (!rsp.startsWith("error ")) throw new IllegalArgumentException("Not a TS3 response");
		int id = 0; String msg = null;
		StringTokenizer tkns = new StringTokenizer(rsp.substring(rsp.indexOf(' ') + 1), " ");
		while (tkns.hasMoreTokens()) {
			String tkn = tkns.nextToken();
			int ofs = tkn.indexOf('=');
			if (ofs == -1) continue;
			String tokenName = tkn.substring(0, ofs);
			switch (tokenName) {
			case "id":
				id = StringUtils.parse(tkn.substring(ofs + 1), -1);
				if (id == 0) return new ServerQueryResponse(0, "ok");
				break;
				
			case "msg":
				msg = QueryEncoder.decode(rsp.substring(ofs + 1));
				break;
			}
		}
		
		return new ServerQueryResponse(id, msg);
	}

	/*
	 * Parses a TeamSpeak 3 Server Query data response.
	 */
	private static Map<String, String> parseData(String rsp) {
		Map<String, String> results = new LinkedHashMap<String, String>();
		StringTokenizer tkns = new StringTokenizer(rsp, " ");
		while (tkns.hasMoreTokens()) {
			String tkn = tkns.nextToken();
			int ofs = tkn.indexOf('=');
			if (ofs > -1)
				results.put(tkn.substring(0, ofs), tkn.substring(ofs + 1));
		}
		
		return results;
	}
}