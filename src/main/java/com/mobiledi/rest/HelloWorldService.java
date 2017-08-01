package com.mobiledi.rest;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sun.jersey.core.header.FormDataContentDisposition;
 
@Path("aws/")
public class HelloWorldService {
	
	private final String UPLOADED_FILE_PATH = "d:\\";
 
	@GET
	public Response getMsg() {
		String output = "Hello world ";
 
		return Response.status(200).entity(output).build();
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String uploadFile(MultipartFormDataInput input) {

		String fileName = "";

		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get("uploadedFile");

		for (InputPart inputPart : inputParts) {

		 try {

			MultivaluedMap<String, String> header = inputPart.getHeaders();
			fileName = getFileName(header);

			//convert the uploaded file to inputstream
			InputStream inputStream = inputPart.getBody(InputStream.class,null);

			byte [] bytes = IOUtils.toByteArray(inputStream);

			//constructs upload file path
			fileName = UPLOADED_FILE_PATH + fileName;

			writeFile(bytes,fileName);

			System.out.println("Done");

		  } catch (IOException e) {
			e.printStackTrace();
		  }

		}
		
		return fileName;
	}
	
	public void uploadFileToS3(File file){
		System.out.println("uploadFileToS3");
    	AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
    	String bucketName = "ntran1321-uploads";
//    	String keyName = fileDetail.getFileName();
		try {
//			CreateBucketRequest createBucket = new CreateBucketRequest(bucketName);
//			Bucket newBucket = s3client.createBucket(createBucket);
			System.out.println("Uploading a new object to S3 from a file\n");
//			File file = new File(fileDetail.getFileName());
			System.out.println(file.getAbsolutePath());
			
			ObjectMetadata omd = new ObjectMetadata();
			omd.setContentLength(file.length());
			PutObjectRequest putObjectRequest = 
					new PutObjectRequest(bucketName, file.getName(), file);
			putObjectRequest.withMetadata(omd);
			putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
			
			System.out.println(putObjectRequest.getFile());
			s3client.putObject(putObjectRequest);

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException.");
			System.out.println("Error Message: " + ace.getMessage());
		}
    }
	private String getFileName(MultivaluedMap<String, String> header) {

		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");

				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return "unknown";
	}

	//save to somewhere
	private void writeFile(byte[] content, String filename) throws IOException {

		File file = new File(filename);

		if (!file.exists()) {
			file.createNewFile();
		}

		FileOutputStream fop = new FileOutputStream(file);
		uploadFileToS3(file);

		fop.write(content);
		fop.flush();
		fop.close();

	}
}
 
