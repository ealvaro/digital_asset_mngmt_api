/**
 * 
 */
package net.bzresults.astmgr.test.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author waltonl
 *
 */
public class MultipartTestUtils {

	private static Logger log = Logger.getLogger(MultipartTestUtils.class);
	
	// assumes you have testDuke.jpg in src/test/java 
	public static MultipartHttpServletRequest createMultipartHttpServletRequestForFile(String fileNameToUpload, 
			                                 MockHttpServletRequest req) throws Exception {
	
		File newAssetFile = new File("src/test/java/" + fileNameToUpload);
		log.debug(newAssetFile.getAbsolutePath());
		
		byte[] newAssetAsBytes = MultipartTestUtils.getBytesFromFile(newAssetFile);
        MockFileItem fileItem = new MockFileItem("image", "image/jpeg", fileNameToUpload, new String(newAssetAsBytes));
        List<MockFileItem> fileItems = new ArrayList<MockFileItem>();
        fileItems.add(fileItem);
        
		MockCommonsMultipartResolver resolver = new MockCommonsMultipartResolver(fileItems);
		
		req.setContentType("multipart/form-data");
		req.addHeader("Content-type", "multipart/form-data");
		MultipartHttpServletRequest multipartRequest = resolver.resolveMultipart(req);
		return multipartRequest;
	}	
	
	/**
	 * returns the byte array to the corresponding file
	 * 
	 * @param file
	 * @return byte[]
	 * @throws IOException
	 */
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		final int DEFAULT_BUFFER_SIZE = 1024 * 4;
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int count = 0;
		int num = 0;
		while ((num = is.read(buffer)) != -1) {
			bytestream.write(buffer, 0, num);
			count += num;
		}
		return bytestream.toByteArray();
	}
}
