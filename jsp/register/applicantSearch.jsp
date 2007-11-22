<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Applicant Search</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.firstName, 2, 'First (given) Name')) return false;
if (!validateText(form.lastName, 2, 'Last (family) Name')) return false;

setSubmit();
disableButton('SearchButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="appfind.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">Applicant E-Mail Address Validation</td>
</tr>
<tr>
 <td class="pri bld left" colspan="2">We seem to be having trouble finding you in our database at
 <content:airline />. Perhaps there is a problem in our system, or you've provided us an incorrect
 Applicant ID. Let's try and find you by name.</td>
</tr>
<tr>
 <td class="label">First Name</td>
 <td class="data"><el:text name="fName" idx="*" className="req" size="16" max="34" value="${param.fName}" /></td>
</tr>
<tr>
 <td class="label">Last Name</td>
 <td class="data"><el:text name="lName" idx="*" className="req" size="16" max="34" value="${param.lName}" /></td>
</tr>

<!-- Button Bar -->
<tr class="title caps">
 <td colspan="2" class="mid"><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH" /></td>
</tr>
</el:table>
</el:form>
<c:if test="${doSearch}">
<br />
<view:table className="view" pad="default" space="default" cmd="appfind">
<!-- Search Results -->
<c:if test="${!empty applicants}">
<tr class="title caps">
 <td colspan="6" class="left">SEARCH RESULTS</td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td width="10%">&nbsp;</td>
 <td width="15%">FIRST NAME</td>
 <td width="15%">LAST NAME</td>
 <td width="10%">REGISTERED ON</td>
 <td width="20%">E-MAIL DOMAIN</td>
 <td>LOCATION</td>
</tr>

<!-- Table Applicant Data -->
<c:forEach var="applicant" items="${applicants}">
<tr>
 <td><el:cmd url="appvalidate" link="${applicant}" className="small bld">THAT'S ME</el:cmd></td>
 <td class="pri bld">${applicant.firstName}</td>
 <td class="pri bld">${applicant.lastName}</td>
 <td><fmt:date fmt="d" date="${applicant.createdOn}" /></td>
 <td class="sec bld">${applicant.emailDomain}</td>
 <td>${applicant.location}</td>
</tr>
</c:forEach>
</c:if>
<c:if test="${empty applicants}">
<tr>
 <td colspan="2" class="pri bld">NO PENDING APPLICANTS MATCHING THE PROVIDED NAME WERE FOUND.</td>
</tr>
</c:if>
</view:table>
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
