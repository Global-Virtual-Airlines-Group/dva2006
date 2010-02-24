// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command for users to view Promotion Eligibility. 
 * @author Luke
 * @version 3.0
 * @since 3.0
 */

public class PromotionEligibilityCommand extends AbstractTestHistoryCommand {

	private class EligibilityMessage implements ViewEntry {
		private boolean _isOK;
		private boolean _isEligible;
		private String _msg;
		
		EligibilityMessage(String msg) {
			this(false, false, msg);
		}
		
		EligibilityMessage(boolean isOK, boolean isEligible, String msg) {
			super();
			_isOK = isOK;
			_isEligible = isEligible;
			_msg = msg;
		}
		
		public String getRowClassName() {
			if (!_isOK && _isEligible)
				return "opt2";
			return _isOK ? null : "warn";
		}
		
		public String toString() {
			return _msg;
		}
	}
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
        // Determine who to display
        int id = ctx.isUserInRole("HR") ? ctx.getID() : ctx.getUser().getID();
        if (id == 0)
        	id = ctx.getUser().getID();
		
		try {
			Connection con = ctx.getConnection();
			
            // Initialize the testing history helper
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(id);
            TestingHistoryHelper testHistory = initTestHistory(p, con);
            ctx.setAttribute("pilot", p, REQUEST);
            
            // Get all of the equipment programs
            GetEquipmentType eqdao = new GetEquipmentType(con);
            Collection<EquipmentType> eqTypes = eqdao.getActive();
            
            // Get the exam profile DAO
            GetExamProfiles epdao = new GetExamProfiles(con);
            
            // Iterate through each equipment type
            Map<EquipmentType, EligibilityMessage> eqData = new TreeMap<EquipmentType, EligibilityMessage>();
            for (Iterator<EquipmentType> i = eqTypes.iterator(); i.hasNext(); ) {
            	EquipmentType eq = i.next();
            	if (eq.getName().equals(p.getEquipmentType())) {
            		eqData.put(eq, new EligibilityMessage(true, true, "You are currently in this Equipment program."));
            		continue;
            	}
            	
            	// Check if we've passed the FO exams
            	Collection<String> examNames = eq.getExamNames(Ranks.RANK_FO); 
            	if (!testHistory.hasPassed(examNames)) {
            		Collection<String> msgs = new ArrayList<String>();
            		for (String examName : examNames) {
            			ExamProfile ep = epdao.getExamProfile(examName);
            			if (!testHistory.hasPassed(Collections.singleton(examName))) {
            				try {
            					testHistory.canWrite(ep);
            					msgs.add("You are eligible to take but have not passed the " + examName + " examination.");
            				} catch (IneligibilityException ie) {
            					msgs.add("You have not passed and cannot take the " + examName + " examination - " + ie.getMessage());
            				}
            			} else
            				msgs.add("You have passed the " + examName + " examination.");
            		}
            		
            		eqData.put(eq, new EligibilityMessage(StringUtils.listConcat(msgs, "\n")));
            		continue;
            	}
            	
            	// Check if we've passed the check ride
            	if (!testHistory.hasCheckRide(eq)) {
            		try {
            			testHistory.canRequestCheckRide(eq);
            			eqData.put(eq, new EligibilityMessage(false, true, "You are elgibile to request but have not passed the Check Ride for this Equipment program."));
            		} catch (IneligibilityException ie) {
            			eqData.put(eq, new EligibilityMessage("You are not elgibile to request the Check Ride for this Equipment program - " + ie.getMessage()));
            		}
            		
            		continue;
            	}
            	
            	// 	Check if we can switch
            	if (!testHistory.canRequestRatings(eq)) {
             		eqData.put(eq, new EligibilityMessage(true, true, "You are eligible to switch to this Equipment program."));
             		continue;
            	}

            	// Figure out what new ratings we can get
            	Collection<String> extraRatings = new TreeSet<String>(CollectionUtils.getDelta(eq.getRatings(), p.getRatings()));
           		eqData.put(eq, new EligibilityMessage(false, true, "You are eligible to switch to or seek the following additional ratings (" +
           				StringUtils.listConcat(extraRatings, ", ") + ") in this Equipment program."));
            }
			
			// Save the equipment type data
            ctx.setAttribute("eqData", eqData, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/promotionEligibility.jsp");
		result.setSuccess(true);
	}
}