<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="forumName" name="airline.forum" />
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ${forumName} Search</title>
<content:sysdata var="airlineName" name="airline.name" />
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:rss title="${airlineName} ${forumName}" path="/cooler_rss.ws" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if ((form.searchStr.value.length < 3) && (form.pilotName.value.length < 3)) {
	if (!validateText(form.searchStr, 3, 'Search Term')) return false;
	if (!validateText(form.pilotName, 3, 'Pilot Name')) return false;
}

setSubmit();
disableButton('SearchButton');
return true;
}

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
<content:page>
<%@ include file="/jsp/cooler/header.jspf" %> 
<%@ include file="/jsp/cooler/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="coolersearch.do" method="post" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title">
 <td colspan="2" class="left caps">${forumName} Search</td>
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
 <td class="data">Within the previous <el:combo name="daysBack" idx="*" size="1" value="${param.daysBack}" options="${days}" /> days</td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="checkSubject" idx="*" value="true" label="Check Subjects as well as Message Body" checked="${param.checkSubject}" /><br />
<el:box name="nameMatch" idx="*" value="true" label="Partial Pilot Name match" checked="${param.nameMatch}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH ${forumName}" /></td>
</tr>
</el:table>

<c:if test="${doSearch}">
<!-- Search Results -->
<c:if test="${!empty viewContext.results}">
<view:table className="view" pad="default" space="default" cmd="coolersearch">
<!-- Table Sort Combo Bar -->
<tr class="title">
 <td colspan="1" class="left caps">${forumName} SEARCH RESULTS</td>
 <td colspan="3" class="right">CHANNEL <el:combo name="sortType" size="1" options="${channels}" value="${channel}" onChange="void setChannel(this)" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td width="50%">THREAD TITLE</td>
 <td width="20%">STARTED BY</td>
 <td width="5%">POSTS</td>
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
<el:table className="view" space="default" pad="default">
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
