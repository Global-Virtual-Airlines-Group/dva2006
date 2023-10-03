// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import java.awt.Color;
import java.util.Collection;

import org.deltava.beans.Pilot;
import org.deltava.beans.discord.ChannelName;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;

/**
 * A utility class to generate Discord responses.
 * @author Luke
 * @version 11.1
 * @since 11.1
 */

class EmbedGenerator {

	// static class
	private EmbedGenerator() {
		super();
	}
	
	/**
	 * Generates an embedded error message.
	 * @param e the MessageCreateEvent
	 * @param ex the Exception
	 * @return an EmbedBuilder
	 */
    static EmbedBuilder createError(MessageCreateEvent e, Exception ex) {
    	return new EmbedBuilder()
                .setTitle(":warning: Internal Error")
                .setColor(Color.RED)
                .addInlineField("User", e.getMessageAuthor().getDisplayName())
                .addInlineField("Error", ex.getMessage())
                .setThumbnail("https://www.deltava.org/img/favicon/v852/favicon-32x32.png")
                .setTimestampToNow();
    }
    
    /**
	 * Generates an embedded keyword message.
	 * @param e the MessageCreateEvent
	 * @param keywords a Collection of keywords 
	 * @return an EmbedBuilder
	 */
    static EmbedBuilder createKeyword(MessageCreateEvent e, Collection<String> keywords) {

    	java.util.List<Role> roles = e.getMessageAuthor().asUser().get().getRoles(e.getServer().get());
        boolean isStaff = roles.stream().anyMatch(r -> r.getName().toLowerCase().contains("staff"));
        return new EmbedBuilder()
                .setTitle(":warning: Auto-Mod Keyword Detected")
                .setDescription(Bot.findRole("HR").getMentionTag() + " A possible match for the following prohibited word(s) or phrase(s) was detected in the below message: " + StringUtils.listConcat(keywords, ", "))
                .addField("Message Content", e.getMessageContent())
                .addField("In channel", "#" + e.getChannel().asServerChannel().get().getName())
                .addInlineField("User", e.getMessageAuthor().getDisplayName())
                .setColor(Color.RED)
                .setFooter("Prohibited Remarks Detected")
                .addInlineField("Role", isStaff ? "Staff" : "Pilot")
                .setTimestampToNow();
    }
    
    static EmbedBuilder createInsufficientAccess(MessageCreateEvent e) {
    	String code = SystemData.get("airline.code");
    	return new EmbedBuilder().setColor(Color.RED)
    			.setFooter("Insufficient Access")
    			.setTitle(":octagonal-sign: Access Denied")
    			.addInlineField("User", e.getMessageAuthor().getDisplayName())
    			.setDescription(String.format("You do not have the necessary access to join the %s Discord server", code))
    			.addInlineField("User", e.getMessageAuthor().getDisplayName())
    			.setTimestampToNow();
    }
    
    /**
	 * Generates an embedded temporary nickname message.
	 * @param e the MessageCreateEvent
	 * @param p the Pilot
	 * @param roleName the Discord security role 
	 * @return an EmbedBuilder
	 */
    static EmbedBuilder createTemporaryNick(MessageCreateEvent e, Pilot p, String roleName) {
    	return new EmbedBuilder().setColor(Color.BLUE)
                .setFooter("Temporary Nickname Assignment")
                .setTitle(":exclamation: Temporary Nickname Assigned")
                .setDescription(Bot.findRole("administrator").getMentionTag() + " I've assigned a temporary nickname to the following member.")
                .addInlineField("Name", e.getMessageAuthor().getDisplayName())
                .addInlineField("Permissions Level", roleName)
                .setTimestampToNow();
    }
    
    /**
	 * Generates an embedded nickname error message.
	 * @param e the MessageCreateEvent
	 * @param p the Pilot
	 * @param roleName the Discord security role 
	 * @return an EmbedBuilder
	 */
    static EmbedBuilder createNicknameError(MessageCreateEvent e, Pilot p, String roleName) {
    	String code = SystemData.get("airline.code");
    	return new EmbedBuilder()
        .setColor(Color.RED)
        .setTitle("Unable to Assign Nickname")
        .setFooter("User Creation Error")
        .setTimestampToNow()
        .setDescription(String.format("%s I ran into an error when attempting to assign a nickname to the following user. Please have an administrator update the user's nickname by hand in accordance with %s policy.", Bot.findRole("administrator").getMentionTag(), code))
        .addInlineField("User", e.getMessageAuthor().getDiscriminatedName())
        .addInlineField("Name", p.getName())
        .addInlineField("Pilot ID", p.getPilotCode())
        .addInlineField("Role", roleName);
    }
    
    /**
	 * Generates an embedded welcome message.
	 * @param id the Discord User UUID
	 * @return an EmbedBuilder
	 */
    static EmbedBuilder register(long id) {
    	String code = SystemData.get("airline.code");
    	String host = SystemData.get("airline.url");
        return new EmbedBuilder()
                .setTitle(":wave: Welcome Aboard!")
                .setDescription("Welcome to the Delta Virtual Airlines Discord Server. To gain access, you must be an active member of Delta Virtual Airlines at www.deltava.org. We are a non-profit organization dedicated to flight simulation and education and are not affiliated with the real Delta Air Lines in any way. To complete your discord registration with us, follow the steps below.")
                .setThumbnail(String.format("https://%s/img/favicon/v852/favicon-32x32.png", host))
                .setImage(String.format("https://%s/img/DeltaBanner_delta_2007.png", host))
                .setFooter(String.format("%s Discord New Member Registration", code))
                .setTimestampToNow()
                .setColor(new Color(1, 0, 100))
                .addField("Step 1: Link your Discord and DVA User Accounts", String.format("To associate your discord account with your %s pilot ID and receive access to the rest of the server, follow this personalized link and sign into your %s account:\n\nhttps://%s/discordreg.do?id=" + id, code, code, host))
                .addField("Step 2: Request your Roles", "Return to the #" + ChannelName.WELCOME.getName() + " channel and send \"done\" when you've completed linking your accounts and your roles will be assigned.")
                .addField("Didn't work?", String.format("If you follow the above process and are still not able to gain access, open a help desk ticket here: https://%s/helpdesk.do", host));
    }
}