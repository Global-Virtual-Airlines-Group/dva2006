<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> ${netInfo.network} Server Information</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.setNetwork = function(combo) {
	self.location = '/netservers.do?id=' + escape(golgotha.form.getCombo(combo));
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>
<content:sysdata var="networks" name="online.networks" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="netservers.do" method="get" validate="return false">
<view:table cmd="netservers">
<tr class="title">
 <td colspan="3" class="left">NETWORK SERVERS - ${netInfo.network} - VALID AS OF <fmt:date date="${netInfo.validDate}" t="HH:mm" /></td>
 <td colspan="2" class="right nophone">SELECT NETWORK <el:combo name="ID" size="1" idx="1" onChange="void golgotha.local.setNetwork(this)" options="${networks}" value="${netInfo.network}" /></td>
</tr>

<!-- Server Title Bar -->
<tr class="title caps">
 <td style="width:20%">NAME</td>
 <td style="width:15%">IP ADDRESS</td>
 <td class="nophone" style="width:25%">LOCATION</td>
 <td style="width:10%">USERS</td>
 <td class="nophone" >COMMENT</td>
</tr>

<!-- Table Server Data -->
<c:forEach var="srv" items="${netInfo.servers}">
<c:set var="ipInfo" value="${addrInfo[srv]}" scope="page" />
<tr>
 <td class="pri bld">${srv.name}</td>
 <td class="bld">${srv.address}</td>
<c:if test="${!empty ipInfo}">
 <td class="nophone" ><el:flag countryCode="${ipInfo.country.code}" caption="${ipInfo.location}" /> ${ipInfo.location}</td>
</c:if>
<c:if test="${empty ipInfo}">
 <td>${srv.location}</td>
</c:if>
 <td class="sec bld"><fmt:int value="${srv.connections}" /></td>
 <td class="left nophone">${srv.comment}</td>
</tr>
</c:forEach>
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</view:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
