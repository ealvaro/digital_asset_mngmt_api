package net.bzresults.astmgr.action;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.bzresults.astmgr.AssetManager;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CreateAssetAction implements IDAMAction {
	private static final Log logger = LogFactory.getLog(CreateAssetAction.class);

	private HttpServletRequest request;
	private AssetManager am;

	public CreateAssetAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() throws FileUploadException, IOException, Exception {
		 boolean isMultipart = FileUpload.isMultipartContent(request);
		 if (isMultipart) {
//		if (request instanceof MultipartHttpServletRequest) {
			multipleUpload(request, am);
		} else {
			String assetName = request.getParameter("name1");
			String filePathName = request.getParameter("file1");
			am.createAsset(filePathName, null);
		}

	}

	/**
	 * @param request
	 * @throws FileUploadException,Exception
	 */
	private void multipleUpload(HttpServletRequest request, AssetManager am) throws FileUploadException, Exception {
		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		// Parse the request
		List items = upload.parseRequest(request);
		// give u the list of items in the form
		Iterator iter = items.iterator();
		String assetName = null;
		while (iter.hasNext()) {
			FileItem item = (FileItem) iter.next();
			if (item.isFormField()) {
				assetName = item.getString();
			} else {
				am.createAssetFromFileItem(item.getName(), item);
			}
		}
	}
	/**
	 * @param request
	 * @throws FileUploadException,Exception
	 */

	// Lynn changed to use Spring's MultipartHttpServletRequest since with
	// common's upload as was before
	// it conflicted and didn't work unless commented out use of
	// MultipartHttpServletRequest in bcc-servlet.xml
	// Commenting out is that is bad until we are ready to convert all the
	// existing plugin's to use the new
	// digital-asset-manager and stop using old asset manager. By changing this,
	// we can keep use of both
	// until ready.
	// private void multipleUpload(HttpServletRequest request, AssetManager am)
	// throws FileUploadException, Exception {
	// logger.debug("in multipleUpload");
	//		
	// MultipartHttpServletRequest multi = (MultipartHttpServletRequest)
	// request;
	// Iterator iter = multi.getFileNames();
	//		
	// // NOTES: currently aaron only sends one file at a time in a request and
	// it always has name File
	// // so have to use getOriginalFileName to get the name
	//		
	// while(iter.hasNext()) {
	// String fileName = (String) iter.next();
	// logger.debug("fileName = " + fileName);
	// MultipartFile file = multi.getFile(fileName);
	// logger.debug("file size : " + file.getSize());
	// String actualFileName = file.getOriginalFilename();
	// File tmp = new File(actualFileName); // use this as quick way to parse
	// and make sure if we got a path & file
	// // as opera may give we only take the file name part
	// actualFileName = tmp.getName();
	// am.createAsset(actualFileName, file);
	// }
	// }
}
