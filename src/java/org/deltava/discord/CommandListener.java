// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import java.awt.*;

import org.apache.logging.log4j.*;

import org.javacord.api.entity.message.component.*;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.*;

import org.deltava.beans.discord.*;
import org.deltava.util.StringUtils;

public class CommandListener implements org.javacord.api.listener.interaction.SlashCommandCreateListener {
	
	private static final Logger log = LogManager.getLogger(CommandListener.class); 

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent e) {
        SlashCommandInteraction sci = e.getSlashCommandInteraction();
        String cmdName = sci.getCommandName().toLowerCase();

        switch (cmdName) {
            case "addkey" -> addWord(e, false);
            case "dropkey" -> dropWord(e, false);
            case "flywithme" -> newFlyWithMeRequest(e);
            case "addsafe" -> addWord(e, true);
            case "dropsafe" -> dropWord(e, true);
            default -> log.info(String.format("Ignored command - %s", cmdName));
        }
    }

    @SuppressWarnings("static-method")
	public void addWord(SlashCommandCreateEvent e, boolean isSafe) {

        SlashCommandInteraction sci = e.getSlashCommandInteraction();
        String keyType = isSafe ? "safe" : "key";
        
        // Get the keyword
    	String key = getOption(sci, keyType + "word");
    	if (StringUtils.isEmpty(key)) {
    		sci.createImmediateResponder().setContent(String.format("You must supply a %s word to add", keyType)).respond();
    		return;
    	}
    	
        // Add the keyword
        boolean isAdded = Bot.getFilter().add(key, isSafe);
        if (!isAdded) {
        	sci.createImmediateResponder().setContent(String.format("%s word already exists", keyType)).respond();
            return;
        }
        
        sci.createImmediateResponder().setContent(String.format("%s word %s added", keyType, key)).respond();

        //Log to bot-alerts
        Bot.send(Channel.ALERTS, new EmbedBuilder()
                .setTitle(":new: Prohibited Keyword Added")
                .setDescription("A new keyword was added to the list of prohibited words or phrases. The bot will now alert to any message which contains this phrase or a similar one.")
                .setTimestampToNow()
                .setFooter("Keyword Added")
                .addInlineField("User", e.getSlashCommandInteraction().getUser().getDisplayName(e.getSlashCommandInteraction().getServer().get()))
                .addInlineField("Keyword Created", key)
                .setColor(Color.GREEN));
    }

    public static void dropWord(SlashCommandCreateEvent e, boolean isSafe) {

    	SlashCommandInteraction sci = e.getSlashCommandInteraction();
    	String keyType = isSafe ? "safe" : "key";
    	
    	String key = getOption(sci, keyType + "word");
    	if (StringUtils.isEmpty(key)) {
    		sci.createImmediateResponder().setContent(String.format("No %s word specified", keyType)).respond();
    		return;
    	}
    	
    	// Remove the keyword
    	boolean isDropped = Bot.getFilter().delete(key, isSafe);
    	if (!isDropped) {
        	sci.createImmediateResponder().setContent(String.format("%s word does not exist", keyType)).respond();
            return;
        }
    	
    	sci.createImmediateResponder().setContent(String.format("%s word %s removed", keyType, key)).respond();
        
    	//Log to bot-alerts
       	Bot.send(Channel.ALERTS, new EmbedBuilder()
       			.setTitle(":x: Safe Keyword Deleted")
                .setDescription("A safe keyword was deleted from the list of safe words or phrases. The bot will no longer ignore this word/phrase.")
                .setTimestampToNow()
                .setFooter("Safe Word Deleted")
                .addInlineField("User", e.getSlashCommandInteraction().getUser().getDisplayName(e.getSlashCommandInteraction().getServer().get()))
                .addInlineField("Safe Word Deleted", key)
                .setColor(Color.GREEN));
    }

    public static void newFlyWithMeRequest(SlashCommandCreateEvent event) {
        event.getSlashCommandInteraction().respondWithModal("fwm_modal", "Create Fly-With-Me Request",
                ActionRow.of(TextInput.create(TextInputStyle.SHORT, "fwm_dep", "Departure Field")),
                ActionRow.of(TextInput.create(TextInputStyle.SHORT, "fwm_arr", "Arrival Field")),
                ActionRow.of(TextInput.create(TextInputStyle.SHORT, "fwm_net", "Requested Network")));
    }
    
    private static String getOption(SlashCommandInteraction ci, String name) {
    	SlashCommandInteractionOption opt = ci.getOptionByName(name).orElse(null);
    	return (opt == null) ? null : opt.getStringValue().orElse(null);
    }
}