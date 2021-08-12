package com.jin.Quiz2;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AgeRange;
import com.amazonaws.services.rekognition.model.DetectFacesRequest;
import com.amazonaws.services.rekognition.model.DetectFacesResult;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Emotion;
import com.amazonaws.services.rekognition.model.FaceDetail;
import com.amazonaws.services.rekognition.model.Gender;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;

@Service
public class Quiz2ServiceImpl implements IQuiz2Service{
	private static final Logger logger = LoggerFactory.getLogger(Quiz2ServiceImpl.class);
	
	private List<MultipartFile> UploadFile(HttpServletRequest request) {
		MultipartHttpServletRequest multiReq = (MultipartHttpServletRequest) request;

		Iterator<String> fileNames = multiReq.getFileNames();
		
		List<MultipartFile> multiFileLst = new ArrayList<MultipartFile>();
		
		while(fileNames.hasNext()) {
			String fileName = fileNames.next();
			MultipartFile multiFile = multiReq.getFile(fileName);
			if(!multiFile.isEmpty())
				multiFileLst.add(multiFile);
		}
		return multiFileLst;
	}
	
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

	private String UploadS3(String bucketname, MultipartFile multiFile) {
		AmazonS3 s3 = getS3();

		String fileName = null;
		InputStream data = null;
		
		try {
			fileName = new String(multiFile.getOriginalFilename().getBytes("8859_1"), "UTF-8");
			data = multiFile.getInputStream();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(multiFile.getSize());
		
		s3.putObject(bucketname, fileName, data, metadata);
		
		return fileName;
	}

	private void setGrant(String bucketName, String fileName) {
		AmazonS3 s3 = getS3();

		AccessControlList acl = s3.getObjectAcl(bucketName, fileName);
		acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
		s3.setObjectAcl(bucketName, fileName, acl);
	}

	private URL getImgUrl(String bucketName, String fileName) {
		AmazonS3 s3 = getS3();
		return s3.getUrl(bucketName, fileName);
	}

	private final String BUCKETNAME = "infiscap-bucket3";
	
	@Override
	public List<URL> Upload(HttpServletRequest request) {
		List<URL> urlLst = new ArrayList<URL>();
		
		List<MultipartFile> multiFileLst = UploadFile(request);
		for(MultipartFile multiFile:multiFileLst) {
			String fileName = UploadS3(BUCKETNAME , multiFile);
			
			setGrant(BUCKETNAME, fileName);
			URL imgUrl = getImgUrl(BUCKETNAME, fileName);
			urlLst.add(imgUrl);
		}
		return urlLst;
	}

	@Override
	public String getRandomImg() {
//		버킷의 파일 목록 얻기
		List<S3ObjectSummary> fileLst = getBucketFileList();
		
//		목록에 있는 파일 정보 출력
//		for (int i=0;i<fileLst.size();i++)
//			logger.warn(fileLst.get(i).toString());
//			logger.warn(fileLst.get(i).getKey());
		
//		Random 얻기
//		nextInt(범위)를 지정하면 0부터 범위보다 작은 랜덤한 정수 반환
		Random rand = new Random();
		int idx = rand.nextInt(fileLst.size());
//		logger.warn(idx+"");
		
		return fileLst.get(idx).getKey();
	}

	private List<S3ObjectSummary> getBucketFileList() {
//		AmazonS3 s3 = getS3();
//		ListObjectsV2Result result = s3.listObjectsV2(BUCKETNAME);
//		return result.getObjectSummaries();
		
		return getS3().listObjectsV2(BUCKETNAME).getObjectSummaries();
	}
	@Override
	public URL getImgUrl(String imgName) {
		return getS3().getUrl(BUCKETNAME, imgName);
	}

	@Override
	public List<FaceDetail> AnalyzeFace(String fileName) {
		AmazonS3 s3 = getS3();
		URL imgUrl = s3.getUrl(BUCKETNAME, fileName);
		ByteBuffer imgBytes = getImageByteBuffer(imgUrl);
		
		AmazonRekognition rekognition = getRekognition();
		List<FaceDetail> faceDetails = getFaceDetail(rekognition, imgBytes);
		
		return faceDetails;
	}
	private ByteBuffer getImageByteBuffer(URL imgUrl) {
		ByteBuffer imgBytes = null;
		try {
			URLConnection conn = imgUrl.openConnection();
			InputStream rawData = conn.getInputStream();
			imgBytes = ByteBuffer.wrap(IOUtils.toByteArray(rawData));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imgBytes;
	}
	private List<FaceDetail> getFaceDetail(AmazonRekognition rekognition, ByteBuffer imgBytes) {
		DetectFacesRequest req = new DetectFacesRequest().
				withImage(new Image().withBytes(imgBytes)).
				withAttributes("ALL");
		DetectFacesResult result = rekognition.detectFaces(req);
		return result.getFaceDetails();
	}
	@Override
	public String MusicRecommend(FaceDetail faceDetail) {
//		연령대
		AgeRange age = faceDetail.getAgeRange();
//		logger.warn(getAgeRange(age));
//		성별
		Gender gender = faceDetail.getGender();
//		logger.warn(getGender(gender));
//		감정상태
		List<Emotion> emotions = faceDetail.getEmotions();
//		logger.warn("");
//		for(Emotion e : emotions)
//			logger.warn(e.getType() + " : " + e.getConfidence());
		
//		logger.warn(getEmotion(emotions.get(0)));
		
		String msg = getAgeRange(age);
		msg += getGender(gender);
		msg += "분에게 음악을 추천해 드리겠습니다.<br/>";
		msg += getEmotion(emotions.get(0));
		msg += "을 들으시는 것은 어떨까요?(<a href='https://www.youtube.com/watch?v=W8lkIxjxUIk'>이동</a>)";
		return msg;
	}
	private String getEmotion(Emotion emotion) {
		if("angry".equalsIgnoreCase(emotion.getType()))	return "차분해 지는 음악";
		if("happy".equalsIgnoreCase(emotion.getType()))	return "신나는 음악";
		if("sad".equalsIgnoreCase(emotion.getType()))	return "기운이 나는 음악";
		return "랜덤한 음악";
	}
	private String getGender(Gender gender) {
		if(gender.getConfidence()<60)	return "중성적인";
//		equalsIgnoreCase : 대소문자와 관계 없이 같은 단어이면
		if("male".equalsIgnoreCase(gender.getValue()))	return "남자";
		return "여자";
	}
	private String getAgeRange(AgeRange age) {
		int ageAvg = (age.getLow() + age.getHigh())/2;
//		25 => 20대, 25 / 10 = 2 * 10 + "대"
		ageAvg = ageAvg / 10 * 10;
		return ageAvg + "대";
	}
	@Override
	public String Quiz3(String bucketName, String fileName) {
		List<Label> labels = AnalyzeImage(bucketName, fileName);
//		logger.warn(labels.toString());
		
//		Top3 추출
		int minConfidence = 50;
		Label []lbls = getTop3Item(labels, minConfidence);
		
//		DB 설정
		Map<String, String> map = setDB();
		
//		추천 아이템 설정
		String items = "";
		
		for(Label lbl : lbls) {
//			lbl.getName() : label 정보
//			map.get(key) : key값 관련 value 추출
			String item = map.get(lbl.getName());
//			만약 item이 비어 있다면 다음 내용을 진행해라.
			if(item == null) continue;
//			#해변 옷;#수영복
			items += "#"+item;
		}
		
		return items+"등을 추천합니다.";
	}
	
	private Label[] getTop3Item(List<Label> labels, int minConfidence) {
		Label[] lbls = new Label[3];
		
		for(Label lbl : labels) {
//			최소 신뢰도보다 작은 값이 나오면 반복문 정지
			if(lbl.getConfidence() < minConfidence)	break;
//			logger.warn(lbl.getName() + " : " + lbl.getConfidence());
			
			for(int i=0;i<3;i++) {
//				lbls에 빈 공간에 데이터 넣기
				if(lbls[i]==null) {
					lbls[i]=lbl;
					break;
//				lbls가 비어 있지 않고 parent 개수가 많은 순
				}else if(lbls[i].getParents().size() < lbl.getParents().size()) {
					Label tmp = lbls[i];
					lbls[i] = lbl;
					lbl = tmp;
				}else if(lbls[i].getParents().size() == lbl.getParents().size()) {
					if(lbls[i].getConfidence()<lbl.getConfidence()) {
						Label tmp = lbls[i];
						lbls[i] = lbl;
						lbl = tmp;
					}
				}
			}
//			for(int i=0;i<3;i++) {
//				if(lbls[i]!=null)
//					logger.warn(i + " : " +lbls[i].getName());
//			}		
//			logger.warn("");
		}
		return lbls;
	}
//	map은 key, value 형식으로 데이터 저장 및 관리
	private Map<String, String> setDB() {
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("Beach", "해변 옷");
		map.put("Coast", "수영복");
		map.put("Sea", "물안경");
		map.put("Peak", "바람막이");
		map.put("Mountain Range", "등산복");
		map.put("Reservoir", "돗자리");
		map.put("River", "구명조끼");
		
		return map;
	}
	private List<Label> AnalyzeImage(String bucketName, String fileName) {
		logger.warn(bucketName);
		URL imgUrl = getS3().getUrl(bucketName, fileName);
		logger.warn(imgUrl+"");
		ByteBuffer byteBuffers = getImageByteBuffer(imgUrl);
		
		List<Label> labels = getImageDetail(getRekognition(), byteBuffers);
		return labels;
	}
	private List<Label> getImageDetail(AmazonRekognition rekognition, ByteBuffer byteBuffers) {
		DetectLabelsRequest req = new DetectLabelsRequest()
				.withImage( new Image().withBytes(byteBuffers))
				.withMaxLabels(200)
				.withMinConfidence(10F);
		
		DetectLabelsResult result = rekognition.detectLabels(req);
		return result.getLabels();
	}
}
