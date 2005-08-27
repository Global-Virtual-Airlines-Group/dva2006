<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Water Cooler Thread</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
<meta http-equiv="refresh" content="3;url=/thread.do?id=0x<fmt:hex value="${thread.ID}" />" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/cooler/header.jsp" %> 
<%@include file="/jsp/cooler/sideMenu.jsp" %>
<content:sysdata var="maxX" name="cooler.img_max.x" />
<content:sysdata var="maxY" name="cooler.img_max.y" />
<content:sysdata var="maxSize" name="cooler.img_max.size" />

<!-- Main Body Frame -->
<div id="main">
<c:if test="${isLocked || isHidden}">
<!-- Thread Locked/Hidden Message -->
<div class="updateHdr">Water Cooler Message Thread Locked/Hidden</div>
<br />
The Water Cooler discussion thread <span class="pri bld">&quot;${thread.subject}&quot;</span> has been 
locked <c:if test="${isHidden}">and hidden. This discussion thread will only appear visible to users 
with the <span class="sec bld">&quot;Moderator&quot;</span> security role</c:if>.<br />
<br />
This discussion thread may be unlocked and/or unhidden at a later date.<br />
</c:if>
<c:if test="${usUnlocked || isUnhidden}">
<!-- Thread Unlocked/Hidden Message -->
<div class="updateHdr">Water Cooler Message Thread Exposed</div>
<br />
The Water Cooler discussion thread <span class="pri bld">&quot;${thread.subject}&quot;</span> has been 
made visible to all users authorized to view posts within the <span class="sec bld">&quot;${thread.channel}&quot;</span> 
channel.<br />
<c:if test="${isUnlocked}">
<div class="updateHdr">Water Cooler Message Thread Unlocked</div>
<br />
This discussion thread has also been unlocked. All users authorized to create new posts or replies within
the <span class="sec bld">&quot;${thread.channel}&quot;</span> channel may create new responses within 
this discussion thread.<br />
</c:if>
</c:if>
<c:if test="${isPosted}">
<!-- New Thread Message -->
<div class="updateHdr">Water Cooler Message Thread Created</div>
<br />
This new Water Cooler discussion thread has been posted in the <span class="sec bld">&quot;${thread.channel}&quot;</span>
channel. All users authorized to read and create posts in this channel may participate.<br />
<c:if test="${hasImage}">
<br />
Your attached image has been saved in the Image Gallery as a <i>Water Cooler Screen Shot</i>.
<c:if test="${imgResized}">Your attached image was too large to fit in the Water Cooler. <span class="sec bld">The
maximum Water Cooler image size is <fmt:int value="${maxX}" />x<fmt:int value="${maxY}" /> pixels, or
<fmt:int value="${maxSize / 1024}" />K.</span> Your image has been scaled to match the Water Cooler limits,
and some loss of image quality may occur.<br /></c:if>
</c:if>
</c:if>
<c:if test="${!empty msg && !isPosted}">
<!-- New Response Message -->
<div class="updateHdr">Water Cooler Message Post Created</div>
<br />
Your response has been posted to the discussion thread <span class="pri bld">&quot;${thread.subject}&quot;</span>.
Thank you for your participation in the <content:airline /> Water Cooler!<br />
</c:if>
<br />
The discussion thread will automatically be displayed within 3 seconds. If your browser does not return
to the thread or you are impatient, you can <el:cmd className="sec bld" url="thread" linkID="0x${thread.ID}" op="read">click here</el:cmd>
to display the discussion thread.<br />
<br />
<content:copyright />
</div>
</body>
</html>
