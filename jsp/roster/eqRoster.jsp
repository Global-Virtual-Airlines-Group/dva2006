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
<title><content:airline /> Pilot Roster - ${param.id}</title>
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
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="eqroster.do" method="post" validate="return false">
<view:table className="view" pad="default" space="default" cmd="eqroster">
<!-- Table Sort Combo Bar -->
<tr class="title">
 <td colspan="2" class="left">PILOT ROSTER - ${param.id}</td>
 <td><el:box name="showAll" idx="*" value="true" checked="${showAll}" label="Show Inactive Pilots" onChange="void sort()" /></td>
 <td colspan="5" class="right">EQUIPMENT TYPE <el:combo name="id" size="1" options="${eqTypes}" value="${param.id}" onChange="void sort()" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td width="10%">PILOT CODE</td>
 <td width="15%">PILOT NAME</td>
 <td width="25%">E-MAIL ADDRESS</td>
 <td width="16%">RANK</td>
 <td width="6%">HOME</td>
 <td width="8%">FLIGHTS</td>
 <td width="8%">HOURS</td>
 <td>LAST FLIGHT</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${viewContext.results}">
<view:row entry="${pilot}">
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="small"><el:link url="mailto:${pilot.email}">${pilot.email}</el:link></td>
 <td class="pri bld">${pilot.rank}</td>
 <td class="sec">${pilot.homeAirport}</td>
 <td><fmt:int value="${pilot.legs}" /></td>
 <td><fmt:dec value="${pilot.hours}" /></td>
 <td><fmt:date fmt="d" date="${pilot.lastFlight}" default="-" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="8"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="100" labels="Active,Inactive,Retired,On Leave" classes=" ,opt2,opt3,warn" /></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
