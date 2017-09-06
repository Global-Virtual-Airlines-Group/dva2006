
<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Proficiency Check Rides - ${pilot.name}</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="currencyInterval" name="testing.currency.validity" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="view">
<tr class="title caps">
 <td colspan="2" class="title caps"><content:airline /> PROFICIENCY CHECK RIDES</td>
</tr>
<tr>
 <td colspan="2"><content:airline /> allows its Pilots to opt into a currency-based rating system. Examinations and Check Rides are still required in order to enter an Equipment Program or gain additional
 type ratings, but the validity of Check Rides will expire after <fmt:int value="${currencyInterval}" /> days and a currency Check Ride will need to be successfully completed in order to maintain type ratings in
 a particular aircraft type or Equipment Program.<br />
<br />
<c:if test="${!doConfirm}">
Enabling currency-based ratings is a significant step. Your Examination and Check Ride history will be examined and your type ratings will be recalculated solely based on this history, with any existing Check Rides
 or waivers valid for the specific currency interval of <fmt:int value="${currencyInterval}" /> plus 30 days.<br />
<br />
<c:if test="${!empty waiverNames}"><span class="pri bld ita">You currently qualify for the <fmt:list value="${waiverNames}" delim=", " /> equipment program(s), and do not have a current Check Ride. If you enable currency-based Check
 Rides, you will receive a waiver and need to obtain a current Check Ride for each program by <fmt:date date="${waiverExpiry}" />.</span><br /><br /></c:if>
<c:if test="${!empty ratingDelta}">After you enable currency-based Check Rides, you will lose the following equipment ratings:<br />
<br />
<fmt:list value="${ratingDelta}" delim=", " /><br />
<br /></c:if>
After you have opted into currency-based Check Rides, you can opt out again at any time. <span class="bld ita">However, any ratings that you lose while opting into this program will <span class="pri">NOT</span>
 be restored after you opt out and return to <content:airline />'s traditional type rating program.</span></c:if>
<c:if test="${doConfirm}">
${pilot.firstName}, you have opted into currency-based Check Rides. We have reviewed your Examination and Check Ride history at <content:airline /> and have recalculated your aircraft type ratings.
<c:if test="${!empty ratingDelta}"> The following equipment type ratings have been removed based on your history:<br />
<br />
<fmt:list value="${ratingDelta}" delim=", " /><br /></c:if>
<br />
 <c:if test="${!empty waiverNames}"><span class="pri bld ita">You currently qualify for the <fmt:list value="${waiverNames}" delim=", " /> equipment program(s), and do not have a current Check Ride. You have received a 
 Check Ride waiver and will need to obtain a current Check Ride for each program by <fmt:date date="${waiverExpiry}" />.</span><br /><br /></c:if>
Thank you for opting into <content:airline />'s currency-based Check Ride program. This adds an additional level of realism to our virtual airline experience and will bring a new dimension to your virtual flying career.</c:if>
 </td>
</tr>
<%@ include file="/jsp/pilot/pilotExams.jspf" %>

<!-- Button Bar -->
<tr class="title">
 <td><c:if test="${!doConfirm}"><el:cmdbutton url="currencyenable" op="true" label="ENABLE CURRENCY CHECK RIDES" /> </c:if><el:cmdbutton url="testcenter" label="RETURN TO TESTING CENTER" /></td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
