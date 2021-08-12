package com.jin.Rekognition;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DetectFacesRequest;
import com.amazonaws.services.rekognition.model.DetectFacesResult;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.FaceDetail;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;

@Service
public class RekoServiceImpl implements IRekoService{
	private static final Logger logger = LoggerFactory.getLogger(RekoServiceImpl.class);
	private final String BUCKETNAME = "infiscap-bucket";
	
	@Value("${aws.accessKey}")private String accesskey;
	@Value("${aws.secretKey}")private String secretkey;

	private AWSStaticCredentialsProvider getProvider() {
		AWSCredentials credential = new BasicAWSCredentials(accesskey, secretkey);
		return new AWSStaticCredentialsProvider(credential);
	}
	private AmazonS3 getS3() {		
		//서울 리전에 있는 s3 정보를 얻어옮
		return AmazonS3ClientBuilder
				.standard()
				.withRegion("ap-northeast-2")
				.withCredentials(getProvider())
				.build();
	}
	private AmazonRekognition getRekognition() {
		//서울 리전에 있는 rekognition 정보를 얻어옮
		return AmazonRekognitionClientBuilder
				.standard()
				.withRegion("ap-northeast-2")
				.withCredentials(getProvider())
				.build();
	}
	@Override
	public List<FaceDetail> AnalyzeFace(String fileName) {
//		S3 정보 얻기
		AmazonS3 s3 = getS3();
//		img URL 얻기
		URL imgUrl = s3.getUrl(BUCKETNAME, fileName);
//		이미지의 stream 정보를 byteBuffer로 변환, 몰라도 됨
		ByteBuffer imgBytes = getImageByteBuffer(imgUrl);
//		logger.warn(imgBytes.toString());
		
//		rekognition 정보 얻기
		AmazonRekognition rekognition = getRekognition();
//		얼굴 분석, 몰라도 됨
		List<FaceDetail> faceDetails = getFaceDetail(rekognition, imgBytes);
		
		return faceDetails;
	}
	private List<FaceDetail> getFaceDetail(AmazonRekognition rekognition, ByteBuffer imgBytes) {
//		이미지와 분석할 속성 정보 설정
		DetectFacesRequest req = new DetectFacesRequest().
				withImage(new Image().withBytes(imgBytes)).
				withAttributes("ALL");
//				withAttributes("DEFAULT");
//		rekognition을 이용한 얼굴 분석
		DetectFacesResult result = rekognition.detectFaces(req);
		return result.getFaceDetails();
	}
	private ByteBuffer getImageByteBuffer(URL imgUrl) {
		ByteBuffer imgBytes = null;
		try {
//			URL을 이용한 접근 정보 얻기, image 연결
			URLConnection conn = imgUrl.openConnection();
//			이미지를 stream 형식으로 다운 받기
			InputStream rawData = conn.getInputStream();
//			stream을 bytebuffer로 변환
			imgBytes = ByteBuffer.wrap(IOUtils.toByteArray(rawData));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return imgBytes;
	}
	@Override
	public List<S3ObjectSummary> BucketFileList() {
		AmazonS3 s3 = getS3();
		ListObjectsV2Result result = s3.listObjectsV2(BUCKETNAME);
		return result.getObjectSummaries();
	}
	@Override
	public List<Label> AnalyzeImage(String bucketName, String fileName) {
		logger.warn(bucketName);
		URL imgUrl = getS3().getUrl(bucketName, fileName);
		logger.warn(imgUrl+"");
		ByteBuffer byteBuffers = getImageByteBuffer(imgUrl);
		
		List<Label> labels = getImageDetail(getRekognition(), byteBuffers);
		return labels;
	}
	private List<Label> getImageDetail(AmazonRekognition rekognition, ByteBuffer byteBuffers) {
//		withMaxLabels 최대 label의 개수
//		withMinConfidence 최소 신뢰도
		DetectLabelsRequest req = new DetectLabelsRequest()
				.withImage( new Image().withBytes(byteBuffers))
				.withMaxLabels(200)
				.withMinConfidence(10F);
		
		DetectLabelsResult result = rekognition.detectLabels(req);
		return result.getLabels();
	}
}
