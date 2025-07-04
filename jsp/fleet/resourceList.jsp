<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Web Resources</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script async>
golgotha.local.sort = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="hrEmail" name="airline.mail.hr" />
<content:sysdata var="cats" name="airline.resources.categories" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="resources.do" method="post" validate="return false">
<view:table cmd="resources">
<tr class="title">
 <td class="left caps" colspan="2"><content:airline /> WEB RESOURCES</td>
 <td class="right" colspan="4"><c:if test="${access.canCreate}"><el:cmdbutton url="resource" op="edit" label="NEW RESOURCE" /></c:if>
 CATEGORY <el:combo name="cat" idx="*" size="1" options="${cats}" firstEntry="ALL" value="${param.cat}" onChange="void golgotha.local.sort()" />
 SORT BY <el:combo name="sortType" idx="*" size="1" options="${sortOptions}" value="${viewContext.sortType}" onChange="void golgotha.local.sort()" /></td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:40%">RESOURCE URL</td>
 <td style="width:8%">HITS</td>
 <td style="width:15%">AUTHOR</td>
 <td style="width:9%">CREATED ON</td>
 <td style="width:20%">CATEGORY</td>
 <td>&nbsp;</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="resource" items="${viewContext.results}">
<c:set var="rAccess" value="${accessMap[resource.ID]}" scope="page" />
<c:set var="author" value="${pilots[resource.authorID]}" scope="page" />
<view:row entry="${resource}">
<c:set var="title" value="${(empty resource.title) ? resource.URL : resource.title}" scope="page" />
 <td class="left"><el:cmd url="gotoresource" link="${resource}" className="pri bld">${title}</el:cmd></td>
 <td class="sec bld"><fmt:int value="${resource.hits}" /></td>
 <td><el:cmd url="profile" link="${author}" className="bld">${author.name}</el:cmd></td>
 <td class="sec"><fmt:date date="${resource.createdOn}" fmt="d" /></td>
<c:if test="${rAccess.canEdit}">
 <td class="pri">${resource.category}</td>
 <td><el:cmdbutton url="resource" link="${resource}" op="edit" label="EDIT" /></td>
</c:if>
<c:if test="${!rAccess.canEdit}">
 <td colspan="2" class="pri">${resource.category}</td>
</c:if>
</view:row>
<view:row entry="${resource}">
 <td colspan="6" class="left small"><fmt:text value="${resource.description}" /></td>
</view:row>
</c:forEach>

<!-- Disclaimer bar -->
<tr>
 <td colspan="6" class="small">All Web Resources listed above are links to external Web Sites. No guarantees are made
 by <content:airline /> regarding the fitness or appropriateness of content contained within these external
 sites. To report inappropriate content, please contact <el:link url="mailto:${hrEmail}">${hrEmail}</el:link>.</td>
</tr>

<!-- Scroll Bar row -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>
<view:legend width="90" labels="Public,Private" classes=" ,opt2" /></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
