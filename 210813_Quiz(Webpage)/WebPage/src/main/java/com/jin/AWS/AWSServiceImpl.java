package com.jin.AWS;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Service
public class AWSServiceImpl implements IAWSService{
	@Value("${aws.accessKey}")private String accesskey;
	@Value("${aws.secretKey}")private String secretkey;
	
	@Override
	public String getRegionName() {
		AmazonS3 s3 = getS3();
		return s3.getRegionName();
	}
//	이 메소드는 credential에서 인증된 계정의 s3를 얻어오는 코드로 내부 코드는 변하지 않는다.
	private AmazonS3 getS3() {
//		credential과 provider는 항상 고정으로 사용되며 provider를 통해 AWS의 서비스를 호출하여 사용할 수 있다.
//		인증
		AWSCredentials credential = new BasicAWSCredentials(accesskey, secretkey);
//		provider
		AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(credential);
//		S3 정보 반환, Regions.AP_NORTHEAST_2은 서울 리전을 의미함
//		withCredentials(provider)은 계정 인증을 위해 사용함. 이로 인해 내 계정의 S3 서비스를 이용할 수 있음.
		return AmazonS3ClientBuilder.
				standard().
				withRegion(Regions.AP_NORTHEAST_2).
				withCredentials(provider).build();
	}

}
