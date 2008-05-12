package net.bzresults.astmgr.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.bzresults.astmgr.AssetManager;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ZipFileAction implements IDAMAction {
	private static final Log logger = LogFactory.getLog(ZipFileAction.class);

	private HttpServletRequest request;
	private AssetManager am;

	public ZipFileAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() throws FileUploadException, IOException, Exception {
		String listFolders[] = null, listAssets[] = null;
		String zipFileName = request.getParameter("name");
		String folders = request.getParameter("folderids");
		String assets = request.getParameter("assetids");
		if (zipFileName != null) {
			if (folders != null && !folders.equals(""))
				listFolders = folders.split(",");
			if (assets != null && !assets.equals(""))
				listAssets = assets.split(",");
			am.makeZipFileInsideDAM(zipFileName, listFolders, listAssets);
			//zipFileName = am.makeZipFileSomewhere(zipFileName, listFolders, listAssets);
		} else
			throw new Exception("Invalid/missing 'name' parameter");
	}
}
