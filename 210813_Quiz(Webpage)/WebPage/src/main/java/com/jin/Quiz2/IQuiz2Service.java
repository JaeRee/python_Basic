package com.jin.Quiz2;

import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.amazonaws.services.rekognition.model.FaceDetail;

public interface IQuiz2Service {
	public List<URL> Upload(HttpServletRequest request);
	public String getRandomImg();
	public URL getImgUrl(String imgName);
	public List<FaceDetail> AnalyzeFace(String fileName);
	public String MusicRecommend(FaceDetail faceDetail);
	public String Quiz3(String bucketName, String fileName);
}
