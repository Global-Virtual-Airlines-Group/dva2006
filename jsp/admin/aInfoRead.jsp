<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>Virtual Airline - ${aInfo.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:googleAnalytics />
<content:js name="common" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">VIRTUAL AIRLINE PROFILE - ${aInfo.name}</td>
</tr>
<tr>
 <td class="label">Name</td>
 <td class="data pri bld">${aInfo.name}</td>
</tr>
<tr>
 <td class="label">Airline Code</td>
 <td class="data sec bld">${aInfo.code}</td>
</tr>
<tr>
 <td class="label">Domain Name</td>
 <td class="data">${aInfo.domain}</td>
</tr>
<tr>
 <td class="label">Database</td>
 <td class="data bld">${aInfo.DB}</td> 
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><c:if test="${aInfo.canTransfer}"><span class="pri bld small">Airline allows inbound Pilot transfers</span></c:if>
<c:if test="${aInfo.canTransfer && aInfo.historicRestricted}"><br></c:if>
<c:if test="${aInfo.historicRestricted}"><span class="ter bld small">Historic Routes require Historic Aircraft</span></c:if>
<c:if test="${aInfo.historicRestricted && aInfo.allowMultiAirline}"><br></c:if>
<c:if test="${aInfo.allowMultiAirline}"><span class="bld small">Airline allows Pilot accounts in multiple Airlines</span></c:if>
<c:if test="${aInfo.allowMultiAirline && aInfo.autoApprove}"><br></c:if>
<c:if test="${aInfo.autoApprove}"><span class="pri bld small">Automatically Approves ACARS Flight Reports with optimal Flight Scores</span></c:if></td>
</tr>
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit}"><el:cmdbutton url="ainfo" linkID="${aInfo.code}" op="edit" label="EDIT VIRTUAL AIRLNE PROFILE" /></c:if>
</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
