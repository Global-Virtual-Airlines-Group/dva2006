<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ACARS Empty Flight Log</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function switchType(combo)
{
self.location = 'acarsempty.do?id=' + combo.options[combo.selectedIndex].value;
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="acarsempty.do" method="post" validate="return false">
<view:table className="view" space="default" pad="default" cmd="acarsempty">
<!-- View Header Bar -->
<tr class="title">
 <td colspan="4">EMPTY ACARS FLIGHT INFORMATION ENTRIES</td>
 <td colspan="4" class="right">VIEW EMPTY <el:combo name="viewType" idx="*" size="1" options="${displayTypes}" value="${displayType}" onChange="void switchType(this)" /></td>
</tr>

<!-- View Legend Bar -->
<tr class="title caps">
 <td width="10%">ID</td>
 <td width="10%">&nbsp;</td>
 <td width="15%">START/END TIME</td>
 <td width="10%">PILOT CODE</td>
 <td width="20%">PILOT NAME</td>
 <td width="10%">FLIGHT NUMBER</td>
 <td width="12%">ORIGIN</td>
 <td>DESTINATION</td>
</tr>

<!-- Result Data -->
<c:forEach var="flight" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[flight.pilotID]}" scope="request" />
<c:set var="pilotLoc" value="${userData[flight.pilotID]}" scope="request" />
<view:row entry="${info}">
 <td class="pri bld"><el:cmd url="acarsinfo" linkID="0x${flight.ID}"><fmt:int value="${flight.ID}" /></el:cmd></td>
 <td><el:cmdbutton url="acarsdelf" linkID="0x${flight.ID}" label="DELETE" /></td>
 <td><fmt:date t="HH:mm" date="${flight.startTime}" />
<c:if test="${!empty flight.endTime}">
<br /><fmt:date t="HH:mm" date="${flight.endTime}" />
</c:if>
</td>
 <td class="sec bld">${pilot.pilotCode}</td>
 <td class="pri bld"><el:profile location="${pilotLoc}">${pilot.name}</el:profile></td>
 <td class="bld">${flight.flightCode}</td>
 <td class="small">${flight.airportD.name} (<fmt:airport airport="${flight.airportD}" />)</td>
 <td class="small">${flight.airportA.name} (<fmt:airport airport="${flight.airportA}" />)</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="8"><view:pgUp />&nbsp;<view:pgDn /></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
