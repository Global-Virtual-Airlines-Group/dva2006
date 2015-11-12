<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>No <content:airline /> Online Events</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/event/header.jspf" %> 
<%@include file="/jsp/event/sideMenu.jspf" %>
<content:sysdata var="ivaoImg" name="online.ivao.banner" />
<content:sysdata var="vatsimImg" name="online.vatsim.banner" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">NO AVAILABLE SCHEDULED ONLINE EVENTS</div>
<br />
At the moment, no <content:airline /> Online Events are currently available for signup. Please be sure to check 
back often - Events are often scheduled on short notice. <content:airline /> typically holds one 
major event per month, and ad hoc events approximately once every seven to ten days.<br />
<br />
<el:table>
<tr>
 <td><el:link url="http://www.vatsim.net/" target="_NEW"><el:img src="network/${vatsimImg}" className="noborder" caption="VATSIM" /></el:link></td>
 <td><el:link url="http://www.ivao.aero/" target="_NEW"><el:img src="network/${ivaoImg}" className="noborder" caption="IVAO" /></el:link></td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
