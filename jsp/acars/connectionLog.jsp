<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Connection Log</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:js name="datePicker" />
<script async>
golgotha.local.validate = function(f) {
    if (!golgotha.form.check()) return false;
    golgotha.form.submit(f);
    return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarslogc.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="4">ACARS CONNECTION LOG</td>
</tr>
<tr>
 <td class="label">Pilot Code</td>
 <td class="data" colspan="3"><el:text name="pilotCode" idx="*" size="7" max="8" value="${param.pilotCode}" /></td>
</tr>
<tr>
 <td class="label">Start Date/Time</td>
 <td class="data"><el:text name="startDate" idx="*" size="10" max="10" value="${param.startDate}" />&nbsp;
<el:text name="startTime" idx="*" size="8" max="8" value="${param.startTime}" />&nbsp;
<el:button label="CALENDAR" onClick="void show_calendar('forms[0].startDate')" /></td>
 <td class="label">End Date/Time</td>
 <td class="data"><el:text name="endDate" idx="*" size="10" max="10" value="${param.endDate}" />&nbsp;
<el:text name="endTime" idx="*" size="8" max="8" value="${param.endTime}" />&nbsp;
<el:button label="CALENDAR" onClick="void show_calendar('forms[0].endDate')" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SEARCH CONNECTION LOG" /></td>
</tr>
</el:table>
</el:form>

<c:choose>
<c:when test="${!empty viewContext.results}">
<!-- Table Log Results -->
<view:table cmd="acarslogc">
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:10%">ID</td>
 <td style="width:15%">DATE/TIME</td>
 <td style="width:10%">PILOT CODE</td>
 <td style="width:25%" class="nophone">PILOT NAME</td>
 <td style="width:5%">BUILD</td>
 <td style="width:5%" class="nophone">TEXT</td>
 <td class="nophone">HOST NAME</td>
</tr>

<!-- Log Entries -->
<c:forEach var="entry" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[entry.pilotID]}" scope="page" />
<c:set var="pilotLoc" value="${userData[entry.pilotID]}" scope="page" />
<view:row entry="${entry}">
 <td class="pri bld"><fmt:hex value="${entry.connectionID}" /></td>
 <td class="small"><fmt:date date="${entry.startTime}" /></td>
 <td class="sec bld">${pilot.pilotCode}</td>
 <td class="pri bld nophone"><el:profile location="${pilotLoc}">${pilot.name}</el:profile></td>
 <td class="sec small bld"><fmt:int value="${entry.clientBuild}" /></td>
 <td class="nophone"><fmt:int value="${entry.messageCount}" /></td>
 <td class="small nophone">${entry.remoteHost} (${entry.remoteAddr})</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</c:when>
<c:when test="${doSearch}">
<el:table className="view">
<tr>
 <td class="pri bld">No Connections matching your search criteria were found in the ACARS log database.</td>
</tr>
</el:table>
</c:when>
</c:choose>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
