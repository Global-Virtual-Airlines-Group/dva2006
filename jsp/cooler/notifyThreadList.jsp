<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="forumName" name="airline.forum" />
<html lang="en">
<head>
<title><content:airline /> ${forumName} Watched Discussion Threads</title>
<meta http-equiv="REFRESH" content="300" />
<content:sysdata var="airlineName" name="airline.name" />
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.setChannel = function(combo) {
	self.location = '/channel.do?id=' + escape(golgotha.form.getCombo(combo));
	return true;
};

golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.threadID, min:1, t:'Message Thread'});
golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/cooler/header.jspf" %> 
<%@ include file="/jsp/cooler/sideMenu.jspf" %>
<c:set var="channelName" value="Watched ${forumName} Discussion Threads" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="notifyupdate.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<view:table cmd="${viewCmdName}">
<!-- Table Sort Combo Bar -->
<tr class="title">
<c:if test="${channelAccess.canPost}">
 <td colspan="2" class="left caps">DISCUSSION THREADS - ${channelName}</td>
 <td><el:cmdbutton url="threadpost" linkID="${channel.name}" label="NEW THREAD" /></td>
</c:if>
<c:if test="${!channelAccess.canPost}">
 <td colspan="3" class="left caps">DISCUSSION THREADS - ${channelName}</td>
</c:if>
 <td colspan="3" class="right">CHANNEL <el:combo name="sortType" size="1" options="${channels}" value="${channel}" onChange="void golgotha.local.setChannel(this)" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td style="max-width:5%">&nbsp;</td>
 <td style="width:31%">THREAD TITLE</td>
 <td class="nophone" style="width:15%">STARTED BY</td>
 <td class="nophone" style="max-width:8%">VIEWS</td>
 <td class="nphone" style="max-width:8%">POSTS</td>
 <td>LAST POST</td>
</tr>

<!-- Table Thread Data -->
<c:forEach var="thread" items="${viewContext.results}">
<c:set var="author" value="${pilots[thread.authorID]}" scope="page" />
<c:set var="authorLoc" value="${userData[thread.authorID]}" scope="page" />
<c:set var="lastPoster" value="${pilots[thread.lastUpdateID]}" scope="page" />
<c:set var="myLastRead" value="${threadViews[thread.ID]}" scope="page" />
<c:set var="cutoffDate" value="${!empty sessionScope.coolerThreadReadOverride ? sessionScope.coolerThreadReadOverride : user.lastLogoff}" scope="page" />
<c:set var="isThreadNew" value="${(thread.lastUpdatedOn > cutoffDate) && ((empty myLastRead) || (myLastRead < thread.lastUpdatedOn))}" scope="page" />
<view:row entry="${thread}" className="${isThreadNew ? 'opt1' : null}">
 <td><el:box name="threadID" idx="*" value="${thread.hexID}" label="" checked="false" /></td>
 <td class="left">
<c:if test="${thread.image != 0}"><el:img caption="Image" x="20" y="20" src="cooler/icon_img.png" /></c:if>
<c:if test="${thread.locked}"><el:img caption="Thread Locked" x="20" y="20" src="cooler/icon_lock.png" /></c:if>
<c:if test="${thread.poll}"><el:img caption="Pilot Poll" x="20" y="20" src="cooler/icon_poll.png" /></c:if>
<c:if test="${!empty thread.stickyUntil}">STICKY:</c:if>
 <el:cmd url="thread" link="${thread}"><fmt:text value="${thread.subject}" /></el:cmd></td>
 <td class="nophone"><el:profile location="${authorLoc}" className="pri bld">${author.name}</el:profile></td>
 <td class="nophone"><fmt:int value="${thread.views}" /></td>
 <td class="nophone"><fmt:int value="${thread.postCount}" /></td>
 <td class="small right"><fmt:date date="${thread.lastUpdatedOn}" /> by <span class="pri bld">${lastPoster.name}</span></td>
</view:row>
</c:forEach>
<tr class="title">
 <td colspan="6"><el:button type="submit" ID="UpdateButton" label="CLEAR NOTIFICATIONS" />
 <view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar></td>
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
