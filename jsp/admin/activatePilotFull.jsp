<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Reactivate <content:airline /> Pilot - Airline Full</title>
<content:css name="main" />
<content:css name="form" />
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
<content:sysdata var="maxPilots" name="users.max" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Pilot Reactivation - Membership Limit Reached</div>
<br />
<content:airline /> has reached its maximum size of <fmt:int value="${maxPilots}" /> Pilots. Reactivating this Pilot will take
the Airline above its maximum size, to a total of <fmt:int value="${airlineSize + 1}" /> Pilots. Do you wish to continue?<br />
<br />

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:cmdbutton ID="ActivateButton" url="activate" link="${pilot}" op="force" label="ACTIVATE PILOT" /></td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
