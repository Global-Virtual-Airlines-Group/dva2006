<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Online Users</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.sortBy = function(combo) {
	self.location = '/users.do?sortOpt=' + escape(golgotha.form.getCombo(combo));
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:attr attr="isHR" roles="HR" value="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="users.do" method="get" validate="return false">
<el:table className="view">
<tr class="title">
 <td colspan="4" class="left caps"><fmt:int value="${pilots.size()}" /> CURRENTLY LOGGED IN USERS
<c:if test="${!empty maxUserDate}"><span class="nophone"> - MAXIMUM <fmt:int value="${maxUsers}" /> on <fmt:date date="${maxUserDate}" /></span></c:if></td>
 <td class="nophone"><el:cmd url="users" op="map">VIEW MAP</el:cmd></td>
 <td class="right nophone">SORT BY <el:combo name="sortOpt" idx="*" size="1" options="${sortOptions}" value="${sortOpt}" onChange="void golgotha.local.sortBy(this)" /></td>
</tr>

<!-- Pilot Title Bar -->
<tr class="title caps">
 <td>PILOT ID</td>
 <td style="max-width:30%">PILOT NAME</td>
 <td class="nophone">RANK</td>
 <td>EQUIPMENT TYPE</td>
 <td>LOCATION</td>
 <td class="nophone">JOINED ON</td>
</tr>

<!-- Pilot Data Bar -->
<c:forEach var="session" items="${pilots}">
<c:set var="pilot" value="${session.person}" scope="page" />
<tr>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td class="bld"><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="pri nophone">${pilot.rank.name}</td>
 <td class="sec">${pilot.equipmentType}</td>
<c:choose>
<c:when test="${isHR && (!empty session.addressInfo)}">
 <td class="small"><el:flag countryCode="${session.addressInfo.country.code}" /> ${session.addressInfo.location}</td>
</c:when>
<c:otherwise>
 <td>${pilot.location}</td>
</c:otherwise>
</c:choose>
 <td class="small nophone"><fmt:date date="${pilot.createdOn}" fmt="d" d="EEEE MMMM dd, yyyy" /></td>
</tr>
<c:if test="${isHR}">
<view:row entry="${session}">
 <td colspan="6">Logged in since <fmt:date date="${pilot.lastLogin}" />, from <b>${pilot.loginHost}</b><br />
<span class="small">${session.userAgent}</span></td>
</view:row>
</c:if>
</c:forEach>
<c:if test="${empty pilots}">
<tr>
 <td colspan="6" class="pri bld caps">NO CURRENTLY LOGGED IN <span class="nophone"><content:airline /> </span>USERS</td>
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
