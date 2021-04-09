// Copyright 2005, 2009, 2014, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;

import org.deltava.beans.Pilot;

import org.deltava.comparators.PilotComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display all members of all security roles.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class SecurityRoleMembersCommand extends AbstractCommand {

	/**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
	@Override
   public void execute(CommandContext ctx) throws CommandException {

      PilotComparator cmp = new PilotComparator(PilotComparator.RANK);
      try {
         GetPilotDirectory dao = new GetPilotDirectory(ctx.getConnection());

         // Load all members for each role
         Map<String, Collection<Pilot>> results = new TreeMap<String, Collection<Pilot>>();
         Collection<?> roles = (List<?>) SystemData.getObject("security.roles");
         for (Iterator<?> i = roles.iterator(); i.hasNext(); ) {
            String roleName = (String) i.next();
            Collection<Pilot> pilots = new TreeSet<Pilot>(cmp);
            pilots.addAll(dao.getByRole(roleName, ctx.getDB(), false));
            results.put(roleName, pilots);
         }
         
         ctx.setAttribute("roleMap", results, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/admin/securityRoles.jsp");
      result.setSuccess(true);
   }
}