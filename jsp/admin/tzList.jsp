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
<title><content:airline /> Time Zone Profiles</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="tzones">
<!-- Table Header Bar-->
<tr class="title caps">
 <td class="nophone">TIME ZONE NAME</td>
 <td class="nophone">CODE</td>
 <td style="width:35%">NAME</td>
 <td><el:cmdbutton url="tz" op="edit" label="NEW TIME ZONE" /></td>
</tr>

<!-- Table Data -->
<c:forEach var="tz" items="${viewContext.results}">
<c:set var="gmtOffset" value="${tz.zone.rules.getOffset(now).totalSeconds}" scope="page" />
<view:row entry="${tz}">
 <td class="nophone bld"><el:cmd url="tz" op="edit" linkID="${tz.ID}">${tz.ID}</el:cmd></td>
 <td class="nophone pri bld">${tz.abbr}</td>
 <td><fmt:text value="${tz.name}" /></td>
 <td>GMT ${gmtOffset < 0 ? '' : '+'}<fmt:dec value="${gmtOffset / 3600.0}" fmt="#0.##" /> hours</td>
</view:row>
</c:forEach>

<!-- Scroll bar -->
<tr class="title">
 <td colspan="4"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
 <view:legend width="200" labels="Observes Daylight Savings,Standard Time" classes="opt1, " /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
