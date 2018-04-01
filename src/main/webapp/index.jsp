<%@ taglib prefix="c" uri="http://www.springframework.org/tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Network simulator</title>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/main.css"/>">
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/react-contextmenu.css"/>">
    <script src="<c:url value="/resources/javascript/public/modules/sockjs.min.js" />" type="text/javascript"></script>
    <script src="<c:url value="/resources/javascript/public/modules/stomp.min.js" />" type="text/javascript"></script>
</head>
<body>
<div id="root"></div>
<script src="<c:url value="/resources/javascript/public/index.bundle.js" />" type="text/javascript"></script>
</body>
</html>
