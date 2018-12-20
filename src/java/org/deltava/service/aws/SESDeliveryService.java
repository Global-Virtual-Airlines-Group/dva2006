// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.aws;

import static javax.servlet.http.HttpServletResponse.*;

import java.sql.Connection;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.json.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.EMailDelivery;

import org.deltava.dao.*;

import org.deltava.service.ServiceException;

/**
 * A Web Service to handle Amazon SES delivery reports.
 * @author Luke
 * @version 8.5
 * @since 8.5
 */

public class SESDeliveryService extends SNSReceiverService {
	
	private static final Logger log = Logger.getLogger(SESDeliveryService.class);

	/**
	 * Executes the Web Service.
	 * @param ctx the SNSContext
	 * @return 200 Always
	 */
	@Override
	protected int execute(SNSContext ctx) throws ServiceException {
		
		SNSPayload sns = ctx.getPayload();
		JSONObject msgo = new JSONObject(sns.getBody().getString("Message"));
		String type = msgo.optString("notificationType").toLowerCase();
		if (!"delivery".equals(type))
			throw error(SC_BAD_REQUEST, "Invalid notification type - " + type);
		
		JSONObject mo = msgo.getJSONObject("mail");
		JSONObject dvo = msgo.optJSONObject("delivery");
		JSONArray ra = mo.getJSONArray("destination");
		Instant sendTime  = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(mo.getString("timestamp")));
		Instant deliveryTime = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(dvo.getString("timestamp")));
		
		try {
			Connection con = ctx.getConnection();
			GetPilot pdao = new GetPilot(con);
			SetEMailDelivery dvwdao = new SetEMailDelivery(con); 

			// Look up the pilots
			for (int x = 0; x < ra.length(); x++) {
				String addr = ra.getString(x);
				Pilot p = pdao.getByEMail(addr);
				if (p == null) {
					log.info("Ignoring message for unknown address " + addr);
					continue;
				}
				
				log.info("Received delivery report for " + p.getName() + "[ " + p.getEmail() + " ]");
				EMailDelivery dv = new EMailDelivery(p.getID(), deliveryTime);
				dv.setSendTime(sendTime);
				dv.setEmail(addr);
				dv.setMessageID(mo.getString("messageId"));
				dv.setProcessTime(dvo.optInt("processingTimeMillis", 0));
				dv.setRemoteAddress(dvo.optString("remoteMtaIp", "0.0.0.0"));
				dv.setRemoteHost(dvo.optString("reportingMTA", "Unknown"));
				dv.setResponse(dvo.optString("smtpResponse", "N/A"));
				dvwdao.write(dv);
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		return SC_OK;
	}

	@Override
	public boolean isLogged() {
		return false;
	}
}