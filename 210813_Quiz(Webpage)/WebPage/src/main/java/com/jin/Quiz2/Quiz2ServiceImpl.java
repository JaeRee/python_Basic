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
		//���� ������ �ִ� s3 ������ ����
		return AmazonS3ClientBuilder
				.standard()
				.withRegion("ap-northeast-2")
				.withCredentials(getProvider())
				.build();
	}
	private AmazonRekognition getRekognition() {
		//���� ������ �ִ� rekognition ������ ����
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
//		��Ŷ�� ���� ��� ���
		List<S3ObjectSummary> fileLst = getBucketFileList();
		
//		��Ͽ� �ִ� ���� ���� ���
//		for (int i=0;i<fileLst.size();i++)
//			logger.warn(fileLst.get(i).toString());
//			logger.warn(fileLst.get(i).getKey());
		
//		Random ���
//		nextInt(����)�� �����ϸ� 0���� �������� ���� ������ ���� ��ȯ
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
//		���ɴ�
		AgeRange age = faceDetail.getAgeRange();
//		logger.warn(getAgeRange(age));
//		����
		Gender gender = faceDetail.getGender();
//		logger.warn(getGender(gender));
//		��������
		List<Emotion> emotions = faceDetail.getEmotions();
//		logger.warn("");
//		for(Emotion e : emotions)
//			logger.warn(e.getType() + " : " + e.getConfidence());
		
//		logger.warn(getEmotion(emotions.get(0)));
		
		String msg = getAgeRange(age);
		msg += getGender(gender);
		msg += "�п��� ������ ��õ�� �帮�ڽ��ϴ�.<br/>";
		msg += getEmotion(emotions.get(0));
		msg += "�� �����ô� ���� ����?(<a href='https://www.youtube.com/watch?v=W8lkIxjxUIk'>�̵�</a>)";
		return msg;
	}
	private String getEmotion(Emotion emotion) {
		if("angry".equalsIgnoreCase(emotion.getType()))	return "������ ���� ����";
		if("happy".equalsIgnoreCase(emotion.getType()))	return "�ų��� ����";
		if("sad".equalsIgnoreCase(emotion.getType()))	return "����� ���� ����";
		return "������ ����";
	}
	private String getGender(Gender gender) {
		if(gender.getConfidence()<60)	return "�߼�����";
//		equalsIgnoreCase : ��ҹ��ڿ� ���� ���� ���� �ܾ��̸�
		if("male".equalsIgnoreCase(gender.getValue()))	return "����";
		return "����";
	}
	private String getAgeRange(AgeRange age) {
		int ageAvg = (age.getLow() + age.getHigh())/2;
//		25 => 20��, 25 / 10 = 2 * 10 + "��"
		ageAvg = ageAvg / 10 * 10;
		return ageAvg + "��";
	}
	@Override
	public String Quiz3(String bucketName, String fileName) {
		List<Label> labels = AnalyzeImage(bucketName, fileName);
//		logger.warn(labels.toString());
		
//		Top3 ����
		int minConfidence = 50;
		Label []lbls = getTop3Item(labels, minConfidence);
		
//		DB ����
		Map<String, String> map = setDB();
		
//		��õ ������ ����
		String items = "";
		
		for(Label lbl : lbls) {
//			lbl.getName() : label ����
//			map.get(key) : key�� ���� value ����
			String item = map.get(lbl.getName());
//			���� item�� ��� �ִٸ� ���� ������ �����ض�.
			if(item == null) continue;
//			#�غ� ��;#������
			items += "#"+item;
		}
		
		return items+"���� ��õ�մϴ�.";
	}
	
	private Label[] getTop3Item(List<Label> labels, int minConfidence) {
		Label[] lbls = new Label[3];
		
		for(Label lbl : labels) {
//			�ּ� �ŷڵ����� ���� ���� ������ �ݺ��� ����
			if(lbl.getConfidence() < minConfidence)	break;
//			logger.warn(lbl.getName() + " : " + lbl.getConfidence());
			
			for(int i=0;i<3;i++) {
//				lbls�� �� ������ ������ �ֱ�
				if(lbls[i]==null) {
					lbls[i]=lbl;
					break;
//				lbls�� ��� ���� �ʰ� parent ������ ���� ��
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
//	map�� key, value �������� ������ ���� �� ����
	private Map<String, String> setDB() {
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("Beach", "�غ� ��");
		map.put("Coast", "������");
		map.put("Sea", "���Ȱ�");
		map.put("Peak", "�ٶ�����");
		map.put("Mountain Range", "��꺹");
		map.put("Reservoir", "���ڸ�");
		map.put("River", "��������");
		
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
