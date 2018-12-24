// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.aws;

import static javax.servlet.http.HttpServletResponse.*;

import java.sql.Connection;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.apache.log4j.Logger;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;
import org.deltava.beans.system.DeliveryType;
import org.deltava.beans.system.EMailDelivery;
import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Service to handle Amazone SES feedback SNS messages.
 * @author Luke
 * @version 8.5
 * @since 8.5
 */

public class SESFeedbackService extends SNSReceiverService {

	private static final Logger log = Logger.getLogger(SESFeedbackService.class);

	/**
	 * Executes the Web Service.
	 * @param ctx the SNSContext
	 * @return 200 Always
	 */
	@Override
	public int execute(SNSContext ctx) throws ServiceException {
		
		String channelName = SystemData.get("cooler.system.channel");

		SNSPayload sns = ctx.getPayload();
		JSONObject msgo = new JSONObject(sns.getBody().getString("Message"));
		String type = msgo.getString("notificationType").toLowerCase();
		JSONObject mo = msgo.getJSONObject("mail");
		try {
			Connection con = ctx.getConnection();
			GetPilot pdao = new GetPilot(con);
			SetCoolerMessage cwdao = new SetCoolerMessage(con);
			SetEMailDelivery dvwdao = new SetEMailDelivery(con);
			
			// Get the Channel
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel c = cdao.get(channelName);
			if (c == null)
				throw new DAOException("Unknown notification Channel - " + channelName);
			
			Message msg = new Message(ctx.getUser().getID());
			msg.setRemoteAddr("127.0.0.1");
			msg.setRemoteHost("localhost");
			
			switch (type) {
			case "bounce":
				MessageThread mt = new MessageThread("Amazon SES bounce notification");
				mt.setChannel(c.getName());
				
				StringBuilder buf = new StringBuilder("Amazon SES bounce notification\n\n");
				JSONObject bo = msgo.getJSONObject("bounce");
				buf.append("Type: ");
				buf.append(bo.optString("bounceType", "?")).append(" / ");
				buf.append(bo.optString("bounceSubType", "?")).append("\n\n");
				
				JSONArray rcpts = bo.getJSONArray("bouncedRecipients");
				for (int x = 0; x < rcpts.length(); x++) {
					JSONObject ro = rcpts.getJSONObject(x);
					Pilot p = pdao.getByEMail(ro.getString("emailAddress"));
					if (p == null) {
						buf.append(ro.getString("emailAddress"));
						buf.append(" (UNKNOWN) - ");
					} else {
						EMailDelivery dv = new EMailDelivery(DeliveryType.BOUNCE, p.getID(), Instant.now());
						dv.setSendTime(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(bo.getString("timestamp"))));
						dv.setEmail(ro.getString("emailAddress"));
						
						buf.append(p.getName());
						buf.append(" (").append(p.getPilotCode()).append(") - ");
					}
						
					if (ro.has("action")) {
						buf.append(ro.getString("action"));
						buf.append(", status: ");
						buf.append(ro.optString("status", "?"));
						if (ro.has("diagnosticCode"))
							buf.append("\nmessage: ").append(ro.getString("diagnosticCode"));
					}
					
					buf.append("\n");
				}

				msg.setBody(buf.toString());
				mt.addPost(msg);
				cwdao.write(mt);
				break;

			case "complaint":
				mt = new MessageThread("Amazon SES complaint notification");
				mt.setChannel(c.getName());
				mt.setStickyUntil(Instant.now().plusSeconds(86400));
				
				buf = new StringBuilder("Amazon SES complaint notification\n\n");
				JSONObject co = msgo.getJSONObject("complaint");
				if (co.has("complaintFeedbackType"))
					buf.append("Type: ").append(co.getString("complaintFeedbackType")).append("\n");
				if (co.has("userAgent"))
					buf.append("From: ").append(co.getString("userAgent")).append("\n");
				
				rcpts = co.getJSONArray("complainedRecipients");
				for (int x = 0; x < rcpts.length(); x++) {
					JSONObject ro = rcpts.getJSONObject(x);
					Pilot p = pdao.getByEMail(ro.getString("emailAddress"));
					if (p == null) {
						buf.append(ro.getString("emailAddress"));
						buf.append(" (UNKNOWN)\n");
					} else {
						EMailDelivery dv = new EMailDelivery(DeliveryType.COMPLAINT, p.getID(), Instant.now());
						dv.setSendTime(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(co.getString("timestamp"))));
						dv.setEmail(ro.getString("emailAddress"));
						dv.setResponse(co.getString("complaintFeedbackType"));
						dvwdao.write(dv);
						
						buf.append(p.getName());
						buf.append(" (").append(p.getPilotCode()).append(")\n");
					}
				}

				msg.setBody(buf.toString());
				mt.addPost(msg);
				cwdao.write(mt);
				break;

			case "delivery":
				log.info("Delivery report to " + mo.getJSONArray("destination"));
				break;

			default:
				log.warn("Unknown feedback type - " + type);
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		return SC_OK;
	}
}