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

public class UpdateAssetTitle implements IDAMAction {
	private static final Log logger = LogFactory.getLog(UpdateAssetTitle.class);

	private HttpServletRequest request;
	private AssetManager am;

	public UpdateAssetTitle(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() throws FileUploadException, IOException, Exception {
		String assetName = request.getParameter("name");
		String title = request.getParameter("title");
		am.updateAssetTitle(assetName, title);

	}

}
