<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ACARS Server Data</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="view" pad="default" space="default">
<tr class="title">
 <td class="left caps" colspan="7">ACARS CONNECTION POOL INFORMATION</td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td width="10%">ID</td>
 <td width="20%">USER</td>
 <td width="10%">FLIGHT NUMBER</td>
 <td width="10%">FLIGHT ID</td> 
 <td width="25%">REMOTE ADDRESS</td>
 <td width="10%">MESSAGES</td>
 <td>BYTES</td>
</tr>

<!-- Table Connection Data -->
<c:forEach var="con" items="${acarsPool}">
<tr>
 <td class="priB"><fmt:hex value="${con.ID}" /></td>
 <td class="pri bld"><el:cmd url="profile" linkID="0x${con.user.ID}">${con.user.name}</el:cmd></td>
<c:if test="${con.flightID == 0}">
 <td class="bld">N/A</td>
 <td>N/A</td>
</c:if>
<c:if test="${con.flightID > 0}">
 <td class="sec bld">${con.flightInfo.flightCode}</td>
 <td><el:cmd url="acarsinfo" linkID="0x${con.flightID}"><fmt:int value="${con.flightID}" /></el:cmd></td>
</c:if>
 <td class="small">${con.remoteAddr} (${con.remoteHost})</td>
 <td><fmt:int value="${con.msgsIn}" /> in, <fmt:int value="${con.msgsOut}" /> out</td>
 <td><fmt:int value="${con.bytesIn}" /> in, <fmt:int value="${con.bytesOut}" /> out</td>
</tr>
<tr>
 <td colspan="7">Socket settings: <fmt:int value="${con.socket.sendBufferSize}" /> bytes out, 
<fmt:int value="${con.socket.receiveBufferSize}" /> bytes in. TCP_NODELAY = ${con.socket.tcpNoDelay}, 
SO_KEEPALIVE = ${con.socket.keepAlive}</td>
</tr>
</c:forEach>
</el:table>
<br />
<!-- Worker threads -->
<el:table className="view" pad="default" space="default">
<tr class="title">
 <td class="left caps" colspan="7">ACARS WORKER THREAD INFORMATION</td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td width="30%">THREAD NAME</td>
 <td width="15%">THREAD STATUS</td>
 <td wdith="10%">EXECUTION COUNT</td>
 <td>CURRENTLY EXECUTING</td>
</tr>

<!-- Table Thread Data -->
<c:forEach var="worker" items="${workers}">
<tr>
 <td class="pri bld">${worker.name}</td>
 <td class="sec">${worker.statusName}</td>
 <td><fmt:int value="${worker.executionCount}" /></td>
 <td class="left">${worker.message}</td>
</tr>
</c:forEach>
</el:table>
<br />
<!-- Server statistics -->
<el:table className="view" pad="default" space="default">
<tr class="title">
 <td class="left caps" colspan="7">ACARS SERVER STATISTICS</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="statIdx" value="${0}" scope="request" />
<c:forEach var="stat" items="${acarsStatNames}">
<c:set var="statIdx" value="${statIdx + 1}" scope="request" />
<tr>
 <td class="left"><span class="pri bld">${stat}</span> <fmt:int value="${acarsStats[statIdx]}" /></td>
</tr>
</c:forEach>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
