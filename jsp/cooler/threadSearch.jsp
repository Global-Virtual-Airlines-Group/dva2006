<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="forumName" name="airline.forum" />
<html lang="en">
<head>
<title><content:airline /> ${forumName} Search</title>
<content:sysdata var="airlineName" name="airline.name" />
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:rss title="${airlineName} ${forumName}" path="/cooler_rss.ws" />
<script>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
if ((f.searchStr.value.length < 3) && (f.pilotName.value.length < 3)) {
	golgotha.form.validate({f:f.searchStr, l:3, t:'Search Term'});
	golgotha.form.validate({f:f.pilotName, l:3, t:'Pilot Name'});
}

golgotha.form.submit(f);
return true;
};

golgotha.local.setChannel = function(combo) {
	self.location = '/channel.do?id=' + escape(golgotha.form.getValue(combo));
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/cooler/header.jspf" %> 
<%@ include file="/jsp/cooler/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="coolersearch.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title">
 <td colspan="2" class="left caps"><content:airline /> ${forumName} Search</td>
</tr>
<tr>
 <td class="label">Search String</td>
 <td class="data"><el:text name="searchStr" idx="*" size="20" className="pri bld" max="34" value="${param.searchStr}" /></td>
</tr>
<tr>
 <td class="label">Cooler Channel</td>
 <td class="data"><el:combo name="channel" idx="*" size="1" options="${channels}" value="${param.channel}" /></td>
</tr>
<tr>
 <td class="label">Pilot Name</td>
 <td class="data"><el:text name="pilotName" idx="*" size="20" max="36" value="${param.pilotName}" /></td>
</tr>
<tr>
 <td class="label">Last Post</td>
 <td class="data">Within the previous <el:combo name="daysBack" idx="*" size="1" value="${daysBack}" options="${days}" /> days</td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="nameMatch" idx="*" value="true" label="Partial Pilot Name match" checked="${param.nameMatch}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SearchButton" type="submit" label="SEARCH ${forumName}" /></td>
</tr>
</el:table>

<c:if test="${doSearch}">
<!-- Search Results -->
<c:if test="${!empty viewContext.results}">
<view:table cmd="coolersearch">
<!-- Table Sort Combo Bar -->
<tr class="title">
 <td colspan="1" class="left caps">${forumName} SEARCH RESULTS</td>
 <td colspan="3" class="right">CHANNEL <el:combo name="sortType" size="1" options="${channels}" value="${channel}" onChange="void golgotha.local.setChannel(this)" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td style="width:50%">THREAD TITLE</td>
 <td style="width:20%">STARTED BY</td>
 <td style="width:5%">POSTS</td>
 <td>LAST POST</td>
</tr>

<!-- Table Thread Data -->
<c:forEach var="thread" items="${viewContext.results}">
<c:set var="author" value="${pilots[thread.authorID]}" scope="page" />
<c:set var="authorLoc" value="${userData[thread.authorID]}" scope="page" />
<c:set var="lastPoster" value="${pilots[thread.lastUpdateID]}" scope="page" />
<view:row entry="${thread}">
 <td class="left">
<c:if test="${thread.image != 0}"><el:img caption="Image" x="20" y="20" src="cooler/icon_img.png" /></c:if>
<c:if test="${thread.locked}"><el:img caption="Thread Locked" x="20" y="20" src="cooler/icon_lock.png" /></c:if>
<c:if test="${thread.poll}"><el:img caption="Pilot Poll" x="20" y="20" src="cooler/icon_poll.png" /></c:if>
<c:if test="${!empty thread.stickyUntil}">STICKY:</c:if>
 <el:cmd url="thread" link="${thread}"><fmt:text value="${thread.subject}" /></el:cmd>
 <span class="small">(in <span class="pri">${thread.channel}</span>)</span></td>
 <td><el:profile location="${authorLoc}" className="pri bld">${author.name}</el:profile></td>
 <td><fmt:int value="${thread.postCount}" /></td>
 <td class="small right"><fmt:date date="${thread.lastUpdatedOn}" /> by 
 <span class="pri bld">${lastPoster.name}</span></td>
</view:row>
</c:forEach>

<tr class="title">
 <td colspan="4" class="small">Search Completed in <fmt:int value="${searchTime}" />ms</td>
</tr>
</view:table>
</c:if>
<c:if test="${empty viewContext.results}">
<el:table className="view">
<tr class="pri bld caps">
 <td>No <content:airline /> ${forumName} Discussion Threads matching your criteria were found.</td>
</tr>
</el:table>
</c:if>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
