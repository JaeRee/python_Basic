package com.jin.Quiz;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;

@Service
public class QuizServiceImpl implements IQuizService{
	/* 
	 * private : ���ο����� ����ϵ��� ������ �� ��� ���
	 * static : ��ü�� new ���� �ʰ� ����� ��� ���
	 * final : ������ �������� �ʵ��� �����ϱ� ���� ��� 
	 */
	private static final Logger logger = LoggerFactory.getLogger(QuizServiceImpl.class);
	
	
	@Override
	public List<MultipartFile> UploadFile(HttpServletRequest request) {
//		logger.warn("UploadFile");
//		���ε��� ���ϵ��� ��� ���� ���� HttpServletRequest�� MultipartHttpServletRequest�� ����ȯ
		MultipartHttpServletRequest multiReq = (MultipartHttpServletRequest) request;
//		���ε��� ���� ������ iterator�� ������.
		Iterator<String> fileNames = multiReq.getFileNames();
		
		List<MultipartFile> multiFileLst = new ArrayList<MultipartFile>();
		
//		fileNames.hasNext()�� fileNames�� ���� �����ϴ� ���� �ݺ�
		while(fileNames.hasNext()) {
//			jsp���� ���� ���� input �±��� name ����
			String fileName = fileNames.next();
//			logger.warn(fileName);
//			input tag�� ���� ���� ���� ���� ������ ����
			MultipartFile multiFile = multiReq.getFile(fileName);
//			����Ǿ� �ִ� ������ ��� ���� �ʴٸ�
			if(!multiFile.isEmpty())
//				���� �� ���
//				logger.warn(multiFile.getOriginalFilename());
				multiFileLst.add(multiFile);
		}
		return multiFileLst;
	}
	
	@Value("${aws.accessKey}")private String accesskey;
	@Value("${aws.secretKey}")private String secretkey;

	private AmazonS3 getS3() {
		AWSCredentials credential = new BasicAWSCredentials(accesskey, secretkey);
		AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(credential);
		
		//���� ������ �ִ� s3 ������ ����
		return AmazonS3ClientBuilder
				.standard()
				.withRegion("ap-northeast-2")
				.withCredentials(provider)
				.build();
	}

	@Override
	public String UploadS3(String bucketname, MultipartFile multiFile) {
//		S3 ���� ���
		AmazonS3 s3 = getS3();

		String fileName = null;
		InputStream data = null;
		
		try {
//			file ����
			fileName = new String(multiFile.getOriginalFilename().getBytes("8859_1"), "UTF-8");
//			stream
			data = multiFile.getInputStream();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		size
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(multiFile.getSize());
		
		s3.putObject(bucketname, fileName, data, metadata);
		
		return fileName;
	}

	@Override
	public void setGrant(String bucketName, String fileName) {
//		s3 ���
		AmazonS3 s3 = getS3();
//		acl ���
		AccessControlList acl = s3.getObjectAcl(bucketName, fileName);
//		acl ����
		acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
//		s3 ����
		s3.setObjectAcl(bucketName, fileName, acl);
	}

	@Override
	public URL getImgUrl(String bucketName, String fileName) {
//		s3 ���
		AmazonS3 s3 = getS3();
//		img URL ���
		return s3.getUrl(bucketName, fileName);
	}

}
