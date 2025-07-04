// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import org.apache.logging.log4j.*;

import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.event.message.MessageReplyEvent;

import com.newrelic.api.agent.Trace;

import org.deltava.beans.discord.ChannelName;

/**
 * A class to handle Discord message responses.
 * @author danielw
 * @author luke
 * @version 11.0
 * @since 11.0
 */

public class MessageReplyListener implements org.javacord.api.listener.message.MessageReplyListener {
	
    private static final Logger log = LogManager.getLogger(MessageReplyListener.class);
    
    @Override
    @Trace(dispatcher=true)
    public void onMessageReply(MessageReplyEvent e) {
    	
        // Only handle the message reply if it is a response to a bot message and in an appropriate channel
        if (isGoodChannel(e) && e.getReferencedMessage().getAuthor().isBotUser()) {
            String content = e.getMessageContent();
            if (content.startsWith("!")) {
                log.info("Mod command received from {}", e.getMessageAuthor().getName());
                ModCommandHandler.handle(e);
            }
        }
    }

    private static boolean isGoodChannel(MessageReplyEvent event) {
    	ServerChannel ch = event.getChannel().asServerChannel().orElse(null);
        return (ch != null) && ch.getName().equals(ChannelName.MOD_ALERTS.getName());
    }
}