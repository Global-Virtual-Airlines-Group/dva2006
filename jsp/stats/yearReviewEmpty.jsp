<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>${year} Year in Review - ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:cspHeader />
<script nonce="${contentSecurity.nonce}">
golgotha.local.updateYear = function() { document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="yearreview.do" method="post" validate="return true">
<el:table className="form">

<!-- Table Header Bar-->
<tr class="title caps">
 <td colspan="2" class="left">${year}<span class="nophone"> YEAR IN REVIEW</span> - <span class="nophone">${pilot.rank.name}&nbsp;</span>${pilot.name} (${pilot.pilotCode})</td>
 <td style="width:150px" class="right">YEAR <el:combo name="year" size="1" idx="*" required="true" firstEntry="[ SELECT ]" options="${years}" onChange="void golgotha.local.updateYear()" /></td>
</tr>
<tr>
 <td colspan="3" class="pri bld mid">You have not logged any flights at <content:airline /> in the ${year} calendar year. Please select another year to view a review of your flight activity in that year.</td>
</tr>
</el:table>

<!-- Table Bottom Bar -->
<el:table className="bar">
<tr>
 <td colspan="3">&nbsp;</td>
</tr>
</el:table>
<el:text name="id" type="hidden" value="${pilot.ID}" />
</el:form>
<br>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
