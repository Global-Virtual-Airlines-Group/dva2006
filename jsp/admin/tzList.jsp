<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Time Zone Profiles</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="tzones">
<!-- Table Header Bar-->
<tr class="title caps">
 <td width="30%">TIME ZONE NAME</td>
 <td width="10%">CODE</td>
 <td width="35%">NAME</td>
 <td><el:cmdbutton url="tz" op="edit" label="NEW TIME ZONE" /></td>
</tr>

<!-- Table Data -->
<c:forEach var="tz" items="${viewContext.results}">
<c:set var="gmtOffset" value="${tz.timeZone.rawOffset / 1000}" scope="request" />
<view:row entry="${tz}">
 <td class="bld"><el:cmd url="tz" op="edit" linkID="${tz.ID}">${tz.ID}</el:cmd></td>
 <td class="pri bld">${tz.abbr}</td>
 <td><fmt:text value="${tz.name}" /></td>
 <td>GMT ${gmtOffset < 0 ? '' : '+'}<fmt:dec value="${gmtOffset / 3600}" fmt="#0.#" /> hours</td>
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
