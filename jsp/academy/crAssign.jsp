<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>Assign Check Ride for ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.crType, t:'Aircraft Type'});
<c:if test="${!isMine}">golgotha.form.validate({f:f.comments, l:6, t:'Check Ride Comments'});</c:if>
golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="courseride.do" method="post" link="${course}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">FLIGHT ACADEMY ${course.name} CHECK RIDE #${rideNumber} - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Aircraft Type</td>
 <td class="data"><el:combo name="acType" idx="*" size="1" className="req" options="${actypes}" value="${param.acType}" firstEntry="-" />
<c:if test="${isMine}"> <span class="pri bld ita small">It is critical that you fly the Check Ride with this aircraft. Flights flown in other aircraft will not be recognized as Check Rides!</span></c:if></td>
</tr>
<tr>
 <td class="label top">Comments</td>
<c:if test="${!isMine}">
 <td class="data"><el:textbox name="comments" idx="*" className="req" width="80%" height="4" resize="true">${rideScript.description}</el:textbox></td>
</c:if>
<c:if test="${isMine}">
 <td class="data">This is a self-assigned <content:airline /> Flight Academy Check Ride. The specific requirements of this Check Ride will be displayed once you save the Check Ride.</td>
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
