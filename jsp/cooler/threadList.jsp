<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Water Cooler</title>
<content:sysdata var="airlineName" name="airline.name" />
<c:set var="serverName" value="${pageContext.request.serverName}" scope="request" />
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:rss title="${airlineName} Water Cooler" url="http://${serverName}/cooler_rss.ws" />
<script language="JavaScript" type="text/javascript">
function setChannel(combo)
{
var channel = combo.options[combo.selectedIndex].value;
self.location = '/channel.do?id=' + escape(channel);
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/cooler/header.jsp" %> 
<%@include file="/jsp/cooler/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="channel.do" method="GET" validate="return false">
<view:table className="view" pad="default" space="default" cmd="channel">
<!-- Table Sort Combo Bar -->
<tr class="title">
<c:if test="${channelAccess.canPost}">
 <td class="left caps">DISCUSSION THREADS - ${channel.name}</td>
 <td><el:cmdbutton url="threadpost" linkID="${channel.name}" label="NEW THREAD" /></td>
</c:if>
<c:if test="${!channelAccess.canPost}">
 <td colspan="2" class="left caps">DISCUSSION THREADS - ${channel.name}</td>
</c:if>
 <td colspan="3" class="right">CHANNEL <el:combo name="sortType" size="1" firstEntry="ALL" options="${channels}" value="${channel}" onChange="void setChannel(this)" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td width="35%">THREAD TITLE</td>
 <td width="15%">STARTED BY</td>
 <td width="8%">VIEWS</td>
 <td width="8%">POSTS</td>
 <td>LAST POST</td>
</tr>

<!-- Table Thread Data -->
<c:forEach var="thread" items="${viewContext.results}">
<c:set var="author" value="${pilots[thread.authorID]}" scope="request" />
<c:set var="authorLoc" value="${userData[thread.authorID]}" scope="request" />
<c:set var="lastPoster" value="${pilots[thread.lastUpdateID]}" scope="request" />
<view:row entry="${thread}">
 <td class="left">
<c:if test="${thread.image != 0}"><el:img caption="Image" x="20" y="20" src="cooler/icon_img.png" /></c:if>
<c:if test="${thread.locked}"><el:img caption="Thread Locked" x="20" y="20" src="cooler/icon_lock.png" />&nbsp;</c:if>
<c:if test="${!empty thread.stickyUntil}">STICKY:</c:if>
 <el:cmd url="thread" linkID="0x${thread.ID}"><fmt:text value="${thread.subject}" /></el:cmd></td>
 <td><el:profile location="${authorLoc}" className="pri bld">${author.name}</el:profile></td>
 <td><fmt:int value="${thread.views}" /></td>
 <td><fmt:int value="${thread.postCount}" /></td>
 <td class="small right"><fmt:date date="${thread.lastUpdatedOn}" /> by 
 <span class="pri bld">${lastPoster.name}</span></td>
</view:row>
</c:forEach>
<tr class="title">
 <td colspan="5"><view:pgUp />&nbsp;<view:pgDn /></td>
</tr>
</view:table>
</el:form>
<content:copyright />
</div>
</body>
</html>
