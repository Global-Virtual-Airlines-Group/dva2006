<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Assign Check Ride for ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.crType, 'Aircraft Type')) return false;
<c:if test="${!isMine}">if (!validateText(form.comments, 6, 'Check Ride Comments')) return false;</c:if>

setSubmit();
disableButton('CourseButton');
disableButton('ProfileButton');
disableButton('AssignButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="courseride.do" method="post" link="${course}" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">FLIGHT ACADEMY ${course.name} CHECK RIDE - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Aircraft Type</td>
 <td class="data"><el:combo name="acType" idx="*" size="1" className="req" options="${actypes}" value="${param.acType}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label top">Comments</td>
<c:if test="${!isMine}">
 <td class="data"><el:textbox name="comments" idx="*" className="req" width="80%" height="4" resize="true">${rideScript.description}</el:textbox></td>
</c:if>
<c:if test="${isMine}">
 <td class="data">This is a self-assigned <content:airline /> Flight Academy Check Ride. The specific requirements of this
Check Ride will be displayed once you save the Check Ride.</td>
</c:if>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>
<c:if test="${!isMine}"><el:cmdbutton ID="ProfileButton" url="profile" link="${pilot}" label="VIEW PROFILE" /> 
<el:cmdbutton ID="CourseButton" url="course" link="${course}" label="VIEW COURSE" /> </c:if>
<el:button ID="AssignButton" type="submit" label="ASSIGN CHECK RIDE" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
