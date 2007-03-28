<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>New <content:airline /> Water Cooler Thread</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="datePicker" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if ((!form) || (!checkSubmit())) return false;
document.linkImage |= ((form.imgURL) && (form.imgURL.value.length > 1));

if (!document.linkImage) {
	if (!validateText(form.subject, 8, 'Title of your Thread')) return false;
	if (!validateText(form.msgText, 5, 'text of your Message')) return false;
	if (!validateFile(form.img, 'gif,jpg,png', 'Attached Image')) return false;
} else {
	if (!validateText(form.imgURL, 12, 'URL of your Linked Image')) return false;
	if (!validateText(form.desc, 8, 'Description of your Linked Image')) return false;
	form.addImage.value = 'true';
}

// Check for multiple image posting methods
if (document.linkImage && (form.img) && (form.img.value.length > 0)) {
	alert('You cannot Link an Image and Upload an Image at the same time.');
	form.img.focus();
	return false;
}

setSubmit();
disableButton('EmoticonButton');
disableButton('LinkButton');
disableButton('SaveButton');
return true;
}

function openEmoticons()
{
var flags = 'height=280,width=250,menubar=no,toolbar=no,status=no,scrollbars=yes';
var w = window.open('emoticons.do', 'emoticonHelp', flags);
return true;
}

function enablePoll()
{
var f = document.forms[0];
if (!f.hasPoll) return false;
f.pollOptions.disabled = !f.hasPoll.checked;
return true;
}

function submitImage()
{
var f = document.forms[0];
if (validate(f)) {
	document.linkImage = true;
	f.submit();
}

return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/cooler/header.jspf" %> 
<%@ include file="/jsp/cooler/sideMenu.jspf" %>
<content:sysdata var="maxX" name="cooler.img_max.x" />
<content:sysdata var="maxY" name="cooler.img_max.y" />
<content:sysdata var="maxSize" name="cooler.img_max.size" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="threadpost.do" method="post" allowUpload="true" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title">
 <td colspan="2" class="left caps">New Water Cooler Discusion Thread</td>
</tr>
<tr>
 <td class="label">Thread Title</td>
 <td class="data"><el:text name="subject" className="pri bld req" idx="*" size="64" max="80" value="${param.subject}" /></td>
</tr>
<tr>
 <td class="label">Water Cooler Channel</td>
 <td class="data"><el:combo name="id" idx="*" size="1" options="${channels}" value="${empty param.id ? 'General Aviation Discussion' : param.id}" /></td>
</tr>
<content:filter roles="Moderator,PIREP,HR,Examination">
<tr>
 <td class="label">Sticky Until</td>
 <td class="data"><el:text name="stickyDate" idx="*" size="10" max="11" value="${param.stickyDate}" />
<c:if test="${user.dateFormat == 'MM/dd/yyyy'}">
 <el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].stickyDate')" /></c:if></td>
</tr>
</content:filter>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="updateNotify" idx="*" label="Send e-mail when responses are posted" value="true" />&nbsp;
<el:button ID="EmoticonButton" className="BUTTON" onClick="void openEmoticons()" label="EMOTICONS" /></td>
</tr>
<c:if test="${empty sessionScope.imageURLs}">
<tr>
 <td class="label">Upload Image</td>
 <td class="data"><el:file name="img" className="small" idx="*" size="64" max="144" onChange="void toggleImgOptions(this)" />
<c:if test="${imgBadSize}"><div class="error bld">Your attached image was too large (<fmt:int value="${imgSize}" /> bytes).</div></c:if>
<c:if test="${imgBadDim}"><div class="error bld">Your attached image was too large (<fmt:int value="${imgX}" />
 by <fmt:int value="${imgY}" /> pixels).</div></c:if>
<c:if test="${imgInvalid}"><div class="error bld">Your attached image is in an unknown format.</div></c:if></td>
</tr>
</c:if>
<tr class="title caps">
 <td colspan="2">LINKED IMAGES</td>
</tr>
<tr>
 <td class="label" valign="top">New Image URL</td>
 <td class="data"><el:text name="imgURL" className="small" idx="*" size="64" max="192" value="" />
<c:if test="${!empty system_message}"><div class="small error bld">${system_message}</div></c:if></td>
</tr>
<tr>
 <td class="label">Description</td>
 <td class="data"><el:text name="desc" idx="*" size="64" max="192" value="" /> <el:button ID="LinkButton" className="BUTTON" label="LINK IMAGE" onClick="void submitImage()" /></td>
</tr>
<c:if test="${!empty sessionScope.imageURLs}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><c:forEach var="imgLink" items="${sessionScope.imageURLs}">
<el:link target="_new" url="${imgLink.URL}">${imgLink.URL}</el:link> - ${imgLink.description}<br />
</c:forEach></td>
</tr>
</c:if>
<content:filter roles="PIREP,HR,Moderator">
<!-- Pilot Poll -->
<tr class="title caps">
 <td colspan="2">PILOT POLL</td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="hasPoll" idx="*" value="true" onChange="void enablePoll()" label="Enable Pilot Poll in this Discussion Thread" /></td>
</tr>
<tr>
 <td class="label" valign="top">Poll Options</td>
 <td class="data"><el:textbox name="pollOptions" idx="*" width="60" height="6"></el:textbox></td>
</tr>
</content:filter>

<!-- Message Text -->
<tr class="title caps">
 <td colspan="2">NEW MESSAGE TEXT</td>
</tr>
<tr>
 <td class="mid" colspan="2"><el:textbox name="msgText" idx="*" width="90%" className="req" height="8">${param.msgText}</el:textbox></td>
</tr>

<!-- Button Bar -->
<tr class="title mid">
 <td colspan="2">
<c:if test="${channelAccess.canPost}">
 <el:button ID="SaveButton" className="BUTTON" label="SAVE NEW THREAD" type="submit" />
</c:if>
 </td>
</tr>
</el:table>
<el:text name="addImage" type="hidden" value="" />
</el:form>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
