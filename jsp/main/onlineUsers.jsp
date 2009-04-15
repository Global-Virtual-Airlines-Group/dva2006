<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Users</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function sortBy(combo)
{
var sortCode = combo.options[combo.selectedIndex].value;
self.location = '/users.do?sortOpt=' + sortCode;
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:filter roles="HR">
<c:set var="isHR" value="${true}" scope="request" /></content:filter>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="users.do" method="get" validate="return false">
<el:table className="view" pad="default" space="default">
<tr class="title">
 <td colspan="4" class="left caps"><fmt:int value="${fn:sizeof(pilots)}" /> CURRENTLY LOGGED IN USERS
<c:if test="${!empty maxUserDate}"> - MAXIMUM <fmt:int value="${maxUsers}" /> on <fmt:date date="${maxUserDate}" /></c:if></td>
 <td><el:cmd url="users" op="map">VIEW MAP</el:cmd></td>
 <td class="right">SORT BY <el:combo name="sortOpt" idx="*" size="1" options="${sortOptions}" value="${sortOpt}" onChange="void sortBy(this)" /></td>
</tr>

<!-- Pilot Title Bar -->
<tr class="title caps">
 <td width="10%">PILOT ID</td>
 <td width="20%">PILOT NAME</td>
 <td width="12%">RANK</td>
 <td width="13%">EQUIPMENT TYPE</td>
 <td width="20%">LOCATION</td>
 <td>JOINED ON</td>
</tr>

<!-- Pilot Data Bar -->
<c:forEach var="session" items="${pilots}">
<c:set var="pilot" value="${session.person}" scope="request" />
<tr>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td class="bld"><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="pri">${pilot.rank}</td>
 <td class="sec">${pilot.equipmentType}</td>
<c:choose>
<c:when test="${isHR && (!empty session.addressInfo)}">
 <td class="small"><el:flag countryCode="${session.addressInfo.countryCode}" /> ${session.addressInfo.location}</td>
</c:when>
<c:otherwise>
 <td>${pilot.location}</td>
</c:otherwise>
</c:choose>
 <td class="small"><fmt:date date="${pilot.createdOn}" fmt="d" d="EEEE MMMM dd, yyyy" /></td>
</tr>
<c:if test="${isHR}">
<tr>
 <td colspan="6">Logged in since <fmt:date date="${pilot.lastLogin}" />, from <b>${pilot.loginHost}</b><br />
<span class="small">${session.userAgent}</span></td>
</tr>
</c:if>
</c:forEach>
<c:if test="${empty pilots}">
<tr>
 <td colspan="6" class="pri bld">NO CURRENTLY LOGGED IN <content:airline /> USERS</td>
</tr>
</c:if>
<tr class="title">
 <td colspan="6">&nbsp;</td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
