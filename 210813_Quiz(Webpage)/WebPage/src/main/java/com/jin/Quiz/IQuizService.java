package com.jin.Quiz;

import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

public interface IQuizService {
	public List<MultipartFile> UploadFile(HttpServletRequest request);
	public String UploadS3(String bucketName, MultipartFile multiFile);
	public void setGrant(String bucketName, String fileName);
	public URL getImgUrl(String bucketName, String fileName);
}
