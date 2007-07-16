<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="forumName" name="airline.forum" />
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ${forumName} Thread</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
<c:if test="${!isDelete}">
<meta http-equiv="refresh" content="3;url=/thread.do?id=0x<fmt:hex value="${thread.ID}" />" />
</c:if>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/cooler/header.jspf" %> 
<%@include file="/jsp/cooler/sideMenu.jspf" %>
<content:sysdata var="maxX" name="cooler.img_max.x" />
<content:sysdata var="maxY" name="cooler.img_max.y" />
<content:sysdata var="maxSize" name="cooler.img_max.size" />

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${isLocked || isHidden}">
<!-- Thread Locked/Hidden Message -->
<div class="updateHdr">${forumName} Message Thread Locked/Hidden</div>
<br />
The ${forumName} discussion thread <span class="pri bld">&quot;${thread.subject}&quot;</span> has been locked 
<c:if test="${isHidden}">and hidden. This discussion thread will only appear visible to users with the 
<span class="sec bld">&quot;Moderator&quot;</span> security role</c:if>.<br />
<br />
This discussion thread may be unlocked and/or unhidden at a later date.<br />
</c:if>
<c:if test="${usUnlocked || isUnhidden}">
<!-- Thread Unlocked/Hidden Message -->
<div class="updateHdr">${forumName} Message Thread Exposed</div>
<br />
The ${forumName} discussion thread <span class="pri bld">&quot;${thread.subject}&quot;</span> has been made visible to all 
users authorized to view posts within the <span class="sec bld">&quot;${thread.channel}&quot;</span> channel.<br />
<c:if test="${isUnlocked}">
<div class="updateHdr">${forumName} Message Thread Unlocked</div>
<br />
This ${forumName} discussion thread has also been unlocked. All users authorized to create new posts or replies within the 
<span class="sec bld">&quot;${thread.channel}&quot;</span> channel may create new responses within this discussion thread.<br />
</c:if>
</c:if>
<c:if test="${isPosted}">
<!-- New Thread Message -->
<div class="updateHdr">${forumName} Message Thread Created</div>
<br />
This new ${forumName} discussion thread has been posted in the <span class="sec bld">&quot;${thread.channel}&quot;</span>
channel. All users authorized to read and create posts in this channel may participate.<br />
<c:if test="${isNotify}">
<br />
You have signed up for response notifications. Each time a new response is posted in this ${forumName} discussion thread, you will 
receive an e-mail notification. You can turn notifications off at any time by returning to the discussion thread.<br />
</c:if>
<c:if test="${hasImage}">
<br />
Your attached image has been saved in the Image Gallery as a <i>${forumName} Screen Shot</i>.
<c:if test="${imgResized}">Your attached image was too large to fit in the ${forumName}. <span class="sec bld">The maximum 
${forumName} image size is <fmt:int value="${maxX}" />x<fmt:int value="${maxY}" /> pixels, or <fmt:int value="${maxSize / 1024}" />K.</span> 
Your image has been scaled to match the ${forumName} limits, and some loss of image quality may occur.<br /></c:if>
</c:if>
</c:if>
<c:if test="${isReply || isVote}">
<!-- New Response Message -->
<div class="updateHdr">${forumName} Message ${isReply ? 'Post' : 'Vote'} Created</div>
<br />
Your response has been posted to the discussion thread <span class="pri bld">&quot;${thread.subject}&quot;</span>. Thank you for 
your participation in the <content:airline /> ${forumName}!<br />
<c:if test="${!empty notifyMsgs}">
<br />
<fmt:int value="${notifyMsgs}" /> individuals have received e-mail notifications of this new ${forumName} discussion thread response.<br />
</c:if>
</c:if>
<c:if test="${isEdit}">
<div class="updateHdr">${forumName} Post Updated</div>
<br />
Your response in the discussion thread <span class="pri bld">&quot;${thread.subject}&quot;</span> has been updated.Thank you for 
your participation in the <content:airline /> ${forumName}!<br />
</c:if>
<c:if test="${isUnstuck}">
<!-- Thread Unstuck -->
<div class="updateHdr">${forumName} Message Thread Unstuck</div>
<br />
The discussion thread <span class="pri bld">&quot;${thread.subject}&quot;</span> has been unstuck.<br />
</c:if>
<c:if test="${isDelete}">
<!-- Thread Deleted -->
<div class="updateHdr">${forumName} Message Thread Deleted</div>
<br />
The discussion thread <span class="pri bld">&quot;${thread.subject}&quot;</span> has been deleted.<br />
</c:if>
<c:if test="${isMoved}">
<!-- Thread Moved -->
<div class="updateHdr">${forumName} Message Thread Moved</div>
<br />
The discussion thread <span class="pri bld">&quot;${thread.subject}&quot;</span> has been moved to the channel 
<span class="sec bld">${newChannel}</span>.<br />
</c:if>
<br />
<c:if test="${!isDelete}">
The discussion thread will automatically be displayed within 3 seconds. If your browser does not return to the thread or you are 
impatient, you can <el:cmd className="sec bld" url="thread" link="${thread}" op="read">click here</el:cmd> to display the 
discussion thread.<br />
<br />
</c:if>
To return to the ${forumName} Channel containing this thread, <el:cmd url="channel" linkID="${thread.channel}" className="sec bld">Click Here</el:cmd>.<br />
To view all ${forumName} discussion threads, <el:cmd url="channel" linkID="ALL" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
