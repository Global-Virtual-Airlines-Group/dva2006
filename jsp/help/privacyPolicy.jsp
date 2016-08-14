<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<html lang="en">
<head>
<title><content:airline /> Privacy Policy</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:js name="common" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="fbClientID" name="users.facebook.id" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr"><content:airline /> PRIVACY POLICY</div>
<br />
Thank you for visiting <content:airline />. We have been in operation since 2001 and during this time have consistently strived to respect and protect the privacy and safety of our members. This page is designed to
allow you to review our privacy and information policies.<br />
<br />
<div class="sec bld">The Information We Collect</div>
<br />
Membership at <content:airline /> is contingent on providing one's name and a valid, verifiable e-mail address. These are the only pieces of personal information we require. Members may voluntarily provide additional
information such as instant messenger addresses and physical location coordinates, however this is completely voluntary. We obfuscate our pilot locations by a random distance of +/- 1.5 miles on each display.<br />
<br />
<content:airline /> also records the IP address, host name and browser information for every request and connection made to our web site and/or ACARS server software. These records are retained in perpetuity. This information is
not released publicly, and is retained for the use of <content:airline /> staff in providing new features and ensuring compliance with <content:airline /> policies and procedures.<br />
<br />
<div class="sec bld">How We Use the Information</div>
<br />
We may use the information you provide about yourself or others to fulfill requests for our products or services, to respond to inquiries about offerings and to offer other products, programs or services that may be of interest. We
sometimes use this information to communicate with you, such as to notify you when we make changes to subscriber agreements, to fulfill a request by you for an online newsletter or to contact you about your account.<br />
<br />
We sometimes use the non-personally identifiable information that we collect to improve the design and content of our web sites and to enable us to customize your Internet experience. We also may use this information to analyze site
usage, as well as to offer you products, programs or services. We will disclose information we maintain when required to do so by law, for example, in response to a court order or a subpoena. We also may disclose such information in
response to a law enforcement agency's request.<br />
<br />
Please note that if <content:airline /> is sold or otherwise disposed of, including through merger of, consolidation or sale of assets, the relevant customer database, including personally identifiable information we may possess about
you, may, in whole or in part, be sold, disposed of, transferred or otherwise disclosed as part of that transaction. Although we take appropriate measures to safeguard against unauthorized disclosures of information, we cannot assure
you that personally identifiable information that we collect will never be disclosed in a manner that is inconsistent with this Privacy Notice. Inadvertent disclosures may result, for example, when third parties misrepresent their
identities in asking the site for access to personally identifiable information about themselves for purposes of correcting possible factual errors in the data.<br />    
<br />
<div class="sec bld">Privacy Options</div>
<br />
<content:airline /> members have the option of making their e-mail addresses visible to all users (including anonymous users), other <content:airline /> members or <content:airline /> staff only. Any Instant Messaging addresses provided to
us by our members will be made visible according to the same privacy settings.<br />
<br />
<c:if test="${!empty fbClientID}">
<content:airline /> has its own Facebook application. You can give this application access to retrieve your e-mail address and publish to your Facebook news feed even when you are not logged in to the <content:airline /> web site. Doing so
allows us to publish information about your completed ACARS flights or any promotions or accomplishments you achieve as a member here at <content:airline />. You can opt out of this at any time through Facebook by removing our application's
rights to read/write your profile information.<br />
<br />
</c:if>
<div class="sec bld">Collection of Information by Third-Party Sites and Sponsors</div>
<br />
Some of our sites contain links to other sites whose information practices may be different than ours. You should consult the other sites' privacy notices, as we have no control over information that is submitted to, or collected
by, these third parties.<br />
<br />
<div class="sec bld">Cookies</div>
<br />
To enhance your online experience we use cookies. Cookies are text files we place in your computer's browser to store your preferences. Cookies, by themselves, do not tell us your e-mail address or other personally identifiable
information unless you choose to provide this information to us by, for example, registering at our site. However, once you choose to furnish the site with personally identifiable information, this information may be linked to the
data stored in the cookie.<br />
<br />
We use cookies to understand Internet usage and to improve our content. For example, we may use cookies to personalize your experience at our web pages (e.g., to recognize you by name when you return to our site) and save your password in
password-protected areas. We also may use cookies to offer you products, programs or services. Similarly, as part of an arrangement with our business partners we may also access cookies placed by others and allow others to access certain
cookies placed by us.<br />
<br />
We may also use small pieces of code such as "web beacons" or "clear gifs" to collect anonymous and aggregate advertising metrics, such as counting page views, promotion views or advertising responses. These "web beacons" may be used to deliver
cookies that conform to our cookie policy.<br /> 
<br />
<div class="sec bld">Our Commitment to Security</div>
<br />
We have put in place appropriate physical, electronic and managerial procedures to safeguard and help prevent unauthorized access, maintain data security and correctly use the information we collect.<br />
<br /> 	 
<div class="sec bld">Special Note for Parents</div>
<br />
Under US law, we cannot request personal information from children that we know are under the age of thirteen without parental consent. We do not solicit information from our members of such a nature that their age would be derived, but in certain
circumstances we will become aware that one of our members is under 13. We will immediately suspend membership at <content:airline /> until parental consent is obtained.<br />
<br />
Our goal at <content:airline /> is to ensure that all user-generated content meets our goal of a family-friendly environment suitable for all ages. We work to moderate our content to ensure it meets this standard, but cannot guarantee consistent compliance.<br />
<br/>
<div class="sec bld">Changes to this Notice</div>
<br />
This Notice may be changed by <content:airline />. The revised Notice will be posted to this page so that you are aware of the information we collect, how we use it and under what circumstances we may disclose it.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
 