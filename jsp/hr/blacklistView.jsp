<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Login / Registration Blacklist</title>
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="blacklist">
<tr class="title">
 <td class="caps left" colspan="5"><content:airline /> LOGIN / REGISTRATION BLACKLIST</td>
</tr>

<!-- Table Header Bar -->
<tr class="title">
 <td style="width:20%">ADDRESS BLOCK</td>
 <td class="nophone"  style="width:5%">&nbsp;</td>
 <td style="width:35%">LOCATION</td>
 <td style="width:10%">CREATED</td>
 <td class="right">COMMENTS</td>
</tr>

<!-- Table Blacklist data -->
<c:forEach var="entry" items="${viewContext.results}">
<c:set var="ipInfo" value="${locations[entry.CIDR.networkAddress]}" scope="page" />
<view:row entry="${entry}">
 <td class="pri bld">${entry.CIDR}</td>
 <td class="nophone"><el:cmdbutton url="blacklistdelete" linkID="${entry.CIDR.networkAddress}" label="DELETE" /></td>
<c:if test="${!empty ipInfo}">
  <td class="small"><el:flag countryCode="${ipInfo.country.code}" caption="${ipInfo.location}" />&nbsp;${ipInfo.location}</td>
 </c:if>
<c:if test="${empty ipInfo}">
 <td>N/A</td>
</c:if>
 <td class="sec"><fmt:date date="${entry.created}" fmt="d" /></td>
 <td class="small right"><fmt:text value="${entry.text}" default="-" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="5"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
