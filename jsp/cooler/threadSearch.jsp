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
<title><content:airline /> Water Cooler Search</title>
<content:sysdata var="airlineName" name="airline.name" />
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:rss title="${airlineName} Water Cooler" path="/cooler_rss.ws" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.searchStr, 3, 'Search Term')) return false;

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
<body onload="enableElement('SearchButton', true); clearSubmit()">
<content:page>
<%@ include file="/jsp/cooler/header.jspf" %> 
<%@ include file="/jsp/cooler/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="coolersearch.do" method="post" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title">
 <td colspan="2" class="left caps">Water Cooler Search</td>
</tr>
<tr>
 <td class="label">Search String</td>
 <td class="data"><el:text name="searchStr" idx="*" size="20" className="pri bld req" max="34" value="${param.searchStr}" /></td>
</tr>
<tr>
 <td class="label">Cooler Channel</td>
 <td class="data"><el:combo name="channel" idx="*" size="1" options="${channels}" value="${param.channel}" /></td>
</tr>
<tr>
 <td class="label">Pilot Name</td>
 <td class="data"><el:text name="pilotName" idx="*" size="16" max="32" value="${param.pilotName}" /></td>
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
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH WATER COOLER" /></td>
</tr>
</el:table>

<c:if test="${doSearch}">
<!-- Search Results -->
<c:if test="${!empty viewContext.results}">
<view:table className="view" pad="default" space="default" cmd="coolersearch">
<!-- Table Sort Combo Bar -->
<tr class="title">
 <td colspan="2" class="left caps">WATER COOLER SEARCH RESULTS</td>
 <td colspan="3" class="right">CHANNEL <el:combo name="sortType" size="1" options="${channels}" value="${channel}" onChange="void setChannel(this)" /></td>
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
<c:if test="${thread.locked}"><el:img caption="Thread Locked" x="20" y="20" src="cooler/icon_lock.png" /></c:if>
<c:if test="${thread.poll}"><el:img caption="Pilot Poll" x="20" y="20" src="cooler/icon_poll.png" /></c:if>
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
 <td colspan="5">&nbsp;</td>
</tr>
</view:table>
</c:if>
<c:if test="${empty viewContext.results}">
<el:table className="view" space="default" pad="default">
<tr class="pri bld caps">
 <td>No <content:airline /> Water Cooler Discussion Threads matching your criteria were found.</td>
</tr>
</el:table>
</c:if>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
