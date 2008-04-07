/**
 * 
 */
package net.bzresults.astmgr.test.utils;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

public class MockCommonsMultipartResolver extends CommonsMultipartResolver {
	private boolean empty;
	private List<MockFileItem> mFileItem;
	private MockFileItem mEmptyFileITem = new MockFileItem("file", "text/html",
			empty ? "" : "test.xml", empty ? "" : "<root/>");

	public MockCommonsMultipartResolver(List<MockFileItem> fileItem) {
		mFileItem = fileItem;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	protected FileUpload newFileUpload(FileItemFactory fileItemFactory) {
		return new MockFileUpload(fileItemFactory);
	}

	class MockFileUpload extends ServletFileUpload {
		public MockFileUpload(FileItemFactory fileItemFactory) {
			super(fileItemFactory);
		}

		public List /* FileItem */parseRequest(HttpServletRequest request)
				throws FileUploadException {
			List fileItems = new ArrayList();

			if (empty)
				fileItems.add(mEmptyFileITem);
			else
				fileItems.addAll(mFileItem);

			return fileItems;
		}
	}

}
