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
<title><content:airline /> TeamSpeak 2 Voice Servers</title>
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="ts2URL" name="airline.voice.ts2.url" />

<!-- Main Body Frame -->
<content:region id="main">
<content:airline /> uses TeamSpeak 2 for our private voice infrastructure. Teamspeak is a high-fidelity 
<span class="ita">freeware</span> voice client that runs on 32-bit Windows, Macintosh and Linux systems, and 
<content:airline /> operates several TeamSpeak 2 servers for the use of our members.<br />
<br />
<el:table className="view">
<tr class="title caps">
 <td colspan="5" class="left">AVAILABLE TEAMSPEAK 2 SERVERS</td>
</tr>

<!-- Table Header Row -->
<tr class="title caps">
 <td style="width:30%">SERVER NAME</td>
 <td style="width:12%">USER ID</td>
 <td style="width:8%">MAX USERS</td>
 <td style="width:20%">ATTRIBUTES</td>
 <td>DESCRIPTION</td>
</tr>

<!-- Table Data -->
<c:if test="${!empty ts2servers}">
<c:forEach var="srv" items="${ts2servers}">
<c:set var="client" value="${clientInfo[srv.ID]}" scope="page" />
<c:set var="acarsOnly" value="${srv.ACARSOnly && (!client.isACARS)}" scope="page" />
<view:row entry="${srv}">
<c:if test="${!acarsOnly}">
 <td class="pri bld"><el:link url="${ts2URL}:${srv.port}/?nickname=${client.userID}?loginname=${client.userID}">${srv.name}</el:link></td>
</c:if>
<c:if test="${acarsOnly}">
 <td class="pri bld">${srv.name}</td>
</c:if>
 <td class="sec bld">${client.userID}</td>
 <td><fmt:int value="${srv.maxUsers}" /></td>
 <td><c:if test="${client.autoVoice}"> <span class="ter small bld">AUTO-VOICE</span></c:if>
<c:if test="${client.serverOperator}"> <span class="sec small bld">SERVER OPERATOR</span></c:if>
<c:if test="${client.serverAdmin}"> <span class="pri small bld">SERVER ADMINISTRATOR</span></c:if></td>
 <td class="small left">${srv.description}</td>
</view:row>
</c:forEach>

<!-- Legend Bar -->
<tr class="title">
 <td colspan="5"><view:legend width="120" labels="Active,ACARS Only,Inactive" classes=" ,opt2,warn" /></td>
</tr>
</c:if>
<c:if test="${empty ts2servers}">
<tr>
 <td colspan="5" class="pri bld left">You do not currently have access to any TeamSpeak 2 Servers.</td>
</tr>
</c:if>
</el:table>
<br />
<div class="mid"><el:link url="http://www.teamspeak.com/?page=downloads&archive=1">
<el:img src="library/teamspeak.png" className="noborder" caption="Download TeamSpeak 2" x="132" y="46" /></el:link></div>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
