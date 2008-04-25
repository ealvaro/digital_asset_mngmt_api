package net.bzresults.astmgr.action;

import java.io.File;
import java.io.FileFilter;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import net.bzresults.astmgr.AllFilesFilter;
import net.bzresults.astmgr.AssetManager;

public class BrowseBZAssetsAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public BrowseBZAssetsAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() {
		getBZFolderContents(request.getParameter("currentBZFolder"));
	}
	
	public String getBZFolderContents(String folder) {
		String root = "/var/www/bzwebs/assets/apec";
		if (folder != null) {
			folder = root + "/" + folder + "/media";
		} else {
			folder = root;
		}

		File folderList = new File(folder);

		FileFilter filter = new AllFilesFilter();
		File[] listing = folderList.listFiles(filter);
		org.dom4j.Document dom = org.dom4j.DocumentHelper.createDocument(DocumentHelper.createElement("folder"));

		for (File item : listing) {
			if (item.isDirectory())
				dom.getRootElement().add(createFolderElement(item));

			else
				dom.getRootElement().add(createFileElement(item));
		}

		return dom.asXML();
	}

	public Element createFolderElement(File file) {
		Element element = DocumentHelper.createElement("folder");
		element.addAttribute("name", file.getName());

		return element;

	}

	public Element createFileElement(File file) {
		Element element = DocumentHelper.createElement("asset");
		element.addAttribute("file_name", file.getName());

		return element;

	}

}
