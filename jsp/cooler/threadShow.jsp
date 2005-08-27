<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Water Cooler - ${thread.subject}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="cooler" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.msgText, 3, 'text of your response')) return false;

setSubmit();
disableButton('SaveButton');
disableButton('LockButton');
disableButton('HideButton');
disableButton('UnlockButton');
disableButton('VoteButton');
disableButton('ImgDeleteButton');
disableButton('ResyncButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/cooler/header.jsp" %> 
<%@include file="/jsp/cooler/sideMenu.jsp" %>
<c:set var="serverName" value="${pageContext.request.serverName}" scope="request" />
<content:sysdata var="imgPath" name="path.img" />
<content:sysdata var="ccLevels" name="centuryClubLevels" />

<!-- Main Body Frame -->
<div id="main">
<el:form action="threadReply.do" linkID="0x${thread.ID}" method="POST" validate="return validate(this)">
<el:table className="thread" pad="default" space="default">
<!-- Thread Header -->
<tr class="title">
 <td colspan="2" class="left"><el:cmd url="channels">DVA WATER COOLER</el:cmd> | 
 <el:cmd url="channel" linkID="${thread.channel}">${thread.channel}</el:cmd> | ${thread.subject}</td>
</tr>

<c:if test="${!empty img}">
<!-- Attached Image -->
<tr class="mid">
 <td colspan="2"><img width="${img.width}" height="${img.height}" alt="${thread.subject}" src="/gallery/0x<fmt:hex value="${thread.image}" />" /></td>
</tr>
</c:if>
<c:if test="${!empty img.votes}">
<tr>
 <td colspan="2" class="bld caps">Feedback: ${img.voteCount} ratings, Average <fmt:dec value="${img.score}" /></td>
</tr>
</c:if>

<!-- Thread Posts -->
<c:forEach var="msg" items="${thread.posts}">
<!-- Response bean 0x<fmt:hex value="${msg.ID}" /> -->
<c:set var="pilot" value="${pilots[msg.authorID]}" scope="request" />
<c:set var="pilotLoc" value="${userData[msg.authorID]}" scope="request" />
<tr>
 <td rowspan="2" class="postInfo small"><el:profile location="${pilotLoc}">${pilot.name}</el:profile><br />
<c:if test="${!empty pilot.pilotCode}"><span class="sec bld">${pilot.pilotCode}</span><br /></c:if>
 <span class="caps bld">${pilot.rank}</span>, ${pilot.equipmentType}<br />
 <br />
 Joined on <fmt:date d="MMMM dd yyyy" fmt="d" date="${pilot.createdOn}" /><br />
<c:choose>
<c:when test="${pilot.legs >= 1500}">
 <font color="#AF2020"><b>${ccLevels['CC1500']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 1000}">
 <font color="#AF2020"><b>${ccLevels['CC1000']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 800}">
 <font color="#6060BF"><b>${ccLevels['CC800']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 700}">
 <font color="#6060BF"><b>${ccLevels['CC700']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 600}">
 <font color="#6060BF"><b>${ccLevels['CC600']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 500}">
 <font color="#8080AF"><b>${ccLevels['CC500']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 400}">
 <font color="#408090"><b>${ccLevels['CC400']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 300}">
 <font color="#308060"><b>${ccLevels['CC300']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 200}">
 <font color="#805020"><b>${ccLevels['CC200']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 100}">
 <font color="#0000A1"><b>${ccLevels['CC100']}</b></font><br />
</c:when>
</c:choose>
 <br />
 <b><fmt:int fmt="#,##0" value="${pilot.legs}" /></b> legs, <b><fmt:dec fmt="#,##0.0" value="${pilot.hours}" /></b> hours total<br />
<c:if test="${pilot.onlineLegs > 0}">
 <span class="sec"><b><fmt:int fmt="#,##0" value="${pilot.onlineLegs}" /></b> legs, <b>
<fmt:dec fmt="#,##0.0" value="${pilot.onlineHours}" /></b> hours online</span><br />
</c:if>
<content:activeUser user="${msg.authorID}">
<span class="glow">CURRENTLY LOGGED IN</span><br />
</content:activeUser>
<c:if test="${!empty pilot.IMHandle}">
<span class="mid"><img border="0" src="http://big.oscar.aol.com/${pilot.IMHandle}?on_url=http://${serverName}/${imgPath}/im/aimonline.png&off_url=http://${serverName}/${imgPath}/im/aimoffline.png" /></span>
</c:if>
 </td>
 <td class="postDate">Post created on <fmt:date date="${msg.createdOn}" d="MMMM dd yyyy" />
<content:filter roles="Admin,Moderator">
 from ${msg.remoteAddr} (${msg.remoteHost})
</content:filter> 
</td>
</tr>
<tr>
 <td class="postBody"><fmt:text value="${msg.body}" />
<c:if test="${pilot.hasSignature}">
<br />
<!-- Signature Image for ${pilot.name} -->
<br />
<img src="/sig/${pilotLoc.DB}/0x<fmt:hex value="${pilot.ID}" />" alt="${pilot.name} (${pilot.pilotCode})" /><br />
</c:if> 
 </td>
</tr>
</c:forEach>

<c:if test="${access.canReply}">
<!-- Thread Response -->
<tr class="title caps">
 <td colspan="2">NEW RESPONSE</td>
</tr>
<tr class="mid">
 <td colspan="2" ><el:textbox name="msgText" width="125" height="8"></el:textbox></td>
</tr>
</c:if>

<!-- Button Bar -->
<tr class="buttons mid">
 <td colspan="2">&nbsp;
<c:if test="${imgAccess.canVote}">
<b>RATE IMAGE</b> <el:combo name="score" idx="*" size="1" options="${scores}" firstEntry="-" />&nbsp;
<el:cmdbutton ID="VoteButton" url="imgvote" linkID="0x${img.ID}" op="0x${fn:hex(thread.ID)}" post="true" label="SUBMIT FEEDBACK" />
</c:if>
<c:if test="${access.canReply}">
 <el:button className="BUTTON" ID="SaveButton" label="SAVE RESPONSE" type="submit" />
</c:if>
<c:if test="${access.canLock}">
 <el:cmdbutton ID="LockButton" label="LOCK THREAD" url="threadlock" linkID="0x${thread.ID}" op="lock" />
 <el:cmdbutton ID="HideButton" label="HIDE THREAD" url="threadlock" linkID="0x${thread.ID}" op="hide" />
</c:if>
<c:if test="${access.canUnlock}">
 <el:cmdbutton ID="UnlockButton" label="UNLOCK THREAD" url="threadunlock" linkID="0x${thread.ID}" op="unlock" />
 <el:cmdbutton ID="UnhideButton" label="UNHIDE THREAD" url="threadunlock" linkID="0x${thread.ID}" op="unhide" />
</c:if>
<c:if test="${imgAccess.canDelete}">
 <el:cmdbutton ID="ImgDeleteButton" label="DELETE IMAGE" url="imgdelete" linkID="0x${img.ID}" />
</c:if>
<c:if test="${access.canResync && !noResync}">
 <el:cmdbutton ID="ResyncButton" label="RESYNCHRONIZE THREAD DATA" url="threadsync" linkID="0x${img.ID}" />
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
