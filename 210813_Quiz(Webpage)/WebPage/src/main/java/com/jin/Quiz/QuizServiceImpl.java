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
	 * private : 내부에서만 사용하도록 제약을 걸 경우 사용
	 * static : 객체를 new 하지 않고 사용할 경우 사용
	 * final : 변수가 수정되지 않도록 고정하기 위해 사용 
	 */
	private static final Logger logger = LoggerFactory.getLogger(QuizServiceImpl.class);
	
	
	@Override
	public List<MultipartFile> UploadFile(HttpServletRequest request) {
//		logger.warn("UploadFile");
//		업로드한 파일들을 얻어 오기 위해 HttpServletRequest를 MultipartHttpServletRequest로 형변환
		MultipartHttpServletRequest multiReq = (MultipartHttpServletRequest) request;
//		업로드한 파일 정보를 iterator에 저장함.
		Iterator<String> fileNames = multiReq.getFileNames();
		
		List<MultipartFile> multiFileLst = new ArrayList<MultipartFile>();
		
//		fileNames.hasNext()은 fileNames의 값이 존재하는 동안 반복
		while(fileNames.hasNext()) {
//			jsp에서 전달 받은 input 태그의 name 정보
			String fileName = fileNames.next();
//			logger.warn(fileName);
//			input tag를 통해 전달 받은 파일 정보를 저장
			MultipartFile multiFile = multiReq.getFile(fileName);
//			저장되어 있는 파일이 비어 있지 않다면
			if(!multiFile.isEmpty())
//				파일 명 출력
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
		
		//서울 리전에 있는 s3 정보를 얻어옮
		return AmazonS3ClientBuilder
				.standard()
				.withRegion("ap-northeast-2")
				.withCredentials(provider)
				.build();
	}

	@Override
	public String UploadS3(String bucketname, MultipartFile multiFile) {
//		S3 정보 얻기
		AmazonS3 s3 = getS3();

		String fileName = null;
		InputStream data = null;
		
		try {
//			file 정보
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
//		s3 얻기
		AmazonS3 s3 = getS3();
//		acl 얻기
		AccessControlList acl = s3.getObjectAcl(bucketName, fileName);
//		acl 설정
		acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
//		s3 설정
		s3.setObjectAcl(bucketName, fileName, acl);
	}

	@Override
	public URL getImgUrl(String bucketName, String fileName) {
//		s3 얻기
		AmazonS3 s3 = getS3();
//		img URL 얻기
		return s3.getUrl(bucketName, fileName);
	}

}
