// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.fleet;

import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetInstallerSystemInfo;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display Fleet Installer Log data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstallerLogViewCommand extends AbstractViewCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the start/count/max
      ViewContext vc = initView(ctx);
      
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO
         GetInstallerSystemInfo dao = new GetInstallerSystemInfo(con);
         dao.setQueryStart(vc.getStart());
         dao.setQueryMax(vc.getCount());
         
         // Populate combobox choices
         ctx.setAttribute("osList", dao.getOperatingSystems(), REQUEST);
         ctx.setAttribute("installers", dao.getInstallerCodes(), REQUEST);
         
         // If we are doing a POST, query the log
         if (ctx.getParameter("os") != null) {
            if (!"-".equals(ctx.getParameter("installerCode"))) {
               vc.setResults(dao.getByInstallerCode(ctx.getParameter("installerCode")));
            } else if (!"-".equals(ctx.getParameter("os"))) {
               vc.setResults(dao.getByOperatingSystem(ctx.getParameter("os")));
            } else if (!StringUtils.isEmpty(ctx.getParameter("userCode"))) {
               vc.setResults(dao.getByUserCode(ctx.getParameter("userCode")));
            }
         }
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/fleet/logView.jsp");
      result.setSuccess(true);
   }
}