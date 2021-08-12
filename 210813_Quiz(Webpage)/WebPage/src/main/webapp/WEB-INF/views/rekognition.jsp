<!-- rekognition.jsp -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
	<title>rekognition</title>
</head>
<body>
<p><a href="home">home</a></p>
<h1>rekognition</h1>

<p>1. 얼굴 상태 분석(<a href="BucketFileList">클릭</a>)</p>
<c:forEach var="imgName" items="${fileLst }">
	<!-- [명령어]?[변수명]=[데이터]&[변수명]=[데이터] -->
	<a href="AnalyzeFace?imgName=${imgName.getKey() }">${imgName.getKey() }</a><br/>
</c:forEach>

<c:forEach var="faceDetail" items="${faceDetails }">
==============================================<br/>
<ul>
	<li>얼굴 위치 : ${faceDetail.getBoundingBox() }</li>
	<li>나이 : ${faceDetail.getAgeRange() }</li>
	<li>웃음 : ${faceDetail.getSmile() }</li>
	<li>안경 : ${faceDetail.getEyeglasses() }</li>
	<li>선글래스 : ${faceDetail.getSunglasses() }</li>
	<li>성별 : ${faceDetail.getGender() }</li>
	<li>턱수염 : ${faceDetail.getBeard() }</li>
	<li>콧수염 : ${faceDetail.getMustache() }</li>
	<li>눈상태 : ${faceDetail.getEyesOpen() }</li>
	<li>입상태 : ${faceDetail.getMouthOpen() }</li>
	<li>포즈 : ${faceDetail.getPose() }</li>
	<li>품질 : ${faceDetail.getQuality() }</li>
	<li>신뢰도 : ${faceDetail.getConfidence() }</li>
</ul>
	<ul>
	<c:forEach var="emotion" items="${faceDetail.getEmotions() }">
		<li>${emotion.getType() } : ${emotion.getConfidence() }</li>	
	</c:forEach>
	</ul>
	
	<ul>
	<c:forEach var="landmark" items="${faceDetail.getLandmarks() }">
		<li>${landmark.getType() } : ${landmark.getX() }, ${landmark.getY() }</li>	
	</c:forEach>
	</ul>
</c:forEach>

<p>2. 이미지 분석</p>
<form action="AnalyzeImage">
	버킷 이름 : <input type="text" name="bucketName">
	파일 이름 : <input type="text" name="fileName">
	<input type="submit">
</form>

<c:forEach var="imgLabel" items="${imgLabels }">
	==========================================================<br/>
	${imgLabel.toString() }<br/>
	${imgLabel.getName() }<br/>
	${imgLabel.getConfidence() }<br/>
</c:forEach>
</body>
</html>

