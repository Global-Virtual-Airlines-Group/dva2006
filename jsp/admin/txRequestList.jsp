<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Equipment Transfer Requests</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function refresh(combo)
{
enableElement(combo, false);
var eqType = combo.options[combo.selectedIndex].text;
self.location = '/txrequests.do?eqType=' + eqType;
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
<el:form action="txrequests.do" method="post" validate="return false">
<view:table className="view" pad="default" space="default" cmd="txrequests">
<!-- Top Header Bar -->
<tr class="title caps">
 <td colspan="3" class="left"><content:airline /> EQUIPMENT TRANSFER REQUESTS</td>
 <td colspan="3" class="right">EQUIPMENT PROGRAM <el:combo name="eqType" size="1" options="${activeEQ}" firstEntry="-" value="${param.eqType}" onChange="void refresh(this)" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td width="30%">PILOT NAME</td>
 <td width="10%">PILOT ID</td>
 <td width="15%">CURRENT RANK</td>
 <td width="15%">CURRENT PROGRAM</td>
 <td width="15%"><el:cmd url="txrequests" className="title" sort="TX.EQTYPE,TX.STATUS">REQUESTED PROGRAM</el:cmd></td>
 <td><el:cmd url="txrequests" className="title" sort="TX.CREATED DESC">REQUESTED ON</el:cmd></td>
</tr>

<!-- Table Data -->
<c:forEach var="txreq" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[txreq.ID]}" scope="page" />
<view:row entry="${txreq}">
 <td class="bld"><el:cmd url="txreqview" link="${txreq}">${pilot.name}</el:cmd></td>
 <td class="pri bld"><el:cmd url="profile" link="${pilot}">${pilot.pilotCode}</el:cmd></td>
 <td class="sec bld">${pilot.rank}</td>
 <td>${pilot.equipmentType}</td>
 <td class="pri bld">${txreq.equipmentType}</td>
 <td class="sec"><fmt:date fmt="d" date="${txreq.date}" /></td>
</view:row>
</c:forEach>

<!-- Scroll bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="145" labels="Needs Check Ride,Ride Assigned,Ride Submitted,Complete"
classes="opt2,opt1,opt3, " /></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
