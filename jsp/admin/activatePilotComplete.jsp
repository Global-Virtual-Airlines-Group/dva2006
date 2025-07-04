<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Reactivated</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Pilot Reactivated</div>
<br />
${pilot.name} has been restored to the <content:airline /> Pilot Roster as an Active ${pilot.rank.name} in the ${pilot.equipmentType} equipment program. This Pilot's password has been reset.<br />
<br />
An e-mail message has been sent to ${pilot.name} containing the new password, informing ${pilot.firstName} of this change in status, and welcoming him or her back to <content:airline />.<br />
<br />
To view the Pilot profile for ${pilot.name}, <el:cmd className="sec bld" url="profile" link="${pilot}">Click Here</el:cmd>.<br />
To return to the <content:airline /> Pilot Center, <el:cmd className="sec bld" url="pilotcenter">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
