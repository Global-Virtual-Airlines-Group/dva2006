// Copyright 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import java.util.*;
import java.awt.Color;
import java.time.*;
import java.time.format.DateTimeFormatter;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.discord.ChannelName;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A utility class to generate Discord responses.
 * @author Luke
 * @version 12.4
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
                .addField("Channel", "#" + e.getChannel().asServerChannel().get().getName())
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
	 * @param userName the Dicsord User name
	 * @param p the Pilot
	 * @param roles the Discord security Roles
	 * @param nickName the nickname 
	 * @return an EmbedBuilder
	 */
    static EmbedBuilder createNick(String userName, Pilot p, Collection<Role> roles, String nickName) {
    	EmbedBuilder eb = new EmbedBuilder()
    		.setColor(new Color(1, 0, 161))
   			.setFooter("Nickname Assignment")
            .setTitle(":exclamation: Nickname Assigned")
            .setThumbnail(String.format("https://%s/img/favicon/favicon-32x32.png", SystemData.get("airline.url")))
            .setDescription(Bot.findRole("administrator").getMentionTag() + " I've assigned a nickname to the following member.")
            .addInlineField("User", userName)
            .addInlineField("Name", nickName)
            .setTimestampToNow();
    	
    	roles.forEach(r -> eb.addInlineField("Server Role", r.getName()));
    	return eb;
    }
    
    /**
     * Creates a registration status message, to provide feedback to the Pilot about registration status.
     * @param e the MessageCreateEvent
     * @param p the Pilot
     * @param roles the Discord security Roles
     * @return an EmbedBuilder
     */
    static EmbedBuilder createStatus(MessageCreateEvent e, Pilot p, Collection<Role> roles) {
    	
    	boolean s1Complete = p.getExternalIDs().containsKey(ExternalID.DISCORD);
    	boolean s2Complete = !roles.isEmpty();
    	
    	String code = SystemData.get("airline.code");
    	String host = SystemData.get("airline.url");
    	EmbedBuilder eb = new EmbedBuilder()
   			.setColor(s1Complete && s2Complete ? Color.GREEN : Color.YELLOW)
            .setFooter(":info: Registration Status")
            .setDescription("Discord Server Reigstration Status")
            .setTimestampToNow()
            .setThumbnail(String.format("https://%s/img/favicon/favicon-32x32.png", host))
            .setFooter(String.format("%s Discord New Member Registration", code))
    		.addField(String.format(":%s_square: - Link Discord and Web Site", s1Complete ? "green" : "red"), s1Complete ? "COMPLETE" : "INCOMPLETE")
    		.addField(String.format(":%s_square: - Request Access Roles", s2Complete ? "green" : "red"), s2Complete ? "COMPLETE" : "INCOMPLETE");
    	
    	if (!s1Complete)
    		eb.setUrl(String.format("https://%s/discordreg.do?id=%d", host, Long.valueOf(e.getMessageAuthor().getId())));
   	
    	return eb;
    }
    
    /**
	 * Generates an embedded nickname error message.
	 * @param userName the Discord User name
	 * @param p the Pilot
	 * @param roles the Discord security Roles 
	 * @return an EmbedBuilder
	 */
    static EmbedBuilder createNicknameError(String userName, Pilot p, Collection<Role> roles) {
    	String code = SystemData.get("airline.code");
    	EmbedBuilder eb = new EmbedBuilder()
    		.setColor(Color.RED)
    		.setTitle("Unable to Assign Nickname")
    		.setFooter("User Creation Error")
    		.setTimestampToNow()
    		.setDescription(String.format("%s I ran into an error when attempting to assign a nickname to the following user. Please have an administrator update the user's nickname by hand in accordance with %s policy.", Bot.findRole("administrator").getMentionTag(), code))
    		.addInlineField("User", userName)
    		.addInlineField("Name", p.getName())
    		.addInlineField("Pilot ID", p.getPilotCode());
    	
    	roles.forEach(r -> eb.addInlineField("Server Role", r.getName()));
    	return eb;
    }
    
    /**
     * Generates a Flight Report submission message.
     * @param fr the FlightReport
     * @param p the Pilot
     * @return an EmbedBuilder
     */
    static EmbedBuilder createPIREP(FlightReport fr, Pilot p) {
    	boolean isACARS = (fr instanceof ACARSFlightReport);
    	boolean isApproved = (fr.getStatus() == FlightStatus.OK);
    	Duration d = isACARS ? ((ACARSFlightReport) fr).getBlockTime() : Duration.ofMinutes(fr.getLeg() * 6);
    	EmbedBuilder eb = new EmbedBuilder()
    		.setTitle(String.format("%s Completed", fr.getShortCode()))
    		.setThumbnail(String.format("https://%s/img/favicon/favicon-32x32.png", SystemData.get("airline.url")))
    		.setFooter((isACARS ? "ACARS " : "") + "Flight Report")
    		.setTimestampToNow()
    		.setColor(isApproved ? Color.GREEN : new Color(1, 0, 161))
    		.setDescription(String.format("A Flight has been completed and a new %s Flight Report has been %s at the %s web site.", isACARS ? "ACARS" : "manual", isApproved ? "approved" : "filed", SystemData.get("airline.name")))
    		.addInlineField("Link", String.format("https://%s/l/fr/%s", SystemData.get("airline.domain"), Integer.toHexString(fr.getID())))
    		.addInlineField("Route", String.format("%s - %s", fr.getAirportD().getIATA(), fr.getAirportA().getIATA()))
    		.addInlineField("Equipment", fr.getEquipmentType())
    		.addInlineField("Pilot Name", p.getName())
    		.addInlineField("Pilot ID", p.getPilotCode())
    		.addInlineField("Simulator", fr.getSimulator().name())
    		.addInlineField("Status", isApproved ? "Auto-Approved" : "Submitted")
    		.addInlineField("Passengers", (fr.getPassengers() > 0) ? String.valueOf(fr.getPassengers()) : "N/A");

    	// Format duration
    	DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");
		ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(d.getSeconds()), ZoneId.of("Z"));
		eb.addInlineField("Duration", df.format(zdt));
    	
    	return eb;
    }
    
    /**
	 * Generates a registration message.
	 * @param id the Discord User UUID
	 * @return an EmbedBuilder
	 */
    static EmbedBuilder register(long id) {
    	String code = SystemData.get("airline.code");
    	String host = SystemData.get("airline.url");
        return new EmbedBuilder()
        	.setTitle(":wave: Welcome Aboard!")
            .setDescription(String.format("Welcome to the %s Discord Server. To gain access, you must be an active member of Delta Virtual Airlines at %s. We are a non-profit organization dedicated to flight simulation and education and are not affiliated with the real Delta Air Lines in any way. To complete your Discord registration with us, follow the steps below.", code, host))
            .setThumbnail(String.format("https://%s/img/favicon/favicon-32x32.png", host))
            .setImage(String.format("https://%s/img/DeltaBanner_delta_2007.png", host))
            .setFooter(String.format("%s Discord New Member Registration", code))
            .setTimestampToNow()
            .setColor(new Color(1, 0, 161))
            .addField("Step 1: Link your Discord and Web site User Accounts", String.format("To associate your discord account with your %s pilot ID and receive access to the rest of the server, follow this personalized link and sign into your %s account:\n\nhttps://%s/discordreg.do?id=%d",code, code, host, Long.valueOf(id)))
            .addField("Step 2: Request your Roles", "Return to the #" + ChannelName.WELCOME.getName() + " channel and send \"!roles\" when you've completed linking your accounts and your Discord roles will be assigned.")
            .addField("Didn't work?", String.format("If you follow the above process and are still not able to gain access, open a help desk ticket here: https://%s/helpdesk.do", host))
            .addField("Check Status", "You can check the status of your registration process by typing \"!status\"");
    }
    
    /**
     * Returns a welcome message.
     * @param e the MessageCreateEvent
     * @return an EmbedBuilder
     */
    static EmbedBuilder welcome(MessageCreateEvent e) {
    	String code = SystemData.get("airline.code");
    	String host = SystemData.get("airline.url");
    	return new EmbedBuilder()
    		.setTitle("Welcome Aboard!")
            .setThumbnail(String.format("https://%s/img/favicon/favicon-32x32.png", host))
            .setColor(new Color(1, 0, 100))
            .setDescription(String.format("Welcome to the %s Discord server! Here are some commands you can use in this channel:", SystemData.get("airline.code")))
            .setFooter(String.format("%s Discord New Member Registration", code))
            .setTimestampToNow()
            .addInlineField("!link", "Link your Discord and Web Site accounts")
            .addInlineField("!roles", "Assign Discord roles based on your web site access")
            .addInlineField("!status", "View Discord Registration Status")
            .addInlineField("!help", "This help message");
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
    
    /**
     * Returns a keyword list message. 
     * @param isSafe TRUE if a safe word list, otherwise FALSE
     * @param keywords a Collection of keywords
     * @return an EmbedBuilder
     */
    static EmbedBuilder showKeys(boolean isSafe, Collection<String> keywords) {
    	
    	// Convert keywords
    	int idx = 0; StringBuilder buf = new StringBuilder();
    	for (String kw : keywords)
    		buf.append(++idx).append(" - ").append(kw).append('\n');
    	
    	return new EmbedBuilder()
    		.setTitle(String.format("%s Word List", isSafe? "Accepted" : "Prohibited"))
    		.setDescription(String.format("This is the list of current %s words", isSafe? "Accepted" : "Prohibited"))
			.setTimestampToNow()
			.setFooter("Keyword List")
			.addInlineField("keywords", buf.toString());
    }

    /**
     * Creates a content warning message.
     * @param author the Author name
     * @param channel the Channel name
     * @param msg the warning message
     * @return an EmbedBuilder
     */
    static EmbedBuilder createWarning(String author, String channel, String msg) {
    	return new EmbedBuilder()
    		.setTitle(":warning: Content Warning")
    		.setTimestampToNow()
    		.setColor(Color.RED)
    		.addField("Channel", "#" + channel)
    		.addInlineField("Author", author)
    		.addInlineField("Info", msg)
    		.setFooter("Content Warning");
    }
}