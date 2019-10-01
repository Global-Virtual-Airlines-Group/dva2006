// Copyright 2010, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.hr;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.crypt.MessageDigester;

import org.deltava.dao.*;

import org.deltava.service.ServiceContext;
import org.deltava.service.ServiceException;
import org.deltava.service.WebService;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to handle Facecbook deauthorization requests.
 * @author Luke
 * @version 8.7
 * @since 3.4
 */

public class FacebookDeauthorizeService extends WebService {
	
	private static final Logger log = Logger.getLogger(FacebookDeauthorizeService.class);

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		String userID = null;
		try {
			String raw = ctx.getParameter("signed_request");
			int ofs = raw.indexOf('.');
			Base64 b64 = new Base64(true);
			String sig = MessageDigester.convert(b64.decode(raw.substring(0, ofs)));
			String rawPayload = raw.substring(ofs + 1);
			byte[] payloadBytes = b64.decode(rawPayload);
			
			// Initialize the sig
			String appSecret = SystemData.get("users.facebook.secret");
			Mac mac = Mac.getInstance("HMACSHA256");
		    SecretKeySpec secret = new SecretKeySpec(appSecret.getBytes(), "HMACSHA256");
		    mac.init(secret);
		    byte[] digest = mac.doFinal(rawPayload.getBytes());
		    String calcSig = MessageDigester.convert(digest);

			// Validate the signature
		    if (!calcSig.equals(sig))
		    	throw new IllegalArgumentException("Invalid Signature");
		    
		    // Parse
			JSONTokener jtk = new JSONTokener(new StringReader(new String(payloadBytes)));
			JSONObject payload = new JSONObject(jtk);
			userID = payload.getString("user_id");
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
		
		try {
			Connection con = ctx.getConnection();
			
			// Find the pilots
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			Map<Integer, Pilot> pilots = pdao.getByIMAddress(userID);
			
			// Start transaction
			ctx.startTX();
			
			// Remove the FB data
			SetPilot pwdao = new SetPilot(con);
			SetStatusUpdate sudao = new SetStatusUpdate(con);			
			for (Iterator<Pilot> i = pilots.values().iterator(); i.hasNext(); ) {
				Pilot p = i.next();
				log.info("Deauthorizing " + p.getName());
				p.setIMHandle(IMAddress.FB, null);
				p.setIMHandle(IMAddress.FBTOKEN, null);
				
				// Create status update bean
				StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.EXT_AUTH);
				upd.setDescription("Facebook Deauthorize Callback");
				upd.setAuthorID(p.getID());
				
				// Write
				pwdao.write(p);
				sudao.write(upd);
			}
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		return SC_OK;
	}
}