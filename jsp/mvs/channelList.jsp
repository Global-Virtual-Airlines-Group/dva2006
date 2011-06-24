<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> MVS Persistent Channels</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" cmd="mvschannels">
<tr class="title">
 <td colspan="6" class="left caps">MODERN VOICE SERVER PERSISTENT CHANNELS</td>
</tr>

<!-- Table Header Bar -->
<tr class="title">
 <td width="20%">CHANNEL NAME</td>
 <td width="15%">SAMPLE RATE</td>
 <td width="10%">RANGE</td>
 <td width="10%">USERS</td>
 <td width="15%"><el:cmdbutton url="mvschannel" op="edit" label="NEW CHANNEL" /></td>
 <td class="left">DESCRIPTION</td>
</tr>

<!-- Table Channel Data -->
<c:forEach var="channel" items="${viewContext.results}">
<view:row entry="${channel}">
 <td><el:cmd url="mvschannel" op="edit" link="${channel}" className="pri bld">${channel.name}</el:cmd></td>
 <td>${channel.sampleRate} (<fmt:int value="${channel.sampleRate.rate}" />)</td>
<c:if test="${channel.range == 0}">
 <td class="bld small caps">Unlimited</td>
</c:if>
<c:if test="${channel.range > 0}">
 <td><fmt:int value="${channel.range}" /> miles</td>
</c:if>
 <td class="sec bld"><fmt:int value="${channel.maxUsers}" /></td>
 <td colspan="2" class="left small">${channel.description}</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
