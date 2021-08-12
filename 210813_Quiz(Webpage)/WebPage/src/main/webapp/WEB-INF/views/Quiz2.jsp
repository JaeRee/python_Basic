<!-- Quiz2.jsp -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
	<title>Quiz</title>
</head>
<body>
<p><a href="home">home</a></p>
<h1>Quiz</h1>

<p> 파일 업로드</p>
<form action="Quiz2UploadFile" method="post" enctype="multipart/form-data">
	<c:forEach begin="0" end="11" varStatus="s">
		<input type="file" name="uploadFile${s.count }"/><br/>
	</c:forEach>	
	<input type="submit">
</form>

<c:forEach var="url" items="${urlLst }">
	<img src="${url }"><br/>
</c:forEach>

<p> 랜덤 이미지 얻어오기(<a href="getRandomImg">클릭</a>)</p>

<a href="QuizAnalyzeFace?imgName=${imgName }">
	<img src="${imgUrl }">
</a><br/>
${msg }


<p> Quiz3</p>
<form action="Quiz3">
	버킷 이름 : <input type="text" name="bucketName">
	파일 이름 : <input type="text" name="fileName">
	<input type="submit">
</form>
${recommand_msg }
</body>
</html>

