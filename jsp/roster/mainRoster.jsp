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
<title><content:airline /> Pilot Roster</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function sort()
{
document.forms[0].submit();
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
<el:form action="roster.do" method="POST" validate="return false">
<view:table className="view" pad="default" space="default" cmd="roster">
<!-- Table Sort Combo Bar -->
<tr class="title">
 <td colspan="3" class="left">PILOT ROSTER</td>
 <td colspan="5" class="right">SORT BY <el:combo name="sortType" size="1" options="${sortTypes}" value="${viewContext.sortType}" onChange="void sort()" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td width="10%">PILOT CODE</td>
 <td width="30%">PILOT NAME</td>
 <td width="10%">EQUIPMENT</td>
 <td width="16%">RANK</td>
 <td width="6%">HUB</td>
 <td width="8%">FLIGHTS</td>
 <td width="8%">HOURS</td>
 <td>LAST FLIGHT</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${viewContext.results}">
<tr>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><el:cmd url="profile" linkID="0x${pilot.ID}">${pilot.firstName} ${pilot.lastName}</el:cmd></td>
 <td class="sec bld">${pilot.equipmentType}</td>
 <td class="pri bld">${pilot.rank}</td>
 <td class="sec">${pilot.homeAirport}</td>
 <td><fmt:int value="${pilot.legs}" /></td>
 <td><fmt:dec value="${pilot.hours}" /></td>
 <td><fmt:date fmt="d" date="${pilot.lastFlight}" /></td>
</tr>
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
