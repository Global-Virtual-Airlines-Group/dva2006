// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import java.util.*;
import java.awt.Color;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import org.deltava.beans.Pilot;
import org.deltava.beans.discord.ChannelName;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

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
	 * @param userName the User Name
	 * @param actionName the action being performed
	 * @param ex the Exception
	 * @return an EmbedBuilder
	 */
    static EmbedBuilder createError(String userName, String actionName, Exception ex) {
    	return new EmbedBuilder()
                .setTitle(":warning: Internal Error")
                .setColor(Color.RED)
                .addInlineField("User", userName)
                .addInlineField("Action", actionName)
                .addInlineField("Error", ex.getMessage())
                .setThumbnail(String.format("https://%s/img/favicon/favicon-32x32.png", SystemData.get("airline.url")))
                .setTimestampToNow();
    }
    
    /**
	 * Generates an keyword warning message.
	 * @param e the MessageCreateEvent
	 * @param keywords a Collection of keywords 
	 * @return an EmbedBuilder
	 */
    static EmbedBuilder createWarning(MessageCreateEvent e, Collection<String> keywords) {

    	Optional<User> ou = e.getMessageAuthor().asUser();
    	List<Role> roles = ou.isEmpty() ? Collections.emptyList() : ou.get().getRoles(e.getServer().get());
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
    
    /**
     * Generates an insufficient access message.
     * @param e the MessageCreateEvent
     * @return an EmbedBuilder
     */
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
	 * Generates a nickname message.
	 * @param e the MessageCreateEvent
	 * @param p the Pilot
	 * @param roleName the Discord security role
	 * @param nickName the nickname 
	 * @return an EmbedBuilder
	 */
    static EmbedBuilder createNick(MessageCreateEvent e, Pilot p, String roleName, String nickName) {
    	return new EmbedBuilder().setColor(Color.BLUE)
                .setFooter("Nickname Assignment")
                .setTitle(":exclamation: Nickname Assigned")
                .setDescription(Bot.findRole("administrator").getMentionTag() + " I've assigned a nickname to the following member.")
                .addInlineField("User", e.getMessageAuthor().getDisplayName())
                .addInlineField("Name", nickName)
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
                .setThumbnail(String.format("https://%s/img/favicon/favicon-32x32.png", host))
                .setImage(String.format("https://%s/img/DeltaBanner_delta_2007.png", host))
                .setFooter(String.format("%s Discord New Member Registration", code))
                .setTimestampToNow()
                .setColor(new Color(1, 0, 100))
                .addField("Step 1: Link your Discord and DVA User Accounts", String.format("To associate your discord account with your %s pilot ID and receive access to the rest of the server, follow this personalized link and sign into your %s account:\n\nhttps://%s/discordreg.do?id=%d",code, code, host, Long.valueOf(id)))
                .addField("Step 2: Request your Roles", "Return to the #" + ChannelName.WELCOME.getName() + " channel and send \"done\" when you've completed linking your accounts and your roles will be assigned.")
                .addField("Didn't work?", String.format("If you follow the above process and are still not able to gain access, open a help desk ticket here: https://%s/helpdesk.do", host));
    }
    
    /**
     * Returns a welcome message.
     * @param e the MessageCreateEvent
     * @return an EmbedBuilder
     */
    static EmbedBuilder welcome(MessageCreateEvent e) {
    	String host = SystemData.get("airline.url");
    	return new EmbedBuilder()
                .setTitle("Welcome Aboard!")
                .setThumbnail(String.format("https://%s/img/favicon/favicon-32x32.png", host))
                .setColor(new Color(1, 0, 100))
                .setDescription(String.format("Welcome to the %s Discord server! Type !roles to register.", SystemData.get("airline.name")))
                .setTimestampToNow();
    }
    
    /**
     * Returns a keyword added message.
     * @param isSafe TRUE if a safe word, otherwise FALSE
     * @param key the keyword
     * @param user the User adding the keyword
     * @return an EmbedBuilder
     */
    static EmbedBuilder wordDeleted(boolean isSafe, String key, String user) {
    	return new EmbedBuilder()
    			.setTitle(String.format(":x: %s Keyword Deleted", isSafe ? "Safe" : "Prohibited"))
    			.setDescription(String.format("A keyword was deleted from the list of %s words or phrases. The bot will no longer ignore this word/phrase.", isSafe ? "accepted" : "prohibited"))
    			.setFooter("Keyword Deleted")
    			.addInlineField("User", user)
    			.addInlineField("Safe Word Deleted", key)
    			.setColor(Color.GREEN)
    			.setTimestampToNow();
    }
  
    /**
     * Returns a keyword removed message.
     * @param isSafe TRUE if a safe word, otherwise FALSE
     * @param key the keyword
     * @param user the User removing the keyword
     * @return an EmbedBuilder
     */
    static EmbedBuilder wordAdded(boolean isSafe, String key, String user) {
    	return new EmbedBuilder()
                .setTitle(String.format(":new: %s keyword Added", isSafe? "Accepted" : "Prohibited"))
                .setDescription(String.format("A new keyword was added to the list of %s words or phrases. The bot will now alert to any message which contains this phrase or a similar one.", isSafe ? "accepted" : "prohibited"))
                .setTimestampToNow()
                .setFooter("Keyword Added")
                .addInlineField("User", user)
                .addInlineField("Keyword Created", key)
                .setColor(Color.GREEN);
    }
}