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

public class AddAssetTagAction implements IDAMAction {
	private static final Log logger = LogFactory.getLog(AddAssetTagAction.class);

	private HttpServletRequest request;
	private AssetManager am;

	public AddAssetTagAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() throws FileUploadException, IOException, Exception {
			String assetName = request.getParameter("name");
			String tagName = request.getParameter("tag");
			String tagValue = request.getParameter("value");
			am.addAssetTag(assetName, tagName, tagValue);

	}

}