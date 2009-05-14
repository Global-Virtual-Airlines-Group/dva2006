<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Staff IMAP Mailboxes</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="imaplist">
<tr class="title caps">
 <td colspan="5" class="left"><content:airline /> STAFF IMAP MAILBOXES</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td width="10%">PILOT ID</td>
 <td width="20%">PILOT NAME</td>
 <td width="15%">ADDRESS</td>
 <td width="5%">QUOTA</td>
 <td>ALIASES</td>
</tr>

<!-- Table Log Data -->
<c:forEach var="mb" items="${viewContext.results}">
<view:row entry="mb">
<c:set var="pilot" value="${pilots[mb.ID]}" scope="page" />
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><el:cmd url="profile" link="${pilot}" className="bld">${pilot.name}</el:cmd></td>
 <td><el:cmd url="imap" link="${mb}" className="plain">${mb.address}</el:cmd></td>
 <td class="small"><fmt:int value="${mb.quota / 1024}" />K</td>
<c:if test="${!empty mb.aliases}">
 <td class="left small">${fn:splice(mb.aliases, ', ')}</td>
</c:if>
<c:if test="${empty mb.aliases}"><td class="left">-</td></c:if>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="5"><view:legend width="100" labels="Active,Inactive" classes=" ,warn" /><br />
<view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
