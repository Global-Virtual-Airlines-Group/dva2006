<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>New <content:airline /> ${isRating ? 'Equipment Transfer' : 'Additional Rating'} Request</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.eqType, 'Equipment Program to transfer into')) return false;

setSubmit();
disableButton('SubmitButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="reqType" value="${isRating ? 'ADDITIONAL RATING' : 'EQUIPMENT PROGRAM TRANSFER'}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="txrequest.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">NEW ${reqType} REQUEST</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" options="${availableEQ}" className="req" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data">Don't see an equipment program listed? <el:cmd url="promoeligibility" className="sec bld">Click Here</el:cmd>
 to see what equipment programs you are eligible to switch to or request additional ratings.</td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SubmitButton" type="submit" className="BUTTON" label="SUBMIT ${reqType} REQUEST" /></td>
</tr>
</el:table>
<c:if test="${isRating}"><el:text name="ratingOnly" type="hidden" value="true" /></c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
