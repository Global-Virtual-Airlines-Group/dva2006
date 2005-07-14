<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<content:sysdata var="infoEmail" name="airline.mail.info" />
<!-- Sidebar Navigation Frame -->
<div id="sidebar">
<el:table ID="sidenav" pad="default" space="default">
<tr class="MenuHeader"><td>IMAGE GALLERY</td></tr>
<tr class="MenuItem">
<c:choose>
<c:when test="${empty pageContext.request.userPrincipal}">
 <td><el:cmd className="bld" url="login">LOG IN</el:cmd></td>
</c:when>
<c:otherwise>
 <td class="MenuItem sec caps">WELCOME, ${pageContext.request.remoteUser}</td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="logout">LOG OUT</el:cmd></td>
</c:otherwise>
</c:choose>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="imagegallery" linkID="ALL">ALL IMAGES</el:cmd></td>
</tr>
<c:if test="${access.canCreate}">
<tr class="MenuItem">
 <td><el:cmd url="image" op="edit">POST NEW IMAGE</el:cmd></td>
</tr>
</c:if>
<tr class="MenuHeader"><td>OUR AIRLINE</td></tr>
<tr class="MenuItem">
 <td><el:link url="/">HOME</el:link></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="channels">WATER COOLER</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="event">ONLINE EVENTS</el:cmd></td>
</tr>
<tr class="MenuHeader"><td>CONTACT US</td></tr>
<tr class="MenuItem">
 <td><el:link url="mailto:${infoEmail}">CORPORATE OFFICES</el:link></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="staff">OUR STAFF</el:cmd></td>
</tr>
<tr class="MenuHeader"><td>DVA 2006</td></tr>
<tr class="MenuItem">
 <td><el:cmd url="issues">ISSUE TRACKER</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:link url="/javadoc/index.html">JAVA API DOCS</el:link></td>
</tr>
<tr class="MenuItem">
 <td><el:link url="/junit/index.html">JUNIT RESULTS</el:link></td>
</tr>
</el:table>
</div>
