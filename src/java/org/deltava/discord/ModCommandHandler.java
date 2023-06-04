// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import org.apache.logging.log4j.*;

import org.javacord.api.entity.message.embed.*;
import org.javacord.api.event.message.MessageReplyEvent;

import java.util.*;

import org.deltava.beans.discord.ChannelName;

/**
 * A class to handle moderator commands.
 * @author danielw
 * @author luke
 * @version 11.0
 * @since 11.0
 */

public class ModCommandHandler {

    private static final Logger log = LogManager.getLogger(ModCommandHandler.class);

    public static void handle(MessageReplyEvent e) {

        String msg = e.getMessageContent();
        
        //Check if it has a comment
        int pos = msg.indexOf(' ');
        String cmd = (pos > -1) ? msg.substring(0, pos) : msg;
        String comment = (pos > -1) ? msg.substring(pos + 1) : null;
        	 
        //Get the embed in the referenced message, or ignore if message didn't respond to an embed
        if (e.getReferencedMessage().getEmbeds().isEmpty()) return;

        //Find the type of command used and call the appropriate method
        Embed embed = e.getReferencedMessage().getEmbeds().get(0);
        switch (cmd.toLowerCase()) {
            case "!ignore", "!ig" -> ignoreAlert(e, embed);
            case "!done", "!d" -> handleAlert(e, embed, comment);
            default -> log.info(String.format("Ignored command - %s", cmd));
        }
    }

    public static void ignoreAlert(MessageReplyEvent e, Embed embed) {

        Optional<EmbedField> f = embed.getFields().stream().filter(ef -> ef.getName().equals("Message Content")).findAny();
        String msg = f.isPresent() ? f.get().getValue() : null;

        log.info(String.format("Alert ignored by %s [ Content = %s ]", e.getMessageAuthor().getName(), msg));
        e.getMessage().delete();
        e.getReferencedMessage().delete();
    }

    public static void handleAlert(MessageReplyEvent e, Embed embed, String comment) {
    	
    	Optional<EmbedField> f = embed.getFields().stream().filter(ef -> ef.getName().equals("Message Content")).findAny();
        String msg = f.isPresent() ? f.get().getValue() : null;
        log.info(String.format("Mod alert handled by %s [ Content = %s ]", e.getMessageAuthor().getName(), msg));

        EmbedBuilder builder = embed.toBuilder();
        e.getMessageAuthor().asUser().flatMap(user -> user.getNickname(e.getServer().get())).ifPresent(usr -> builder.addField("Handled By", usr));
        
        builder.addField("Internal Comment", comment);
        builder.setTitle(":white_check_mark: Moderator alert handled");
        builder.setDescription("The following flagged message has been handled by a staff member and requires no further action.");
        builder.setFooter("Alert handled");
        builder.setColor(java.awt.Color.GREEN);
        Bot.send(ChannelName.MOD_ARCHIVE, builder);

        // Delete original message
        e.getMessage().delete();
        e.getReferencedMessage().delete();
    }
}