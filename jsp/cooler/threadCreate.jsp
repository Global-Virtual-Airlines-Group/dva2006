<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
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
if (!checkSubmit()) return false;
if (!validateText(form.subject, 10, 'Title of your Thread')) return false;
if (!validateText(form.msgText, 5, 'text of your Message')) return false;
if (!validateFile(form.img, 'gif,jpg,png', 'Attached Image')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
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
<el:form action="threadpost.do" method="POST" allowUpload="true" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title">
 <td colspan="2" class="left caps">New Water Cooler Discusion Thread</td>
</tr>
<tr>
 <td class="label">Thread Title</td>
 <td class="data"><el:text name="subject" className="pri bld" idx="*" size="64" max="80" value="${param.subject}" /></td>
</tr>
<tr>
 <td class="label">Water Cooler Channel</td>
 <td class="data"><el:combo name="id" idx="*" size="1" options="${channels}" value="${empty param.id ? 'General Discussion' : param.id}" /></td>
</tr>
<content:filter roles="Moderator,PIREP,HR,Examination">
<tr>
 <td class="label">Sticky Until</td>
 <td class="data"><el:text name="stickyDate" idx="*" size="10" max="11" value="${param.stickyDate}" />&nbsp;
<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].stickyDate')" /></td>
</tr>
</content:filter>
<tr>
 <td class="label">Upload Image</td>
 <td class="data"><el:file name="img" className="small" idx="*" size="64" max="144" onChange="void toggleImgOptions(this)" />
<c:if test="${imgBadSize}"><div class="error bld">Your attached image was too large (<fmt:int value="${imgSize}" /> bytes).</div></c:if>
<c:if test="${imgBadDim}"><div class="error bld">Your attached image was too large (<fmt:int value="${imgX}" />
 by <fmt:int value="${imgY}" /> pixels).</div></c:if>
 </td>
</tr>

<!-- Message Text -->
<tr class="title caps">
 <td colspan="2">NEW MESSAGE TEXT</td>
</tr>
<tr>
 <td class="mid" colspan="2"><el:textbox name="msgText" width="125" height="8">${param.msgText}</el:textbox></td>
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
</el:form>
<content:copyright />
</div>
</body>
</html>
